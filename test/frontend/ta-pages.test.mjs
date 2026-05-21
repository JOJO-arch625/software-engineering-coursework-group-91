import assert from "node:assert/strict";
import { assertContainsText, assertNotContainsText, assertTitle } from "../helpers/htmlAssertions.mjs";
import { SessionClient } from "../helpers/sessionClient.mjs";
import { DEMO_ACCOUNTS } from "../helpers/testConfig.mjs";

export default function registerTaPageTests(runner) {
  runner.suite("TA frontend pages", ({ before, beforeEach, test }) => {
    before(async () => {
      const client = new SessionClient();
      await client.ensureServerAvailable();
    });

    let client;

    beforeEach(async () => {
      client = new SessionClient();
      await client.login(DEMO_ACCOUNTS.ta.username, DEMO_ACCOUNTS.ta.password);
    });

    test("jobs page shows only open postings to TA users", async () => {
      const { response, html } = await client.getHtml("/ta/jobs");

      assert.equal(response.status, 200);
      assertTitle(html, "Browse Job Postings | TA Recruitment System");
      assertContainsText(html, "Available Jobs");
      assertContainsText(html, "Object-Oriented Programming TA");
      assertContainsText(html, "Digital Systems TA");
      assertContainsText(html, "Responsible MO");
      assertContainsText(html, "Application deadline");
      assertNotContainsText(html, "Data Analytics TA");
      assertNotContainsText(html, "Closed");
    });

    test("applications page keeps rejected applications visible", async () => {
      const { response, html } = await client.getHtml("/ta/applications");

      assert.equal(response.status, 200);
      assertTitle(html, "My Applications | TA Recruitment System");
      assertContainsText(html, "Visible rejection rule");
      assertContainsText(html, "Digital Systems TA");
      assertContainsText(html, "Under Review");
      assertContainsText(html, "MO review");
      assertContainsText(html, "Data Analytics TA");
      assertContainsText(html, "Rejected");
    });

    test("dashboard shows explainable AI recommendations", async () => {
      const { response, html } = await client.getHtml("/ta/dashboard");

      assert.equal(response.status, 200);
      assertContainsText(html, "AI Job Recommendations");
      assertContainsText(html, "AI recommendation is for reference only");
      assertContainsText(html, "Matched");
      assertContainsText(html, "match rate");
    });

    test("job detail page exposes the application form and optional AI summary", async () => {
      const { response, html } = await client.getHtml("/ta/job?id=job-1");

      assert.equal(response.status, 200);
      assertTitle(html, "Job Detail And Application | TA Recruitment System");
      assertContainsText(html, "Apply To This Job");
      assertContainsText(html, "Preference ranking");
      assertContainsText(html, "Submit application");
      assertContainsText(html, "Optional AI Fit Summary");
    });
  });
}
