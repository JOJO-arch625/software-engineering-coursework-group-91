import assert from "node:strict";
import { SessionClient } from "../helpers/sessionClient.mjs";
import { DEMO_ACCOUNTS } from "../helpers/testConfig.mjs";
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

    suite.test("MO can access job creation page", async () => {
      const { html } = await client.getHtml("/mo/create-job");
      assertTitle(html, "Create Job Posting");
      assertContainsText(html, "Module Code");
      assertContainsText(html, "Job Title");
    });

    suite.test("MO can access job list page", async () => {
      const { html } = await client.getHtml("/mo/jobs");
      assertTitle(html, "My Job Postings");
      assertContainsText(html, "Posted Jobs");
    });

    suite.test("MO can access application review page", async () => {
      const { html } = await client.getHtml("/mo/applications");
      assertContainsText(html, "Applications");
    });
  });
}