import fs from "node:fs/promises";
import path from "node:path";
import { spawn } from "node:child_process";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const appUrl = process.env.APP_URL ?? "http://127.0.0.1:5173";
const apiUrl = process.env.API_URL ?? "http://localhost:8080/api";
const username = process.env.DEMO_USERNAME ?? "testuser";
const password = process.env.DEMO_PASSWORD ?? "Password123!";
const debugPort = Number(process.env.DEMO_DEBUG_PORT ?? 9700 + Math.floor(Math.random() * 200));
const captureWidth = Number(process.env.DEMO_WIDTH ?? 1440);
const captureHeight = Number(process.env.DEMO_HEIGHT ?? 900);
const outputDir = path.join(root, "docs", "demo");
const mp4Path = path.join(outputDir, "walkthrough.mp4");
const rawMp4Path = path.join(outputDir, "walkthrough-raw.mp4");
const gifPath = path.join(outputDir, "walkthrough.gif");
const palettePath = path.join(outputDir, "walkthrough-palette.png");
const cropFilter = "crop=1424:860:8:32";

const taskTitle = "Demo: Trace settings save timeout";
const taskDescription = "Reproduce the settings save timeout, attach the error log, and ask the assistant for likely causes.";
const logTitle = "Demo: Settings save request timeout";
const logSource = "frontend";
const logContent =
  "TypeError: Failed to fetch while saving user settings\n" +
  "Request: PUT /api/settings/profile\n" +
  "Browser console: net::ERR_CONNECTION_TIMED_OUT after 30000ms";
const assistantQuestion = "What is the most likely cause of this settings save timeout, and what should I check first?";

function edgeCandidates() {
  return [
    process.env.EDGE_PATH,
    "C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe",
    "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe",
    "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
    "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe"
  ].filter(Boolean);
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
      body: JSON.stringify({ username, password, email: `${username}@example.com` })
    });
  }
}

async function cleanupDemoData(headers) {
  const tasks = await api("/tasks", { headers });
  for (const task of tasks.filter((item) => item.title?.startsWith("Demo:"))) {
    await api(`/tasks/${task.id}`, { method: "DELETE", headers });
  }

  const logs = await api("/logs", { headers });
  for (const log of logs.filter((item) => item.title?.startsWith("Demo:"))) {
    await api(`/logs/${log.id}`, { method: "DELETE", headers });
  }
}

