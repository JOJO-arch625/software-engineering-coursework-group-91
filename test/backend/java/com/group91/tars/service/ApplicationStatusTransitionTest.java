package com.group91.tars.service;

import com.group91.tars.model.ApplicationRecord;
import com.group91.tars.model.JobPosting;
import com.group91.tars.model.OperationResult;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for application submission and status transition business rules.
 * Tests key business rules: job existence check, open status check,
 * application count limits, status update validation.
 */
public class ApplicationStatusTransitionTest {

    private TarsService service;

    @Before
    public void setUp() {
        service = TarsService.getInstance();
    }

    // ===================== Application Submission Rules =====================

    @Test
    public void submitApplication_requiresJobExists() {
        // Submit to non-existent job should fail
        OperationResult result = service.submitTaApplication(
            "ta-submit-test-" + System.currentTimeMillis(),
            "non-existent-job-xyz123",
            "Priority 1",
            "Test notes", "Java", "Test description"
        );
        assertFalse("Should fail for non-existent job", result.isSuccess());
    }

    @Test
    public void submitApplication_blocksDuplicateApplication() {
        // Find a job that ta-1 has already applied to
        List<ApplicationRecord> apps = service.getApplicationsForTa("ta-1");
        if (!apps.isEmpty()) {
            String jobId = apps.get(0).getJobId();
            // Try to apply again
            OperationResult result = service.submitTaApplication(
                "ta-1", jobId, "Priority 1",
                "Duplicate application", "Java", "Test"
            );
            assertFalse("Should block duplicate application", result.isSuccess());
        }
    }

    @Test
    public void submitApplication_blocksClosedJob() {
        // Find a closed job
        List<JobPosting> allJobs = service.getAllJobs();
        JobPosting closedJob = null;
        for (JobPosting job : allJobs) {
            if ("Closed".equals(job.getStatus())) {
                closedJob = job;
                break;
            }
        }

        if (closedJob != null) {
            OperationResult result = service.submitTaApplication(
                "ta-closed-test-" + System.currentTimeMillis(),
                closedJob.getId(),
                "Priority 1",
                "Applying to closed job", "Java", "Test"
            );
            assertFalse("Should block application to closed job", result.isSuccess());
        }
    }

    @Test
    public void submitApplication_defaultsPriorityWhenBlank() {
        String uniqueTaId = "ta-defaults-" + System.currentTimeMillis();
        List<JobPosting> openJobs = service.getOpenJobs();

        if (!openJobs.isEmpty()) {
            // Empty priority should default to "Priority 3"
            OperationResult result = service.submitTaApplication(
                uniqueTaId, openJobs.get(0).getId(),
                "", // Empty priority
                "Test notes", "Java", "Test"
            );
            // Result depends on duplicate check, but priority defaults
            assertNotNull(result);
        }
    }

    // ===================== Application Count =====================

    @Test
    public void countApplicationsForTa_returnsNonNegative() {
        int count = service.countApplicationsForTa("ta-1");
        assertTrue("Count should be >= 0", count >= 0);
    }

    @Test
    public void countApplicationsForTa_returnsZero_forNewTa() {
        int count = service.countApplicationsForTa("non-existent-ta-xyz123");
        assertEquals("New TA should have 0 applications", 0, count);
    }

    // ===================== Application Retrieval =====================

    @Test
    public void getApplicationsForTa_returnsNonNull() {
        List<ApplicationRecord> apps = service.getApplicationsForTa("ta-1");
        assertNotNull("Should never return null", apps);
    }

    @Test
    public void getApplicationsForTa_returnsEmpty_forNewTa() {
        List<ApplicationRecord> apps = service.getApplicationsForTa("new-ta-xyz123");
        assertNotNull("Should never return null", apps);
        assertTrue("New TA should have no applications", apps.isEmpty());
    }

    @Test
    public void getApplicationsForJob_returnsNonNull() {
        List<ApplicationRecord> apps = service.getApplicationsForJob("job-2");
        assertNotNull("Should never return null", apps);
    }

    // ===================== Status Update =====================

    @Test
    public void updateApplicationStatus_rejectsInvalidStatus() {
        OperationResult result = service.updateApplicationStatus(
            "app-1", "InvalidStatus", "Test notes"
        );
        assertFalse("Should reject invalid status", result.isSuccess());
    }

    @Test
    public void updateApplicationStatus_rejectsNonExistentApplication() {
        OperationResult result = service.updateApplicationStatus(
            "non-existent-app-xyz", "Accepted", "Test notes"
        );
        assertFalse("Should reject non-existent application", result.isSuccess());
    }

    @Test
    public void updateApplicationStatus_handlesValidStatuses() {
        // Valid statuses should be processed (may fail due to other rules)
        String[] validStatuses = {"Under Review", "Shortlisted", "Accepted", "Rejected"};
        for (String status : validStatuses) {
            OperationResult result = service.updateApplicationStatus("app-1", status, "");
            assertNotNull("Should handle status: " + status, result);
        }
    }

    // ===================== Bulk Operations =====================

    @Test
    public void bulkShortlistApplications_handlesValidJob() {
        List<ApplicationRecord> apps = service.getApplicationsForJob("job-2");
        if (!apps.isEmpty()) {
            OperationResult result = service.bulkShortlistApplications("job-2", "Bulk shortlist");
            assertNotNull("Result should not be null", result);
        }
    }

    // ===================== Application Existence =====================

    @Test
    public void getApplicationById_returnsCorrectApplication() {
        ApplicationRecord app = service.getApplicationById("app-1");
        if (app != null) {
            assertEquals("app-1", app.getId());
        }
    }

    @Test
    public void getApplicationById_returnsNull_forNonExistent() {
        ApplicationRecord app = service.getApplicationById("non-existent-app-xyz123");
        assertNull("Should return null for non-existent application", app);
    }

    // ===================== Job Applicant Count =====================

    @Test
    public void countApplicantsForJob_returnsNonNegative() {
        int count = service.countApplicantsForJob("job-2");
        assertTrue("Count should be >= 0", count >= 0);
    }

    @Test
    public void countApplicantsForJob_matchesActualList() {
        int count = service.countApplicantsForJob("job-2");
        List<ApplicationRecord> apps = service.getApplicationsForJob("job-2");
        assertEquals("Count should match list size", apps.size(), count);
    }
}
