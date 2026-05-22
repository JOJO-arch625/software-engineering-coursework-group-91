const assert = require("assert");

const {
  state,
  VALID_APPLICATION_STATUSES,
  getAllJobs,
  getTotalWeeklyHours,
  getRecommendedJobsForTa,
  isValidApplicationStatus,
  updateApplicationStatus,
} = require("../app.js");

function test(name, fn) {
  try {
    fn();
    console.log(`PASS ${name}`);
  } catch (error) {
    console.error(`FAIL ${name}`);
    console.error(error);
    process.exitCode = 1;
  }
}

test("S2-01 sorts jobs by earliest deadline and exposes MO names", () => {
  const jobs = getAllJobs();
  assert.deepStrictEqual(
    jobs.map((job) => job.moduleCode),
    ["EIE2105", "EIE3320", "ECS5001"]
  );
  assert.ok(jobs.every((job) => job.moName));
});

test("S2-02 supports Shortlisted as a valid application state", () => {
  assert.ok(VALID_APPLICATION_STATUSES.includes("Shortlisted"));
  assert.strictEqual(isValidApplicationStatus("Shortlisted"), true);
  const app = { id: "test-app", status: "Submitted", moduleCode: "TEST1000" };
  assert.strictEqual(updateApplicationStatus(app, "Shortlisted", "Good fit."), true);
  assert.strictEqual(app.status, "Shortlisted");
  assert.strictEqual(app.reviewerNotes, "Good fit.");
});

test("S2-03 keeps TA motivation notes separate from MO reviewer notes", () => {
  const app = {
    id: "test-notes",
    status: "Under Review",
    moduleCode: "TEST1001",
    notes: "TA motivation stays here.",
  };
  updateApplicationStatus(app, "Rejected", "MO rejection reason.");
  assert.strictEqual(app.notes, "TA motivation stays here.");
  assert.strictEqual(app.reviewerNotes, "MO rejection reason.");
});

test("S2-04 calculates admin workload by weekly hours", () => {
  const overloaded = state.adminRecords.find((record) => record.ta === "Siyu Chen");
  assert.strictEqual(getTotalWeeklyHours(overloaded), 15);
  assert.ok(getTotalWeeklyHours(overloaded) > 10);
});

test("S2-05 returns only explainable AI recommendations at or above 60%", () => {
  const recommendations = getRecommendedJobsForTa({
    skills: "Java, Python, VHDL, OOP, debugging",
  });
  assert.ok(recommendations.length > 0);
  assert.ok(recommendations.length <= 5);
  assert.ok(recommendations.every((item) => item.matchRate >= 60));
  assert.ok(recommendations.every((item) => item.explanation.includes("Matched")));
});

