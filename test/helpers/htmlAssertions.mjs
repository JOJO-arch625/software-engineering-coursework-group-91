import assert from "node:assert/strict";

function normalize(html) {
  return html.replace(/\s+/g, " ").trim();
}

export function assertContainsText(html, expectedText) {
  assert.ok(
    normalize(html).includes(expectedText),
    `Expected HTML to contain text: ${expectedText}`
  );
}

export function assertNotContainsText(html, unexpectedText) {
  assert.ok(
    !normalize(html).includes(unexpectedText),
    `Expected HTML not to contain text: ${unexpectedText}`
  );
}

export function assertTitle(html, expectedTitle) {
  const match = html.match(/<title>([^<]+)<\/title>/i);
  assert.ok(match, "Expected page HTML to include a <title> element.");
  assert.equal(match[1].trim(), expectedTitle);
}
