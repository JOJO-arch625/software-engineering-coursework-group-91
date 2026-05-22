import assert from "node:assert/strict";
import { SessionClient } from "../helpers/sessionClient.mjs";
import { DEMO_ACCOUNTS, pageTitle } from "../helpers/testConfig.mjs";
import { assertContainsText, assertTitle } from "../helpers/htmlAssertions.mjs";

export function registerSearchInboxTests(runner) {
  runner.suite("Search and Inbox smoke tests", (suite) => {
    suite.before(async () => {
      const client = new SessionClient();
      await client.ensureServerAvailable();
    });

    suite.test("TA can open global search", async () => {
      const client = new SessionClient();
      await client.login(DEMO_ACCOUNTS.ta.username, DEMO_ACCOUNTS.ta.password);

      const { html, response } = await client.getHtml("/search");
      assert.equal(response.status, 200);
      assertTitle(html, pageTitle("Global Search"));
      assertContainsText(html, "Global Search");
      assertContainsText(html, "Search keywords");
    });

    suite.test("TA search returns matching open jobs", async () => {
      const client = new SessionClient();
      await client.login(DEMO_ACCOUNTS.ta.username, DEMO_ACCOUNTS.ta.password);

      const { html, response } = await client.getHtml("/search?q=Digital&category=jobs");
      assert.equal(response.status, 200);
      assertContainsText(html, "Digital Systems TA");
    });

    suite.test("TA can open notification inbox", async () => {
      const client = new SessionClient();
      await client.login(DEMO_ACCOUNTS.ta.username, DEMO_ACCOUNTS.ta.password);

      const { html, response } = await client.getHtml("/inbox");
      assert.equal(response.status, 200);
      assertTitle(html, pageTitle("Notifications"));
      assertContainsText(html, "Notifications");
      assertContainsText(html, "unread");
    });

    suite.test("MO can open search and inbox", async () => {
      const client = new SessionClient();
      await client.login(DEMO_ACCOUNTS.mo.username, DEMO_ACCOUNTS.mo.password);

      const searchPage = await client.getHtml("/search?q=Programming");
      assert.equal(searchPage.response.status, 200);
      assertContainsText(searchPage.html, "Global Search");
      assertContainsText(searchPage.html, "Object-Oriented Programming TA");

      const inboxPage = await client.getHtml("/inbox");
      assert.equal(inboxPage.response.status, 200);
      assertTitle(inboxPage.html, pageTitle("Notifications"));
      assertContainsText(inboxPage.html, "Notification Categories");
    });
  });
}
