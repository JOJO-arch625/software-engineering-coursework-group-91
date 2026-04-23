import assert from "node:assert/strict";
import { SessionClient } from "../helpers/sessionClient.mjs";
import { DEMO_ACCOUNTS } from "../helpers/testConfig.mjs";
import { assertRedirectLocation } from "../helpers/sessionClient.mjs";

export function registerLogoutTests(runner) {
  runner.suite("Logout Tests", (suite) => {
    let client;

    suite.before(async () => {
      client = new SessionClient();
      await client.ensureServerAvailable();
    });

    suite.test("After logout, user is redirected to login", async () => {
      await client.login(DEMO_ACCOUNTS.ta.username, DEMO_ACCOUNTS.ta.password);
      const res = await client.request("/logout", { method: "GET" });
      assertRedirectLocation(res, "/login");
    });

    suite.test("After logout, dashboard is no longer accessible", async () => {
      const res = await client.request("/ta/dashboard", { method: "GET" });
      assertRedirectLocation(res, "/login");
    });
  });
}