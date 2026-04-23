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
      await client.login(DEMO_ACCOUNTS.ta.username, DEMO_ACCOUNTS.password);
    });

    suite.test("TA can view available jobs", async () => {
      const { html } = await client.getHtml("/ta/jobs");
      assertContainsText(html, "Available Jobs");
    });

    suite.test("TA cannot apply to a job they already applied to", async () => {
      const { html } = await client.getHtml("/ta/jobs");
      assertContainsText(html, "Already Applied");
    });

    suite.test("System shows limit message when max applications reached", async () => {
      const { html } = await client.getHtml("/ta/jobs");
      assertContainsText(html, "maximum number of applications");
    });
  });
}