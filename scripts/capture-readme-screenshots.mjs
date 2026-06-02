import fs from "node:fs/promises";
import path from "node:path";
import { spawn } from "node:child_process";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const screenshotsDir = path.join(root, "docs", "screenshots");
const appUrl = process.env.APP_URL ?? "http://127.0.0.1:5173";
const apiUrl = process.env.API_URL ?? "http://localhost:8080/api";
const username = process.env.SCREENSHOT_USERNAME ?? "testuser";
const password = process.env.SCREENSHOT_PASSWORD ?? "Password123!";
const debugPort = Number(process.env.SCREENSHOT_DEBUG_PORT ?? 9333);

const screenshotRoutes = [
  { name: "login", url: `${appUrl}/login`, waitForText: "Welcome back" },
  { name: "dashboard", url: `${appUrl}/`, waitForText: "Dashboard" },
  { name: "tasks", url: `${appUrl}/tasks`, waitForText: "Tasks" },
  { name: "error-logs", url: `${appUrl}/logs`, waitForText: "Error Logs" },
  { name: "account", url: `${appUrl}/account`, waitForText: "Account" }
];

function edgeCandidates() {
  const candidates = [
    process.env.EDGE_PATH,
    "C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe",
    "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe",
    "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
    "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe"
  ];

  return candidates.filter(Boolean);
}

async function findBrowserExecutable() {
  for (const candidate of edgeCandidates()) {
    try {
      await fs.access(candidate);
      return candidate;
    } catch {
      // Try the next common browser path.
    }
  }

  throw new Error("Could not find Edge or Chrome. Set EDGE_PATH to a Chromium-based browser executable.");
}

async function api(pathname, options = {}) {
  const response = await fetch(`${apiUrl}${pathname}`, {
    headers: { "Content-Type": "application/json", ...(options.headers ?? {}) },
    ...options
  });
  const text = await response.text();
  const json = text ? JSON.parse(text) : null;

  if (!response.ok) {
    throw new Error(`${options.method ?? "GET"} ${pathname} failed: ${response.status} ${text}`);
  }

  return json;
}

async function getAuth() {
  try {
    return await api("/auth/login", {
      method: "POST",
      body: JSON.stringify({ username, password })
    });
  } catch {
    return api("/auth/register", {
      method: "POST",
      body: JSON.stringify({
        username,
        password,
        email: `${username}@example.com`
      })
    });
  }
}

async function findAssistantLog(headers) {
  const logs = await api("/logs", { headers });
  const matchingLogs = logs.filter((log) => /react state update warning/i.test(log.title ?? ""));

  if (matchingLogs.length === 0) {
    throw new Error('Could not find a "React state update warning" error log for the screenshot.');
  }

  for (const log of matchingLogs) {
    const history = await api(`/chat/history?errorLogId=${log.id}`, { headers });
    if (history.length > 0) {
      return log;
    }
  }

  return matchingLogs.find((log) => log.title === "React state update warning") ?? matchingLogs[0];
}

async function waitForJson(url, timeoutMs = 10000) {
  const started = Date.now();
  while (Date.now() - started < timeoutMs) {
    try {
      const response = await fetch(url);
      if (response.ok) {
        return response.json();
      }
    } catch {
      // Browser is still starting.
    }
    await new Promise((resolve) => setTimeout(resolve, 200));
  }
  throw new Error(`Timed out waiting for ${url}`);
}

class CdpClient {
  constructor(socket) {
    this.socket = socket;
    this.nextId = 1;
    this.pending = new Map();
    this.events = new Map();

    socket.addEventListener("message", (event) => {
      const message = JSON.parse(event.data);
      if (message.id && this.pending.has(message.id)) {
        const { resolve, reject } = this.pending.get(message.id);
        this.pending.delete(message.id);
        if (message.error) reject(new Error(message.error.message));
        else resolve(message.result);
        return;
      }

      const listeners = this.events.get(message.method) ?? [];
      for (const listener of listeners) listener(message.params);
    });
  }

  send(method, params = {}) {
    const id = this.nextId++;
    this.socket.send(JSON.stringify({ id, method, params }));
    return new Promise((resolve, reject) => {
      this.pending.set(id, { resolve, reject });
    });
  }

  once(method) {
    return new Promise((resolve) => {
      const listener = (params) => {
        const listeners = this.events.get(method) ?? [];
        this.events.set(method, listeners.filter((item) => item !== listener));
        resolve(params);
      };
      this.events.set(method, [...(this.events.get(method) ?? []), listener]);
    });
  }

  close() {
    this.socket.close();
  }
}

async function connectToPage() {
  await waitForJson(`http://127.0.0.1:${debugPort}/json/version`);
  const response = await fetch(`http://127.0.0.1:${debugPort}/json/new?${encodeURIComponent("about:blank")}`, {
    method: "PUT"
  });
  if (!response.ok) {
    throw new Error(`Could not create browser tab: ${response.status} ${await response.text()}`);
  }
  const target = await response.json();
  const socket = new WebSocket(target.webSocketDebuggerUrl);
  await new Promise((resolve, reject) => {
    socket.addEventListener("open", resolve, { once: true });
    socket.addEventListener("error", reject, { once: true });
  });
  return new CdpClient(socket);
}

