import assert from "node:assert/strict";
import { assertContainsText, assertTitle } from "../helpers/htmlAssertions.mjs";
import { SessionClient } from "../helpers/sessionClient.mjs";
import { DEMO_ACCOUNTS } from "../helpers/testConfig.mjs";

export default function registerMoAndAdminPageTests(runner) {
  runner.suite("MO and Admin frontend pages", ({ before, test }) => {
    before(async () => {
      const client = new SessionClient();
      await client.ensureServerAvailable();
    });

    test("MO dashboard shows posting and applicant review context", async () => {
      const client = new SessionClient();
      await client.login(DEMO_ACCOUNTS.mo.username, DEMO_ACCOUNTS.mo.password);

      const { response, html } = await client.getHtml("/mo/dashboard");

      assert.equal(response.status, 200);
      assertTitle(html, "MO Dashboard | TA Recruitment System");
      assertContainsText(html, "My Job Postings");
      assertContainsText(html, "Object-Oriented Programming TA");
      assertContainsText(html, "Decision Support");
      assertContainsText(html, "Pending review");
    });

    test("MO review page shows applicant detail and decision actions", async () => {
      const client = new SessionClient();
      await client.login(DEMO_ACCOUNTS.mo.username, DEMO_ACCOUNTS.mo.password);

      const { response, html } = await client.getHtml("/mo/review?jobId=job-2&appId=app-1");

      assert.equal(response.status, 200);
      assertTitle(html, "Applicant Review | TA Recruitment System");
      assertContainsText(html, "Applicant Review");
      assertContainsText(html, "Selected Applicant Detail");
      assertContainsText(html, "Yuyanchen Long");
      assertContainsText(html, "Mark under review");
      assertContainsText(html, "Accept applicant");
    });

    test("Admin dashboard highlights workload alerts and tracked TA records", async () => {
      const client = new SessionClient();
      await client.login(DEMO_ACCOUNTS.admin.username, DEMO_ACCOUNTS.admin.password);

      const { response, html } = await client.getHtml("/admin/workload");

      assert.equal(response.status, 200);
      assertTitle(html, "Admin Workload Dashboard | TA Recruitment System");
      assertContainsText(html, "Workload Overview");
      assertContainsText(html, "Threshold: 3 accepted jobs");
      assertContainsText(html, "Overload risk");
      assertContainsText(html, "Siyu Chen");
    });
  });
}
