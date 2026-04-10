package com.group91.tars.tests;

import com.group91.tars.model.ApplicationRecord;
import com.group91.tars.model.JobPosting;
import com.group91.tars.model.OperationResult;
import com.group91.tars.model.TAProfile;
import com.group91.tars.model.UserAccount;
import com.group91.tars.model.WorkloadSummary;

public class BackendModelTestMain {
    private int passed;
    private int failed;

    public static void main(String[] args) {
        new BackendModelTestMain().run();
    }

    private void run() {
        runTest("UserAccount.getInitials 支持双词姓名", this::testUserAccountInitialsWithFullName);
        runTest("UserAccount.getInitials 支持单词姓名", this::testUserAccountInitialsWithSingleName);
        runTest("UserAccount.getInitials 支持缺省 displayName 时回退到角色", this::testUserAccountInitialsFallbackToRole);
        runTest("OperationResult 工厂方法返回正确状态", this::testOperationResultFactories);
        runTest("WorkloadSummary 默认列表可用且可累计模块", this::testWorkloadSummaryDefaults);
        runTest("TAProfile 字段读写保持一致", this::testTaProfileRoundTrip);
        runTest("JobPosting 字段读写保持一致", this::testJobPostingRoundTrip);
        runTest("ApplicationRecord 字段读写保持一致", this::testApplicationRecordRoundTrip);

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

        assertEquals("PA", account.getInitials(), "双词姓名应提取首尾字母。");
    }

    private void testUserAccountInitialsWithSingleName() {
        UserAccount account = new UserAccount();
        account.setDisplayName("Admin");
        account.setRole("ADMIN");

        assertEquals("A", account.getInitials(), "单词姓名应提取首字母。");
    }

    private void testUserAccountInitialsFallbackToRole() {
        UserAccount account = new UserAccount();
        account.setRole("TA");

        assertEquals("T", account.getInitials(), "缺少 displayName 时应回退到角色首字母。");
    }

    private void testOperationResultFactories() {
        OperationResult success = OperationResult.success("saved");
        OperationResult failure = OperationResult.failure("blocked");

        assertTrue(success.isSuccess(), "success 工厂方法应返回成功状态。");
        assertEquals("saved", success.getMessage(), "success 消息应正确保留。");
        assertTrue(!failure.isSuccess(), "failure 工厂方法应返回失败状态。");
        assertEquals("blocked", failure.getMessage(), "failure 消息应正确保留。");
    }

    private void testWorkloadSummaryDefaults() {
        WorkloadSummary summary = new WorkloadSummary();
        summary.setTaId("ta-3");
        summary.setTaName("Siyu Chen");
        summary.getAcceptedModules().add("ECS5001");
        summary.getAcceptedModules().add("EIE2105");
        summary.setAcceptedCount(summary.getAcceptedModules().size());
        summary.setOverloadFlag(summary.getAcceptedCount() >= 3);

        assertTrue(summary.getAcceptedModules() != null, "acceptedModules 默认列表不应为 null。");
        assertEquals(2, summary.getAcceptedCount(), "acceptedCount 应与模块数量一致。");
        assertTrue(!summary.isOverloadFlag(), "2 门课不应触发 overload。");
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

        assertEquals("ta-1", profile.getId(), "TAProfile id 应正确保存。");
        assertEquals("Yuyanchen Long", profile.getFullName(), "TAProfile fullName 应正确保存。");
        assertEquals("uploads/cv/demo.pdf", profile.getCvPath(), "TAProfile cvPath 应正确保存。");
    }

    private void testJobPostingRoundTrip() {
        JobPosting posting = new JobPosting();
        posting.setId("job-1");
        posting.setModuleCode("EIE3320");
        posting.setTitle("Object-Oriented Programming TA");
        posting.setStatus("Open");

        assertEquals("job-1", posting.getId(), "JobPosting id 应正确保存。");
        assertEquals("EIE3320", posting.getModuleCode(), "JobPosting moduleCode 应正确保存。");
        assertEquals("Open", posting.getStatus(), "JobPosting status 应正确保存。");
    }

    private void testApplicationRecordRoundTrip() {
        ApplicationRecord record = new ApplicationRecord();
        record.setId("app-1");
        record.setJobId("job-2");
        record.setTaId("ta-1");
        record.setPriority("Priority 1");
        record.setStatus("Under Review");
        record.setNotes("MO reviewing VHDL experience.");
        record.setSubmittedAt("2026-03-24 10:00");

        assertEquals("app-1", record.getId(), "ApplicationRecord id 应正确保存。");
        assertEquals("job-2", record.getJobId(), "ApplicationRecord jobId 应正确保存。");
        assertEquals("Under Review", record.getStatus(), "ApplicationRecord status 应正确保存。");
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
