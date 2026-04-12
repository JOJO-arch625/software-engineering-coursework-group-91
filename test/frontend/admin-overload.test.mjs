import assert from "node:strict";
import { SessionClient } from "../helpers/sessionClient.mjs";
import { DEMO_ACCOUNTS } from "../helpers/testConfig.mjs";
import { assertContainsText } from "../helpers/htmlAssertions.mjs";

export function registerAdminOverloadTests(runner) {
  runner.suite("Admin Workload Overload Tests", (suite) => {
    let client;

    suite.before(async () => {
      client = new SessionClient();
      await client.ensureServerAvailable();
      await client.login(DEMO_ACCOUNTS.admin.username, DEMO_ACCOUNTS.admin.password);
    });

    suite.test("Admin workload page shows correctly", async () => {
      const { html } = await client.getHtml("/admin/workload");
      assertContainsText(html, "TA Workload Summary");
    });

    suite.test("Admin page shows overload indicator when applicable", async () => {
      const { html } = await client.getHtml("/admin/workload");
      assertContainsText(html, "Overload");
    });
  });
}