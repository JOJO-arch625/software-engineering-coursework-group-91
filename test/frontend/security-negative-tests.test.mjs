import assert from "node:assert/strict";
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
      const res = await client.request("/mo/jobs/edit", { method: "GET" });
      assertRedirectLocation(res, "/ta/dashboard");
    });

    suite.test("TA cannot access Admin routes", async () => {
      await client.login("ta.demo", "TaDemo123");
      const res = await client.request("/admin/workload", { method: "GET" });
      assertRedirectLocation(res, "/ta/dashboard");
    });

    suite.test("Cannot apply to closed job", async () => {
      await client.login("ta.demo", "TaDemo123");
      const form = new URLSearchParams();
      form.set("jobId", "job-3");
      form.set("priority", "Priority 1");
      form.set("notes", "Closed job attempt.");
      form.set("applicantSkills", "Python");
      form.set("applicantDescription", "Attempting to apply to a closed posting.");

      const res = await client.request("/ta/job", {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded"
        },
        body: form.toString()
      });
      assertRedirectLocation(res, "/ta/job");
      const { html } = await client.getHtml("/ta/job?id=job-3");
      assertContainsText(html, "This posting is closed and cannot accept new applications.");
    });
  });
}
