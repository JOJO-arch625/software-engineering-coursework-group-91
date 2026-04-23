import assert from "node:assert/strict";
import { assertContainsText, assertTitle } from "../helpers/htmlAssertions.mjs";
import { SessionClient, assertRedirectLocation } from "../helpers/sessionClient.mjs";
import { DEMO_ACCOUNTS } from "../helpers/testConfig.mjs";

export default function registerLoginAndRoutingTests(runner) {
  runner.suite("Frontend login and route protection", ({ before, test }) => {
    before(async () => {
      const client = new SessionClient();
      await client.ensureServerAvailable();
    });

    test("login page renders the main CTA and demo account hints", async () => {
      const client = new SessionClient();
      const { response, html } = await client.getHtml("/login");

      assert.equal(response.status, 200);
      assertTitle(html, "Login | TA Recruitment System");
      assertContainsText(html, "Open your role workspace");
      assertContainsText(html, "Demo accounts");
      assertContainsText(html, "ta.demo / TaDemo123");
    });

    test("unauthenticated TA route redirects back to /login", async () => {
      const client = new SessionClient();
      const response = await client.request("/ta/dashboard");

      assert.equal(response.status, 302);
      assertRedirectLocation(response, "/login");
    });

    test("TA login redirects to the TA dashboard and keeps the session alive", async () => {
      const client = new SessionClient();
      const loginResponse = await client.login(DEMO_ACCOUNTS.ta.username, DEMO_ACCOUNTS.ta.password);

      assert.equal(loginResponse.status, 302);
      assertRedirectLocation(loginResponse, DEMO_ACCOUNTS.ta.homePath);

      const { response, html } = await client.getHtml(DEMO_ACCOUNTS.ta.homePath);
      assert.equal(response.status, 200);
      assertContainsText(html, "TA Dashboard");
      assertContainsText(html, "Latest Notifications");
      assertContainsText(html, "Applications: 2 / 3");
    });

    test("TA users are redirected away from admin-only pages", async () => {
      const client = new SessionClient();
      await client.login(DEMO_ACCOUNTS.ta.username, DEMO_ACCOUNTS.ta.password);

      const blockedResponse = await client.request("/admin/workload");
      assert.equal(blockedResponse.status, 302);
      assertRedirectLocation(blockedResponse, DEMO_ACCOUNTS.ta.homePath);

      const { html } = await client.getHtml(DEMO_ACCOUNTS.ta.homePath);
      assertContainsText(html, "does not have permission to open that page");
    });

    test("invalid login shows a user-facing error message", async () => {
      const client = new SessionClient();
      const response = await client.login("wrong.user", "wrong.password");

      assert.equal(response.status, 302);
      assertRedirectLocation(response, "/login");

      const { html } = await client.getHtml("/login");
      assertContainsText(html, "Invalid username or password");
    });
  });
}
