import fs from "node:fs";
import path from "node:path";
import { createRequire } from "node:module";
import { fileURLToPath } from "node:url";

const require = createRequire(new URL("../frontend/package.json", import.meta.url));
const GIFEncoder = require("gif-encoder-2");
const sharp = require("sharp");

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.resolve(__dirname, "..");
const framesDir = path.join(root, "docs", "demo", "frames");
const manifestPath = path.join(framesDir, "manifest.json");

const manifest = JSON.parse(fs.readFileSync(manifestPath, "utf8"));

const frameMetadata = await Promise.all(
  manifest.map(async (frame) => {
    const filePath = path.join(framesDir, frame.file);
    const metadata = await sharp(filePath).metadata();
    return { ...frame, filePath, width: metadata.width, height: metadata.height };
  })
);
const width = Math.max(...frameMetadata.map((frame) => frame.width));
const height = Math.max(...frameMetadata.map((frame) => frame.height));

const frames = [];
for (const frame of frameMetadata) {
  const { data } = await sharp(frame.filePath)
    .extend({
      top: 0,
      bottom: height - frame.height,
      left: 0,
      right: width - frame.width,
      background: "#f4f7fb"
    })
    .ensureAlpha()
    .raw()
    .toBuffer({ resolveWithObject: true });
  frames.push({ data, delay: frame.delay });
}

const encoder = new GIFEncoder(width, height);
encoder.start();
encoder.setRepeat(0);
encoder.setQuality(1);

for (const frame of frames) {
  encoder.setDelay(frame.delay);
  encoder.addFrame(frame.data);
}
encoder.finish();

const outputPath = path.join(root, "docs", "demo", "walkthrough.gif");
fs.mkdirSync(path.dirname(outputPath), { recursive: true });
fs.writeFileSync(outputPath, encoder.out.getData());

console.log(`Wrote ${outputPath} (${width}x${height})`);
