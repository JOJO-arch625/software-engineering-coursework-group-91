import assert from "node:assert/strict";
import { buildUrl, DEFAULT_BASE_URL } from "./testConfig.mjs";

function getSetCookieHeaders(response) {
  if (typeof response.headers.getSetCookie === "function") {
    return response.headers.getSetCookie();
  }

  const rawHeader = response.headers.get("set-cookie");
  return rawHeader ? [rawHeader] : [];
}

export class SessionClient {
  constructor(baseUrl = DEFAULT_BASE_URL) {
    this.baseUrl = baseUrl;
    this.cookies = new Map();
  }

  async ensureServerAvailable() {
    try {
      const response = await fetch(this.resolve("/login"), {
        method: "GET",
        redirect: "manual"
      });
      assert.ok(
        response.status >= 200 && response.status < 400,
        `Expected /login to be reachable, but got status ${response.status}.`
      );
    } catch (error) {
      throw new Error(
        `Frontend test target is unavailable at ${this.baseUrl}. Start the web app first, then rerun the tests.\nOriginal error: ${error.message}`
      );
    }
  }

  resolve(pathname) {
    return new URL(pathname, this.baseUrl).toString();
  }

  async request(pathname, options = {}) {
    const response = await fetch(this.resolve(pathname), {
      redirect: "manual",
      ...options,
      headers: {
        ...(options.headers || {}),
        ...(this.buildCookieHeader() ? { Cookie: this.buildCookieHeader() } : {})
      }
    });

    this.storeCookies(response);

    return response;
  }

  async getHtml(pathname) {
    const response = await this.request(pathname, {
      method: "GET"
    });
    const html = await response.text();
    return {
      response,
      html
    };
  }

  async login(username, password) {
    const form = new URLSearchParams();
    form.set("username", username);
    form.set("password", password);

    return this.request("/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded"
      },
      body: form.toString()
    });
  }

  buildCookieHeader() {
    if (this.cookies.size === 0) {
      return "";
    }
    return Array.from(this.cookies.entries())
      .map(([name, value]) => `${name}=${value}`)
      .join("; ");
  }

  storeCookies(response) {
    for (const header of getSetCookieHeaders(response)) {
      const pair = header.split(";", 1)[0];
      const separatorIndex = pair.indexOf("=");
      if (separatorIndex <= 0) {
        continue;
      }
      const name = pair.slice(0, separatorIndex).trim();
      const value = pair.slice(separatorIndex + 1).trim();
      this.cookies.set(name, value);
    }
  }
}

export function assertRedirectLocation(response, expectedPath) {
  const location = response.headers.get("location");
  assert.ok(location, "Expected response to include a Location header.");
  assert.equal(new URL(location, buildUrl("/")).pathname, expectedPath);
}
