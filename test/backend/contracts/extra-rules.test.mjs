import assert from "node:assert/strict";
import { readProjectJson } from "../../helpers/fileUtils.mjs";

export function registerExtraServiceRuleTests(runner) {
  runner.suite("Extra Backend Service Rules", (suite) => {
    suite.test("Seed data has no TA with more than 3 accepted jobs", async () => {
      const apps = await readProjectJson("data", "applications.json");
      const accepted = apps.filter(a => a.status === "Accepted");

      const counts = {};
      for (const a of accepted) {
        counts[a.taId] = (counts[a.taId] || 0) + 1;
      }

      for (const taId in counts) {
        assert.ok(counts[taId] <= 3, `TA ${taId} has too many accepted jobs`);
      }
    });

    suite.test("All jobs have valid status (Open/Closed)", async () => {
      const jobs = await readProjectJson("data", "job-postings.json");
      for (const j of jobs) {
        assert.ok(["Open", "Closed"].includes(j.status));
      }
    });
  });
}