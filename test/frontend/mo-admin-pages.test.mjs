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
      assertTitle(html, "MO Dashboard | INTERNATIONAL SCHOOL TA RECRUITMENT SYSTEM");
      assertContainsText(html, "EBU6304 Software Engineering - Applications");
      assertContainsText(html, "Software Engineering TA");
      assertContainsText(html, "Open postings");
      assertContainsText(html, "Pending review");
    });

    test("MO review page shows applicant detail and decision actions", async () => {
      const client = new SessionClient();
      await client.login(DEMO_ACCOUNTS.mo.username, DEMO_ACCOUNTS.mo.password);

      const { response, html } = await client.getHtml("/mo/review?jobId=job-2&appId=app-1");

      assert.equal(response.status, 200);
      assertTitle(html, "Applicant Review | INTERNATIONAL SCHOOL TA RECRUITMENT SYSTEM");
      assertContainsText(html, "Applicant Review");
      assertContainsText(html, "Applicant Detail");
      assertContainsText(html, "Yuyanchen Long");
      assertContainsText(html, "Mark under review");
      assertContainsText(html, "Mark shortlisted");
      assertContainsText(html, "Accept");
    });

    test("Admin dashboard highlights workload alerts and tracked TA records", async () => {
      const client = new SessionClient();
      await client.login(DEMO_ACCOUNTS.admin.username, DEMO_ACCOUNTS.admin.password);

      const { response, html } = await client.getHtml("/admin/workload");

      assert.equal(response.status, 200);
      assertTitle(html, "Admin Workload Dashboard | INTERNATIONAL SCHOOL TA RECRUITMENT SYSTEM");
      assertContainsText(html, "Workload Overview");
      assertContainsText(html, "Threshold: 10 weekly hours");
      assertContainsText(html, "Weekly Hours");
      assertContainsText(html, "Search TA");
      assertContainsText(html, "Overload risk");
      assertContainsText(html, "Siyu Chen");
    });
  });
}
