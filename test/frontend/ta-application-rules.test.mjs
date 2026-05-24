import assert from "node:assert/strict";
import { SessionClient } from "../helpers/sessionClient.mjs";
import { DEMO_ACCOUNTS } from "../helpers/testConfig.mjs";
import { assertContainsText } from "../helpers/htmlAssertions.mjs";

export function registerTaApplicationRulesTests(runner) {
  runner.suite("TA Application Rules Tests", (suite) => {
    let client;

    suite.before(async () => {
      client = new SessionClient();
      await client.ensureServerAvailable();
      await client.login(DEMO_ACCOUNTS.ta.username, DEMO_ACCOUNTS.ta.password);
    });

    suite.test("TA can view available jobs", async () => {
      const { html } = await client.getHtml("/ta/jobs");
      assertContainsText(html, "Available Jobs");
    });

    suite.test("TA cannot apply to a job they already applied to", async () => {
      const form = new URLSearchParams();
      form.set("jobId", "job-2");
      form.set("priority", "Priority 1");
      form.set("notes", "Duplicate application attempt.");
      form.set("applicantSkills", "Java, Python");
      form.set("applicantDescription", "Already applied to this posting.");

      const response = await client.request("/ta/job", {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded"
        },
        body: form.toString()
      });
      assert.equal(response.status, 302);
      const { html } = await client.getHtml("/ta/job?id=job-2");
      assertContainsText(html, "Application blocked. A TA can apply for at most three jobs.");
    });

    suite.test("System shows limit message when max applications reached", async () => {
      const form = new URLSearchParams();
      form.set("jobId", "job-5");
      form.set("priority", "Priority 2");
      form.set("notes", "Limit check.");
      form.set("applicantSkills", "Embedded systems");
      form.set("applicantDescription", "Checking the application limit.");

      const response = await client.request("/ta/job", {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded"
        },
        body: form.toString()
      });
      assert.equal(response.status, 302);
      const { html } = await client.getHtml("/ta/job?id=job-5");
      assertContainsText(html, "Application blocked. A TA can apply for at most three jobs.");
    });
  });
}
