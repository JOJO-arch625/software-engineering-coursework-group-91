import assert from "node:assert/strict";
import { SessionClient } from "../helpers/sessionClient.mjs";
import { DEMO_ACCOUNTS } from "../helpers/testConfig.mjs";
import { assertContainsText, assertTitle } from "../helpers/htmlAssertions.mjs";

export function registerTaProfileTests(runner) {
  runner.suite("TA Profile & CV Tests", (suite) => {
    let client;

    suite.before(async () => {
      client = new SessionClient();
      await client.ensureServerAvailable();
    });

    suite.beforeEach(async () => {
      client = new SessionClient();
      await client.login(DEMO_ACCOUNTS.ta.username, DEMO_ACCOUNTS.ta.password);
    });

    suite.test("TA can access profile page", async () => {
      const { html } = await client.getHtml("/ta/profile");
      assertTitle(html, "TA Profile And CV | INTERNATIONAL SCHOOL TA RECRUITMENT SYSTEM");
      assertContainsText(html, "Full name");
      assertContainsText(html, "CV Upload");
    });

    suite.test("Profile page shows skills and availability", async () => {
      const { html } = await client.getHtml("/ta/profile");
      assertContainsText(html, "Skills");
      assertContainsText(html, "Availability");
    });
  });
}
