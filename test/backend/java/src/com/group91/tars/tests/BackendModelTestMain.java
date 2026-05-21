package com.group91.tars.tests;

import com.group91.tars.model.ApplicationRecord;
import com.group91.tars.model.JobPosting;
import com.group91.tars.model.JobRecommendation;
import com.group91.tars.model.OperationResult;
import com.group91.tars.model.TAProfile;
import com.group91.tars.model.UserAccount;
import com.group91.tars.model.WorkloadSummary;

import java.util.ArrayList;
import java.util.List;

public class BackendModelTestMain {
    private int passed;
    private int failed;

    public static void main(String[] args) {
        new BackendModelTestMain().run();
    }

    private void run() {
        runTest("UserAccount.getInitials supports two-word names", this::testUserAccountInitialsWithFullName);
        runTest("UserAccount.getInitials supports single-word names", this::testUserAccountInitialsWithSingleName);
        runTest("UserAccount.getInitials falls back to role", this::testUserAccountInitialsFallbackToRole);
        runTest("OperationResult factory methods preserve status", this::testOperationResultFactories);
        runTest("WorkloadSummary defaults and weekly hours work", this::testWorkloadSummaryDefaults);
        runTest("TAProfile fields round-trip", this::testTaProfileRoundTrip);
        runTest("JobPosting fields and weeklyHours round-trip", this::testJobPostingRoundTrip);
        runTest("ApplicationRecord notes and reviewerNotes are separate", this::testApplicationRecordRoundTrip);
        runTest("JobRecommendation match explanation fields round-trip", this::testJobRecommendationRoundTrip);

        System.out.println();
        System.out.println("Summary: " + passed + " passed, " + failed + " failed.");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private void testUserAccountInitialsWithFullName() {
        UserAccount account = new UserAccount();
        account.setDisplayName("Programme Admin");
        account.setRole("ADMIN");

        assertEquals("PA", account.getInitials(), "Two-word names should use first and last initials.");
    }

    private void testUserAccountInitialsWithSingleName() {
        UserAccount account = new UserAccount();
        account.setDisplayName("Admin");
        account.setRole("ADMIN");

        assertEquals("A", account.getInitials(), "Single-word names should use the first initial.");
    }

    private void testUserAccountInitialsFallbackToRole() {
        UserAccount account = new UserAccount();
        account.setRole("TA");

        assertEquals("T", account.getInitials(), "Missing displayName should fall back to role initial.");
    }

    private void testOperationResultFactories() {
        OperationResult success = OperationResult.success("flash.saved", "saved");
        OperationResult failure = OperationResult.failure("flash.blocked", "blocked");

        assertTrue(success.isSuccess(), "success factory should return success status.");
        assertEquals("saved", success.getMessage(), "success message should be preserved.");
        assertEquals("flash.saved", success.getMessageKey(), "success message key should be preserved.");
        assertTrue(!failure.isSuccess(), "failure factory should return failure status.");
        assertEquals("blocked", failure.getMessage(), "failure message should be preserved.");
        assertEquals("flash.blocked", failure.getMessageKey(), "failure message key should be preserved.");
    }

    private void testWorkloadSummaryDefaults() {
        WorkloadSummary summary = new WorkloadSummary();
        summary.setTaId("ta-3");
        summary.setTaName("Siyu Chen");
        summary.getAcceptedModules().add("ECS5001");
        summary.getAcceptedModules().add("EIE2105");
        summary.setAcceptedCount(summary.getAcceptedModules().size());
        summary.setTotalWeeklyHours(11);
        summary.setOverloadFlag(summary.getTotalWeeklyHours() > 10);

        assertTrue(summary.getAcceptedModules() != null, "acceptedModules should be initialized.");
        assertEquals(2, summary.getAcceptedCount(), "acceptedCount should match accepted module count.");
        assertEquals(11, summary.getTotalWeeklyHours(), "weekly hours should be stored separately from accepted count.");
        assertTrue(summary.isOverloadFlag(), "more than 10 weekly hours should trigger overload.");
    }

    private void testTaProfileRoundTrip() {
        TAProfile profile = new TAProfile();
        profile.setId("ta-1");
        profile.setFullName("Yuyanchen Long");
        profile.setStudentNumber("231224653");
        profile.setEmail("demo@example.com");
        profile.setPhone("+86 1000");
        profile.setSkills("Java, Python");
        profile.setAvailability("Friday morning");
        profile.setCvPath("uploads/cv/demo.pdf");

        assertEquals("ta-1", profile.getId(), "TAProfile id should be stored.");
        assertEquals("Yuyanchen Long", profile.getFullName(), "TAProfile fullName should be stored.");
        assertEquals("uploads/cv/demo.pdf", profile.getCvPath(), "TAProfile cvPath should be stored.");
    }

    private void testJobPostingRoundTrip() {
        JobPosting posting = new JobPosting();
        posting.setId("job-1");
        posting.setModuleCode("EIE3320");
        posting.setTitle("Object-Oriented Programming TA");
        posting.setWorkload("6 hours / week");
        posting.setWeeklyHours(6);
        posting.setStatus("Open");

        assertEquals("job-1", posting.getId(), "JobPosting id should be stored.");
        assertEquals("EIE3320", posting.getModuleCode(), "JobPosting moduleCode should be stored.");
        assertEquals(6, posting.getWeeklyHours(), "JobPosting weeklyHours should be stored.");
        assertEquals("Open", posting.getStatus(), "JobPosting status should be stored.");
    }

    private void testApplicationRecordRoundTrip() {
        ApplicationRecord record = new ApplicationRecord();
        record.setId("app-1");
        record.setJobId("job-2");
        record.setTaId("ta-1");
        record.setPriority("Priority 1");
        record.setStatus("Shortlisted");
        record.setNotes("TA motivation note.");
        record.setReviewerNotes("MO reviewer note.");
        record.setSubmittedAt("2026-03-24 10:00");

        assertEquals("app-1", record.getId(), "ApplicationRecord id should be stored.");
        assertEquals("job-2", record.getJobId(), "ApplicationRecord jobId should be stored.");
        assertEquals("Shortlisted", record.getStatus(), "ApplicationRecord should accept Shortlisted status values.");
        assertEquals("TA motivation note.", record.getNotes(), "TA motivation note should be preserved.");
        assertEquals("MO reviewer note.", record.getReviewerNotes(), "MO reviewer note should be stored separately.");
    }

    private void testJobRecommendationRoundTrip() {
        JobPosting posting = new JobPosting();
        posting.setId("job-1");
        posting.setTitle("Object-Oriented Programming TA");

        List<String> matchedSkills = new ArrayList<String>();
        matchedSkills.add("java");
        matchedSkills.add("oop");

        JobRecommendation recommendation = new JobRecommendation();
        recommendation.setJob(posting);
        recommendation.setMatchedSkills(matchedSkills);
        recommendation.setMatchedCount(2);
        recommendation.setTotalRequiredCount(3);
        recommendation.setMatchRate(67);

        assertEquals("job-1", recommendation.getJob().getId(), "recommendation should retain the job.");
        assertEquals(2, recommendation.getMatchedCount(), "matched count should be stored.");
        assertEquals(3, recommendation.getTotalRequiredCount(), "required count should be stored.");
        assertEquals(67, recommendation.getMatchRate(), "match rate should be stored.");
        assertEquals("java", recommendation.getMatchedSkills().get(0), "matched skills should be preserved.");
    }

    private void runTest(String name, ThrowingRunnable testCase) {
        try {
            testCase.run();
            passed++;
            System.out.println("[PASS] " + name);
        } catch (Throwable error) {
            failed++;
            System.out.println("[FAIL] " + name);
            error.printStackTrace(System.out);
        }
    }

    private void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + " Expected: " + expected + ", actual: " + actual);
        }
    }

    private void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private interface ThrowingRunnable {
        void run();
    }
}
