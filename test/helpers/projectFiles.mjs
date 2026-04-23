import { readFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";

const helperDirectory = path.dirname(fileURLToPath(import.meta.url));
const testRoot = path.resolve(helperDirectory, "..");
const projectRoot = path.resolve(testRoot, "..");

export function resolveProjectPath(...segments) {
  return path.join(projectRoot, ...segments);
}

export async function readProjectFile(...segments) {
  return readFile(resolveProjectPath(...segments), "utf8");
}

export async function readProjectJson(...segments) {
  const content = await readProjectFile(...segments);
  return JSON.parse(content);
}