async function navigate(client, url, waitForText) {
  const loadEvent = client.once("Page.loadEventFired");
  await client.send("Page.navigate", { url });
  await loadEvent;
  await waitForTextInPage(client, waitForText);
  await client.send("Runtime.evaluate", {
    expression: "window.scrollTo(0, 0)",
    awaitPromise: true
  });
  await new Promise((resolve) => setTimeout(resolve, 600));
}

async function waitForTextInPage(client, text, timeoutMs = 10000) {
  const started = Date.now();
  const escaped = JSON.stringify(text);
  while (Date.now() - started < timeoutMs) {
    const result = await client.send("Runtime.evaluate", {
      expression: `document.body && document.body.innerText.includes(${escaped})`,
      returnByValue: true
    });
    if (result.result.value) return;
    await new Promise((resolve) => setTimeout(resolve, 250));
  }
  const debug = await client.send("Runtime.evaluate", {
    expression: `({ url: location.href, text: document.body ? document.body.innerText.slice(0, 500) : "" })`,
    returnByValue: true
  });
  throw new Error(`Timed out waiting for page text: ${text}. Current page: ${JSON.stringify(debug.result.value)}`);
}

async function loginThroughUi(client) {
  await navigate(client, `${appUrl}/login`, "Welcome back");
  const expression = `
    (() => {
      const setNativeValue = (element, value) => {
        const setter = Object.getOwnPropertyDescriptor(element.constructor.prototype, 'value').set;
        setter.call(element, value);
        element.dispatchEvent(new Event('input', { bubbles: true }));
      };
      const usernameInput = document.querySelector('input[type="text"]');
      const passwordInput = document.querySelector('input[type="password"]');
      const form = document.querySelector('form');
      if (!usernameInput || !passwordInput || !form) return false;
      setNativeValue(usernameInput, ${JSON.stringify(username)});
      setNativeValue(passwordInput, ${JSON.stringify(password)});
      form.requestSubmit();
      return true;
    })()
  `;
  const result = await client.send("Runtime.evaluate", { expression, returnByValue: true });
  if (!result.result.value) {
    throw new Error("Could not submit the login form.");
  }
  await waitForTextInPage(client, "Tasks");
}

async function navigateSpa(client, url, waitForText) {
  const pathname = new URL(url).pathname;
  await client.send("Runtime.evaluate", {
    expression: `history.pushState({}, "", ${JSON.stringify(pathname)}); window.dispatchEvent(new PopStateEvent("popstate"));`,
    awaitPromise: true
  });
  await waitForTextInPage(client, waitForText);
  await client.send("Runtime.evaluate", {
    expression: "window.scrollTo(0, 0)",
    awaitPromise: true
  });
  await new Promise((resolve) => setTimeout(resolve, 600));
}

async function selectAssistantLog(client, log) {
  await navigateSpa(client, `${appUrl}/assistant`, "AI Assistant");
  await waitForTextInPage(client, log.title);
  const expression = `
    (() => {
      const select = document.querySelector('.context-panel select');
      if (!select) return false;
      select.value = ${JSON.stringify(String(log.id))};
      select.dispatchEvent(new Event('change', { bubbles: true }));
      return true;
    })()
  `;
  const result = await client.send("Runtime.evaluate", { expression, returnByValue: true });
  if (!result.result.value) {
    throw new Error("Could not select assistant log context.");
  }
  await waitForTextInPage(client, `Chat history for "${log.title}"`);
  await new Promise((resolve) => setTimeout(resolve, 1000));
}

async function capture(client, filename) {
  const result = await client.send("Page.captureScreenshot", {
    format: "png",
    fromSurface: true,
    captureBeyondViewport: false
  });
  await fs.writeFile(path.join(screenshotsDir, filename), Buffer.from(result.data, "base64"));
}

await fs.mkdir(screenshotsDir, { recursive: true });
const auth = await getAuth();
const headers = { Authorization: `Bearer ${auth.token}` };
const assistantLog = await findAssistantLog(headers);

const browserPath = await findBrowserExecutable();
const userDataDir = path.join(root, ".runtime", `readme-screenshots-profile-${Date.now()}`);
await fs.mkdir(userDataDir, { recursive: true });

const browser = spawn(browserPath, [
  "--headless=new",
  `--remote-debugging-port=${debugPort}`,
  `--user-data-dir=${userDataDir}`,
  "--disable-gpu",
  "--no-first-run",
  "--no-default-browser-check",
  "--hide-scrollbars",
  "about:blank"
], { stdio: "ignore" });

try {
  const client = await connectToPage();
  await client.send("Page.enable");
  await client.send("Runtime.enable");
  await client.send("Emulation.setDeviceMetricsOverride", {
    width: 1920,
    height: 1080,
    deviceScaleFactor: 1,
    mobile: false
  });

  await navigate(client, `${appUrl}/login`, "Welcome back");
  await capture(client, "login.png");

  await loginThroughUi(client);
  for (const route of screenshotRoutes.filter((item) => item.name !== "login")) {
    await navigateSpa(client, route.url, route.waitForText);
    await capture(client, `${route.name}.png`);
  }

  await selectAssistantLog(client, assistantLog);
  await capture(client, "ai-assistant.png");

  client.close();
  console.log(`Captured README screenshots for ${username}. Assistant context: ${assistantLog.title}`);
} finally {
  browser.kill();
}
