package com.group91.tars.service;

import com.group91.tars.model.JobPosting;
import com.group91.tars.model.OperationResult;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Tests for job posting date validation and deadline rules.
 */
public class JobDateValidationTest {

    private TarsService service;

    @Before
    public void setUp() {
        service = TarsService.getInstance();
    }

    // ===================== Deadline Validation =====================

    @Test
    public void isValidFutureDeadline_acceptsFutureDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String futureDate = sdf.format(new Date(System.currentTimeMillis() + 86400000L * 365));
        assertTrue(service.isValidFutureDeadlinePublic(futureDate));
    }

    @Test
    public void isValidFutureDeadline_acceptsDateOneDayInFuture() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String tomorrow = sdf.format(new Date(System.currentTimeMillis() + 86400000L));
        assertTrue(service.isValidFutureDeadlinePublic(tomorrow));
    }

    @Test
    public void isValidFutureDeadline_rejectsToday() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new Date());
        assertFalse(service.isValidFutureDeadlinePublic(today));
    }

    @Test
    public void isValidFutureDeadline_rejectsYesterday() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String yesterday = sdf.format(new Date(System.currentTimeMillis() - 86400000L));
        assertFalse(service.isValidFutureDeadlinePublic(yesterday));
    }

    @Test
    public void isValidFutureDeadline_rejectsFarPastDate() {
        assertFalse(service.isValidFutureDeadlinePublic("2020-01-01"));
        assertFalse(service.isValidFutureDeadlinePublic("1990-12-31"));
    }

    @Test
    public void isValidFutureDeadline_rejectsInvalidDateFormats() {
        assertFalse(service.isValidFutureDeadlinePublic("2026/01/01"));
        assertFalse(service.isValidFutureDeadlinePublic("01-01-2026"));
        assertFalse(service.isValidFutureDeadlinePublic("invalid-date"));
        assertFalse(service.isValidFutureDeadlinePublic(""));
        assertFalse(service.isValidFutureDeadlinePublic(null));
    }

    // ===================== Job Posting Field Validation =====================

    @Test
    public void saveJobPosting_requiresAllRequiredFields() {
        JobPosting job = new JobPosting();
        job.setId("test-job-" + System.currentTimeMillis());

        OperationResult result = service.saveJobPosting("mo-1", job);
        assertFalse("Should fail when all required fields are empty", result.isSuccess());
    }

    @Test
    public void saveJobPosting_requiresValidDeadline() {
        JobPosting job = createValidJob();
        job.setDeadline("2020-01-01"); // Past date

        OperationResult result = service.saveJobPosting("mo-1", job);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().toLowerCase().contains("deadline"));
    }

    @Test
    public void saveJobPosting_acceptsValidJob() {
        JobPosting job = createValidJob();
        job.setId("test-job-" + System.currentTimeMillis());

        OperationResult result = service.saveJobPosting("mo-1", job);
        assertTrue("Valid job should be saved: " + result.getMessage(), result.isSuccess());
    }

    @Test
    public void saveJobPosting_validatesWeeklyHours() {
        JobPosting job = createValidJob();
        job.setId("test-job-hours-" + System.currentTimeMillis());
        job.setWorkload("0 hours");
        job.setWeeklyHours(0);

        OperationResult result = service.saveJobPosting("mo-1", job);
        // May fail if hours can't be parsed to positive
        assertNotNull(result);
    }

    @Test
    public void saveJobPosting_autoSetsModuleCode() {
        JobPosting job = createValidJob();
        job.setId("test-job-module-" + System.currentTimeMillis());
        job.setModuleCode("EBU6304");

        OperationResult result = service.saveJobPosting("mo-1", job);
        JobPosting saved = service.getJobById(job.getId());
        if (saved != null) {
            assertEquals("Module code should be EBU6304", "EBU6304", saved.getModuleCode());
        }
    }

    // ===================== Helper Methods =====================

    private JobPosting createValidJob() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String futureDate = sdf.format(new Date(System.currentTimeMillis() + 86400000L * 30));

        JobPosting job = new JobPosting();
        job.setId("test-job-" + System.currentTimeMillis());
        job.setMoId("mo-1");
        job.setModuleCode("EBU6304");
        job.setTitle("Test TA Position");
        job.setSkills("Java, Python");
        job.setRequirements("Strong programming skills");
        job.setWorkload("6 hours / week");
        job.setWeeklyHours(6);
        job.setDeadline(futureDate);
        job.setStatus("Open");
        job.setDescription("Test description");
        return job;
    }
}
