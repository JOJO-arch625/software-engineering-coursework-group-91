import assert from "node:assert/strict";
import { SessionClient, assertRedirectLocation } from "../helpers/sessionClient.mjs";
import { DEMO_ACCOUNTS } from "../helpers/testConfig.mjs";
import { assertContainsText } from "../helpers/htmlAssertions.mjs";

export function registerSecurityNegativeTests(runner) {
  runner.suite("Security & Negative Tests", (suite) => {
    suite.before(async () => {
      const client = new SessionClient();
      await client.ensureServerAvailable();
    });

    suite.test("Unauthenticated user is redirected to login", async () => {
      const client = new SessionClient();
      const res = await client.request("/ta/dashboard", { method: "GET" });
      assertRedirectLocation(res, "/login");
    });

    suite.test("TA cannot access MO job editor", async () => {
      const client = new SessionClient();
      await client.login(DEMO_ACCOUNTS.ta.username, DEMO_ACCOUNTS.ta.password);
      const res = await client.request("/mo/jobs/edit", { method: "GET" });
      assert.equal(res.status, 302);
      assertRedirectLocation(res, DEMO_ACCOUNTS.ta.homePath);

      const { html } = await client.getHtml(DEMO_ACCOUNTS.ta.homePath);
      assertContainsText(html, "does not have permission to open that page");
    });

    suite.test("TA cannot access Admin routes", async () => {
      const client = new SessionClient();
      await client.login(DEMO_ACCOUNTS.ta.username, DEMO_ACCOUNTS.ta.password);
      const res = await client.request("/admin/workload", { method: "GET" });
      assert.equal(res.status, 302);
      assertRedirectLocation(res, DEMO_ACCOUNTS.ta.homePath);

      const { html } = await client.getHtml(DEMO_ACCOUNTS.ta.homePath);
      assertContainsText(html, "does not have permission to open that page");
    });

    suite.test("Cannot apply to closed job", async () => {
      const client = new SessionClient();
      await client.login(DEMO_ACCOUNTS.ta.username, DEMO_ACCOUNTS.ta.password);

      const form = new URLSearchParams();
      form.set("jobId", "job-3");
      form.set("priority", "Priority 1");
      form.set("notes", "Attempted closed job application");
      form.set("applicantSkills", "Python, pandas");
      form.set("applicantDescription", "Test application for closed posting.");

      const postResponse = await client.request("/ta/job", {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded"
        },
        body: form.toString()
      });

      assert.equal(postResponse.status, 302);

      const { html } = await client.getHtml("/ta/job?id=job-3");
      assertContainsText(html, "closed");
      assertContainsText(html, "cannot accept new applications");
    });
  });
}
