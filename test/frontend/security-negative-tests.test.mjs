import assert from "node:strict";
import { SessionClient } from "../helpers/sessionClient.mjs";
import { assertRedirectLocation } from "../helpers/sessionClient.mjs";
import { assertContainsText } from "../helpers/htmlAssertions.mjs";

export function registerSecurityNegativeTests(runner) {
  runner.suite("Security & Negative Tests", (suite) => {
    let client;

    suite.before(async () => {
      client = new SessionClient();
      await client.ensureServerAvailable();
    });

    suite.test("Unauthenticated user is redirected to login", async () => {
      const res = await client.request("/ta/dashboard", { method: "GET" });
      assertRedirectLocation(res, "/login");
    });

    suite.test("TA cannot access MO routes", async () => {
      await client.login("ta.demo", "TaDemo123");
      const res = await client.request("/mo/create-job", { method: "GET" });
      assertRedirectLocation(res, "/access-denied");
    });

    suite.test("TA cannot access Admin routes", async () => {
      await client.login("ta.demo", "TaDemo123");
      const res = await client.request("/admin/workload", { method: "GET" });
      assertRedirectLocation(res, "/access-denied");
    });

    suite.test("Cannot apply to closed job", async () => {
      await client.login("ta.demo", "TaDemo123");
      const res = await client.request("/jobs/apply/closed-job-1", { method: "POST" });
      const html = await res.text();
      assertContainsText(html, "closed");
    });
  });
}