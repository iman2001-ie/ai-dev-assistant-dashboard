import fs from "node:fs/promises";
import path from "node:path";
import { createRequire } from "node:module";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.resolve(__dirname, "..");
const framesDir = path.join(root, "docs", "demo", "frames");
const runtimePackage = path.join(root, ".runtime", "demo-tools", "package.json");
const appUrl = "http://127.0.0.1:5173";
const apiUrl = "http://localhost:8080/api";

let playwright;
try {
  const require = createRequire(runtimePackage);
  playwright = require("playwright");
} catch {
  throw new Error(
    "Playwright is required to capture demo frames. Install it locally with: npm install --prefix .runtime/demo-tools playwright"
  );
}

async function api(pathname, options = {}) {
  const response = await fetch(`${apiUrl}${pathname}`, {
    headers: { "Content-Type": "application/json", ...(options.headers ?? {}) },
    ...options
  });

  if (!response.ok) {
    throw new Error(`${options.method ?? "GET"} ${pathname} failed: ${response.status} ${await response.text()}`);
  }

  return response.status === 204 ? null : response.json();
}

async function cleanupDemoData() {
  const tasks = await api("/tasks");
  for (const task of tasks.filter((item) => item.title?.includes("Demo:"))) {
    await api(`/tasks/${task.id}`, { method: "DELETE" });
  }

  const logs = await api("/logs");
  for (const log of logs.filter((item) => item.title?.includes("Demo:"))) {
    await api(`/chat/history?errorLogId=${log.id}`, { method: "DELETE" });
    await api(`/logs/${log.id}`, { method: "DELETE" });
  }
}

async function launchBrowser() {
  try {
    return await playwright.chromium.launch({ headless: true });
  } catch {
    return playwright.chromium.launch({ channel: "msedge", headless: true });
  }
}

async function capture(page, file, delay) {
  await page.waitForTimeout(500);
  await page.screenshot({ path: path.join(framesDir, file), fullPage: false });
  return { file, delay };
}

await fs.mkdir(framesDir, { recursive: true });
await cleanupDemoData();

const browser = await launchBrowser();
const context = await browser.newContext({
  viewport: { width: 1440, height: 900 },
  deviceScaleFactor: 1
});
const page = await context.newPage();
const frames = [];

await page.goto(`${appUrl}/`);
frames.push(await capture(page, "01-dashboard.png", 2600));

await page.goto(`${appUrl}/tasks`);
await page.getByPlaceholder("Fix failing integration test").fill("Demo: Add assistant smoke test");
await page
  .getByPlaceholder("What needs to be done?")
  .fill("Create a focused test task, move it through the workflow, and verify the dashboard updates.");
await page.locator("form").first().getByLabel("Status").selectOption("IN_PROGRESS");
await page.locator("form").first().getByLabel("Priority").selectOption("HIGH");
frames.push(await capture(page, "02-task-form-filled.png", 2800));
await page.getByRole("button", { name: "Create task" }).click();
await page.getByRole("heading", { name: "Demo: Add assistant smoke test" }).waitFor();
frames.push(await capture(page, "03-task-created.png", 2800));

await page
  .locator("article")
  .filter({ hasText: "Demo: Add assistant smoke test" })
  .getByRole("button", { name: "Edit" })
  .click();
await page.getByRole("button", { name: "Update task" }).waitFor();
await page.locator("form").first().getByLabel("Status").selectOption("DONE");
frames.push(await capture(page, "04-task-editing.png", 2800));
await page.getByRole("button", { name: "Update task" }).click();
await page.waitForTimeout(700);
frames.push(await capture(page, "05-task-updated.png", 2600));

await page.goto(`${appUrl}/logs`);
await page.getByPlaceholder("Database connection refused").fill("Demo: React state update warning");
await page.getByPlaceholder("backend, frontend, CI").fill("frontend");
await page
  .getByPlaceholder("Paste stack trace or error output")
  .fill(
    "Warning: Cannot update a component while rendering a different component.\nCheck the render method and move state updates into an event handler or effect."
  );
frames.push(await capture(page, "06-log-form-filled.png", 3000));
await page.getByRole("button", { name: "Save log" }).click();
await page.getByRole("heading", { name: "Demo: React state update warning" }).waitFor();
frames.push(await capture(page, "07-log-saved.png", 2800));

await page.goto(`${appUrl}/assistant`);
const logs = await api("/logs");
const demoLog = logs.find((log) => log.title === "Demo: React state update warning");
if (!demoLog) {
  throw new Error("Demo log was not created.");
}
await api(`/chat/history?errorLogId=${demoLog.id}`, { method: "DELETE" });
await page.locator(".context-panel select").selectOption(String(demoLog.id));
await page.getByText("Ask the assistant about this context.").waitFor();
await page.getByPlaceholder("Ask about Demo: React state update warning").fill(
  "Why was this warning caused, and what should I check first?"
);
frames.push(await capture(page, "08-assistant-question.png", 3200));
await page.getByRole("button", { name: "Send" }).click();
await page.waitForFunction(() => {
  const assistantMessage = document.querySelector(".chat-message.assistant");
  const sendButton = Array.from(document.querySelectorAll("button")).find((button) => button.textContent?.trim() === "Send");
  return Boolean(assistantMessage && sendButton);
});
await page.waitForTimeout(900);
frames.push(await capture(page, "09-assistant-response.png", 5200));

await page.goto(`${appUrl}/`);
frames.push(await capture(page, "10-dashboard-updated.png", 3800));

await fs.writeFile(path.join(framesDir, "manifest.json"), `${JSON.stringify(frames, null, 2)}\n`);
await browser.close();

console.log(`Captured ${frames.length} demo frames in ${framesDir}`);