async function waitForJson(url, timeoutMs = 10000) {
  const started = Date.now();
  while (Date.now() - started < timeoutMs) {
    try {
      const response = await fetch(url);
      if (response.ok) return response.json();
    } catch {
      // Browser is still starting.
    }
    await wait(200);
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
      for (const listener of this.events.get(message.method) ?? []) listener(message.params);
    });
  }

  send(method, params = {}) {
    const id = this.nextId++;
    this.socket.send(JSON.stringify({ id, method, params }));
    return new Promise((resolve, reject) => this.pending.set(id, { resolve, reject }));
  }

  once(method) {
    return new Promise((resolve) => {
      const listener = (params) => {
        this.events.set(method, (this.events.get(method) ?? []).filter((item) => item !== listener));
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
  const pages = await waitForJson(`http://127.0.0.1:${debugPort}/json`);
  const page = pages.find((item) => item.type === "page") ?? pages[0];
  if (!page?.webSocketDebuggerUrl) {
    throw new Error("Could not find a debuggable browser page.");
  }
  const socket = new WebSocket(page.webSocketDebuggerUrl);
  await new Promise((resolve, reject) => {
    socket.addEventListener("open", resolve, { once: true });
    socket.addEventListener("error", reject, { once: true });
  });
  return new CdpClient(socket);
}

async function waitForText(client, text, timeoutMs = 15000) {
  const started = Date.now();
  while (Date.now() - started < timeoutMs) {
    const result = await client.send("Runtime.evaluate", {
      expression: `document.body && document.body.innerText.includes(${JSON.stringify(text)})`,
      returnByValue: true
    });
    if (result.result.value) return;
    await wait(250);
  }
  const debug = await client.send("Runtime.evaluate", {
    expression: `({ url: location.href, text: document.body ? document.body.innerText.slice(0, 500) : "" })`,
    returnByValue: true
  });
  throw new Error(`Timed out waiting for "${text}". Current page: ${JSON.stringify(debug.result.value)}`);
}

async function navigate(client, pathname, waitText) {
  await client.send("Runtime.evaluate", {
    expression: `history.pushState({}, "", ${JSON.stringify(pathname)}); window.dispatchEvent(new PopStateEvent("popstate"));`,
    awaitPromise: true
  });
  await waitForText(client, waitText);
  await injectCursor(client);
  await wait(800);
}

async function injectCursor(client) {
  await client.send("Runtime.evaluate", {
    expression: `
      (() => {
        let cursor = document.getElementById('demo-cursor');
        if (!cursor) {
          cursor = document.createElement('div');
          cursor.id = 'demo-cursor';
          cursor.innerHTML = '<svg width="30" height="32" viewBox="0 0 30 32" xmlns="http://www.w3.org/2000/svg"><path d="M6 3.5v23l6.2-6.3 3.5 8.2 3.8-1.6-3.4-8h8.4L6 3.5z" fill="#ffffff" stroke="#111827" stroke-width="1.8" stroke-linejoin="round"/></svg>';
          cursor.style.position = 'fixed';
          cursor.style.left = '0';
          cursor.style.top = '0';
          cursor.style.zIndex = '2147483647';
          cursor.style.pointerEvents = 'none';
          cursor.style.filter = 'drop-shadow(0 3px 4px rgba(0,0,0,0.28))';
          cursor.style.transition = 'transform 180ms ease-out';
          document.body.appendChild(cursor);
        }
      })()
    `,
    awaitPromise: true
  });
}

async function moveCursor(client, x, y, delay = 350) {
  await client.send("Runtime.evaluate", {
    expression: `document.getElementById('demo-cursor').style.transform = 'translate(${x}px, ${y}px)'`,
    awaitPromise: true
  });
  await wait(delay);
}

async function setField(client, selector, value) {
  const result = await client.send("Runtime.evaluate", {
    expression: `
      (() => {
        const element = document.querySelector(${JSON.stringify(selector)});
        if (!element) return false;
        const setter = Object.getOwnPropertyDescriptor(element.constructor.prototype, 'value').set;
        setter.call(element, ${JSON.stringify(value)});
        element.dispatchEvent(new Event('input', { bubbles: true }));
        element.dispatchEvent(new Event('change', { bubbles: true }));
        return true;
      })()
    `,
    returnByValue: true
  });
  if (!result.result.value) throw new Error(`Could not set field: ${selector}`);
}

async function typeField(client, selector, value, step = 1, delay = 90) {
  for (let i = step; i <= value.length; i += step) {
    await setField(client, selector, value.slice(0, i));
    await wait(delay);
  }
  await setField(client, selector, value);
  await wait(300);
}

async function selectField(client, selector, value) {
  await client.send("Runtime.evaluate", {
    expression: `
      (() => {
        const select = document.querySelector(${JSON.stringify(selector)});
        if (!select) return false;
        select.value = ${JSON.stringify(value)};
        select.dispatchEvent(new Event('change', { bubbles: true }));
        return true;
      })()
    `,
    awaitPromise: true
  });
  await wait(500);
}

async function clickButtonByText(client, text) {
  const result = await client.send("Runtime.evaluate", {
    expression: `
      (() => {
        const button = Array.from(document.querySelectorAll('button')).find((item) => item.textContent.trim() === ${JSON.stringify(text)});
        if (!button) return false;
        button.click();
        return true;
      })()
    `,
    returnByValue: true
  });
  if (!result.result.value) throw new Error(`Could not click button: ${text}`);
  await wait(500);
}

async function clickAccountLink(client) {
  const result = await client.send("Runtime.evaluate", {
    expression: `
      (() => {
        const link = document.querySelector('a.sidebar-account-link');
        if (!link) return false;
        link.click();
        return true;
      })()
    `,
    returnByValue: true
  });
  if (!result.result.value) throw new Error("Could not click account link.");
  await waitForText(client, "Account");
  await injectCursor(client);
  await wait(1200);
}

async function scrollAssistantAnswerIntoView(client) {
  await client.send("Runtime.evaluate", {
    expression: `
      (() => {
        const history = document.querySelector('.chat-history');
        const lastMessage = document.querySelector('.chat-message.assistant:last-of-type');
        if (history) history.scrollTop = history.scrollHeight;
        if (lastMessage) lastMessage.scrollIntoView({ block: 'center', behavior: 'smooth' });
        window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' });
      })()
    `,
    awaitPromise: true
  });
  await wait(2200);
}

async function login(client) {
  await waitForText(client, "Welcome back");
  await injectCursor(client);
  await moveCursor(client, 585, 402);
  await typeField(client, 'input[type="text"]', username, 1, 100);
  await moveCursor(client, 585, 492);
  await typeField(client, 'input[type="password"]', password, 2, 100);
  await moveCursor(client, 706, 565);
  await clickButtonByText(client, "Login");
  await waitForText(client, "Dashboard");
  await injectCursor(client);
  await wait(1200);
}

async function createTask(client) {
  await moveCursor(client, 105, 220);
  await navigate(client, "/tasks", "Tasks");
  await moveCursor(client, 398, 244);
  await typeField(client, ".content-grid form input", taskTitle, 2, 85);
  await moveCursor(client, 402, 338);
  await typeField(client, ".content-grid form textarea", taskDescription, 4, 85);
  await selectField(client, ".content-grid form select", "IN_PROGRESS");
  await selectField(client, ".content-grid form .form-row label:nth-child(2) select", "HIGH");
  await moveCursor(client, 345, 535);
  await clickButtonByText(client, "Create task");
  await waitForText(client, taskTitle);
  await wait(1200);
  await moveCursor(client, 755, 414);
  await clickButtonByText(client, "Edit");
  await waitForText(client, "Update task");
  await wait(800);
  await selectField(client, ".content-grid form select", "DONE");
  await moveCursor(client, 358, 535);
  await clickButtonByText(client, "Update task");
  await wait(1200);
}

async function createLog(client) {
  await moveCursor(client, 115, 275);
  await navigate(client, "/logs", "Error Logs");
  await moveCursor(client, 410, 244);
  await typeField(client, ".content-grid form input", logTitle, 2, 85);
  await moveCursor(client, 410, 337);
  await typeField(client, ".content-grid form label:nth-child(2) input", logSource, 1, 110);
  await moveCursor(client, 400, 430);
  await typeField(client, ".content-grid form textarea", logContent, 5, 85);
  await moveCursor(client, 350, 667);
  await clickButtonByText(client, "Save log");
  await waitForText(client, logTitle);
  await wait(1500);
}

async function askAssistant(client) {
  await moveCursor(client, 128, 330);
  await navigate(client, "/assistant", "AI Assistant");
  await waitForText(client, logTitle);
  const logId = await getContextOptionValue(client, logTitle);
  await selectField(client, ".context-panel select", logId);
  await waitForText(client, `Chat history for "${logTitle}"`);
  await moveCursor(client, 405, 812);
  await typeField(client, ".chat-form textarea", assistantQuestion, 3, 85);
  await moveCursor(client, 1350, 868);
  await clickButtonByText(client, "Send");
  await waitForAssistantResponse(client);
  await wait(1000);
  await scrollAssistantAnswerIntoView(client);
  await wait(3000);
}

async function getContextOptionValue(client, title) {
  const result = await client.send("Runtime.evaluate", {
    expression: `
      (() => {
        const option = Array.from(document.querySelectorAll('.context-panel select option')).find((item) => item.textContent.trim() === ${JSON.stringify(title)});
        return option ? option.value : "";
      })()
    `,
    returnByValue: true
  });
  if (!result.result.value) throw new Error(`Could not find assistant context option for ${title}`);
  return result.result.value;
}

async function waitForAssistantResponse(client) {
  const started = Date.now();
  while (Date.now() - started < 90000) {
    const result = await client.send("Runtime.evaluate", {
      expression: `Array.from(document.querySelectorAll('.chat-message.assistant')).length`,
      returnByValue: true
    });
    if (result.result.value > 0) return;
    await wait(600);
  }
  throw new Error("Timed out waiting for assistant response.");
}

async function finishDemo(client) {
  await moveCursor(client, 110, 748);
  await clickAccountLink(client);
  await wait(2000);
  await moveCursor(client, 115, 165);
  await navigate(client, "/", "Dashboard");
  await wait(2500);
}

function runProcess(command, args, options = {}) {
  return new Promise((resolve, reject) => {
    const child = spawn(command, args, { stdio: "inherit", ...options });
    child.on("error", reject);
    child.on("exit", (code) => {
      if (code === 0) resolve();
      else reject(new Error(`${command} exited with code ${code}`));
    });
  });
}

function waitForExit(child) {
  return new Promise((resolve) => child.on("exit", resolve));
}

async function wait(ms) {
  await new Promise((resolve) => setTimeout(resolve, ms));
}

await fs.mkdir(outputDir, { recursive: true });
const auth = await getAuth();
await cleanupDemoData({ Authorization: `Bearer ${auth.token}` });

const browserPath = await findBrowserExecutable();
const userDataDir = path.join(root, ".runtime", `readme-demo-video-profile-${Date.now()}`);
await fs.mkdir(userDataDir, { recursive: true });

const browser = spawn(browserPath, [
  `--remote-debugging-port=${debugPort}`,
  `--user-data-dir=${userDataDir}`,
  `--app=${appUrl}/login`,
  `--window-size=${captureWidth},${captureHeight}`,
  "--window-position=0,0",
  "--force-device-scale-factor=1",
  "--no-first-run",
  "--no-default-browser-check"
], { stdio: "ignore" });

let recorder;
let client;

try {
  client = await connectToPage();
  await client.send("Page.enable");
  await client.send("Runtime.enable");
  await waitForText(client, "Welcome back");
  await injectCursor(client);
  await wait(1000);

  recorder = spawn("ffmpeg", [
    "-y",
    "-f", "gdigrab",
    "-framerate", "24",
    "-draw_mouse", "0",
    "-offset_x", "0",
    "-offset_y", "0",
    "-video_size", `${captureWidth}x${captureHeight}`,
    "-i", "desktop",
    "-c:v", "libx264",
    "-preset", "veryfast",
    "-crf", "20",
    "-pix_fmt", "yuv420p",
    rawMp4Path
  ], { stdio: ["pipe", "inherit", "inherit"] });

  await login(client);
  await createTask(client);
  await createLog(client);
  await askAssistant(client);
  await finishDemo(client);

  recorder.stdin.write("q");
  recorder.stdin.end();
  await waitForExit(recorder);
  recorder = null;
} finally {
  if (recorder) {
    recorder.kill("SIGINT");
  }
  if (client) {
    client.close();
  }
  browser.kill();
}

await runProcess("ffmpeg", [
  "-y",
  "-i", rawMp4Path,
  "-vf", cropFilter,
  "-c:v", "libx264",
  "-preset", "veryfast",
  "-crf", "19",
  "-pix_fmt", "yuv420p",
  mp4Path
]);

await fs.rm(rawMp4Path, { force: true });

await runProcess("ffmpeg", [
  "-y",
  "-i", mp4Path,
  "-vf", "fps=12,scale=960:-1:flags=lanczos,palettegen",
  palettePath
]);

await runProcess("ffmpeg", [
  "-y",
  "-i", mp4Path,
  "-i", palettePath,
  "-filter_complex", "fps=12,scale=960:-1:flags=lanczos[x];[x][1:v]paletteuse=dither=bayer:bayer_scale=5",
  gifPath
]);

await fs.rm(palettePath, { force: true });
console.log(`Wrote ${mp4Path}`);
console.log(`Wrote ${gifPath}`);
