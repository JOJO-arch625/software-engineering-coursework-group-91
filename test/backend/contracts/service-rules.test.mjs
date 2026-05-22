import assert from "node:assert/strict";
import { readProjectFile } from "../../helpers/projectFiles.mjs";

export default function registerServiceRuleTests(runner) {
  runner.suite("Backend contract tests: service core rules", ({ before, test }) => {
    let source;

    before(async () => {
      source = await readProjectFile(
        "src",
        "main",
        "java",
        "com",
        "group91",
        "tars",
        "service",
        "TarsService.java"
      );
    });

    test("application and accepted job caps stay at 3", () => {
      assert.match(source, /MAX_APPLICATIONS\s*=\s*3/);
      assert.match(source, /MAX_ACCEPTED_JOBS\s*=\s*3/);
    });

    test("role home routing covers TA, MO, and Admin", () => {
      assert.match(source, /ROLE_MO/);
      assert.match(source, /ROLE_ADMIN/);
      assert.match(source, /return "\/mo\/dashboard";/);
      assert.match(source, /return "\/admin\/workload";/);
      assert.match(source, /return "\/ta\/dashboard";/);
    });

    test("application submission blocks closed jobs, duplicates, and over-cap submissions", () => {
      assert.match(source, /!"Open"\.equals\(job\.getStatus\(\)\)/);
      assert.match(source, /countApplicationsForTa\(taId\) >= MAX_APPLICATIONS/);
      assert.match(source, /You have already applied for this job\./);
    });

    test("acceptance update blocks TAs over the accepted job cap", () => {
      assert.match(source, /"Accepted"\.equals\(status\)/);
      assert.match(source, /countAcceptedJobsForTa\(application\.getTaId\(\)\) >= MAX_ACCEPTED_JOBS/);
      assert.match(source, /Acceptance would exceed the TA workload cap\./);
    });

    test("Stage 2 core rules are implemented in the service layer", () => {
      assert.match(source, /left\.getDeadline\(\)/);
      assert.match(source, /"Shortlisted"\.equals\(status\)/);
      assert.match(source, /setReviewerNotes/);
      assert.match(source, /getRecommendedJobsForTa/);
      assert.match(source, /getWorkloadSummaries\(String query\)/);
      assert.match(source, /getTotalWeeklyHours/);
      assert.match(source, /> 10/);
      assert.match(source, /Pattern\.compile\("\(\\\\d\+\)"\)/);
    });

    test("notification logic tells TAs how many applications remain", () => {
      assert.match(source, /You can still apply for /);
      assert.match(source, /MAX_APPLICATIONS - countApplicationsForTa\(taId\)/);
    });
  });
}
