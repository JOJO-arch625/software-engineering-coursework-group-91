import assert from "node:assert/strict";
import { SessionClient } from "../helpers/sessionClient.mjs";
import { DEMO_ACCOUNTS, pageTitle } from "../helpers/testConfig.mjs";
import { assertContainsText, assertTitle } from "../helpers/htmlAssertions.mjs";

export function registerMoWorkflowTests(runner) {
  runner.suite("MO Workflow Tests - Create, Edit, Review Applications", (suite) => {
    let client;

    suite.before(async () => {
      client = new SessionClient();
      await client.ensureServerAvailable();
    });

    suite.beforeEach(async () => {
      client = new SessionClient();
      await client.login(DEMO_ACCOUNTS.mo.username, DEMO_ACCOUNTS.mo.password);
    });

    suite.test("MO can access job editor page", async () => {
      const { html, response } = await client.getHtml("/mo/jobs/edit");
      assert.equal(response.status, 200);
      assertTitle(html, pageTitle("Create Or Edit Job Posting"));
      assertContainsText(html, "Module code");
      assertContainsText(html, "Job title");
      assertContainsText(html, "Your Postings");
    });

    suite.test("MO can access dashboard with posting context", async () => {
      const { html, response } = await client.getHtml("/mo/dashboard");
      assert.equal(response.status, 200);
      assertTitle(html, pageTitle("MO Dashboard"));
      assertContainsText(html, "Open postings");
      assertContainsText(html, "Object-Oriented Programming TA");
    });

    suite.test("MO can access application review page", async () => {
      const { html, response } = await client.getHtml("/mo/review?jobId=job-2");
      assert.equal(response.status, 200);
      assertTitle(html, pageTitle("Applicant Review"));
      assertContainsText(html, "Applicant Review");
      assertContainsText(html, "Mark under review");
    });
  });
}
