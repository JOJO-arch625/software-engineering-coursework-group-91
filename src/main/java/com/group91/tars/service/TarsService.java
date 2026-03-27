package com.group91.tars.service;

import com.group91.tars.model.ApplicationRecord;
import com.group91.tars.model.JobPosting;
import com.group91.tars.model.OperationResult;
import com.group91.tars.model.TAProfile;
import com.group91.tars.model.WorkloadSummary;
import com.group91.tars.storage.JsonDataStore;

import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class TarsService {
    public static final String CURRENT_TA_ID = "ta-1";
    public static final String CURRENT_MO_ID = "mo-1";
    public static final int MAX_APPLICATIONS = 3;
    public static final int MAX_ACCEPTED_JOBS = 3;

    private static final TarsService INSTANCE = new TarsService();

    private final JsonDataStore store = JsonDataStore.getInstance();

    private TarsService() {
    }

    public static TarsService getInstance() {
        return INSTANCE;
    }

    public void initializeStorage() {
        store.initialize();
    }

    public TAProfile getCurrentTaProfile() {
        return getProfileById(CURRENT_TA_ID);
    }

    public TAProfile getProfileById(String taId) {
        for (TAProfile profile : store.loadProfiles()) {
            if (profile.getId().equals(taId)) {
                return profile;
            }
        }
        return null;
    }

    public List<JobPosting> getAllJobs() {
        List<JobPosting> jobs = store.loadJobs();
        Collections.sort(jobs, new Comparator<JobPosting>() {
            public int compare(JobPosting left, JobPosting right) {
                return left.getModuleCode().compareToIgnoreCase(right.getModuleCode());
            }
        });
        return jobs;
    }

    public List<JobPosting> getOpenJobs() {
        List<JobPosting> openJobs = new ArrayList<JobPosting>();
        for (JobPosting job : getAllJobs()) {
            if ("Open".equals(job.getStatus())) {
                openJobs.add(job);
            }
        }
        return openJobs;
    }

    public List<JobPosting> getJobsForCurrentMo() {
        List<JobPosting> moJobs = new ArrayList<JobPosting>();
        for (JobPosting job : getAllJobs()) {
            if (CURRENT_MO_ID.equals(job.getMoId())) {
                moJobs.add(job);
            }
        }
        return moJobs;
    }

    public JobPosting getJobById(String jobId) {
        for (JobPosting job : store.loadJobs()) {
            if (job.getId().equals(jobId)) {
                return job;
            }
        }
        return null;
    }

    public List<ApplicationRecord> getApplicationsForCurrentTa() {
        List<ApplicationRecord> applications = new ArrayList<ApplicationRecord>();
        for (ApplicationRecord application : store.loadApplications()) {
            if (CURRENT_TA_ID.equals(application.getTaId())) {
                applications.add(application);
            }
        }
        Collections.sort(applications, new Comparator<ApplicationRecord>() {
            public int compare(ApplicationRecord left, ApplicationRecord right) {
                return left.getSubmittedAt().compareToIgnoreCase(right.getSubmittedAt());
            }
        });
        return applications;
    }

    public List<ApplicationRecord> getApplicationsForJob(String jobId) {
        List<ApplicationRecord> matches = new ArrayList<ApplicationRecord>();
        for (ApplicationRecord application : store.loadApplications()) {
            if (jobId.equals(application.getJobId())) {
                matches.add(application);
            }
        }
        return matches;
    }

    public ApplicationRecord getApplicationById(String applicationId) {
        for (ApplicationRecord application : store.loadApplications()) {
            if (application.getId().equals(applicationId)) {
                return application;
            }
        }
        return null;
    }

    public int countCurrentTaApplications() {
        return getApplicationsForCurrentTa().size();
    }

    public int countCurrentTaAcceptedJobs() {
        return countAcceptedJobsForTa(CURRENT_TA_ID);
    }

    public int countAcceptedJobsForTaPublic(String taId) {
        return countAcceptedJobsForTa(taId);
    }

    public int countOpenJobs() {
        return getOpenJobs().size();
    }

    public int countPendingApplications() {
        int pending = 0;
        for (ApplicationRecord application : store.loadApplications()) {
            if ("Submitted".equals(application.getStatus()) || "Under Review".equals(application.getStatus())) {
                pending++;
            }
        }
        return pending;
    }

    public int countAllApplications() {
        return store.loadApplications().size();
    }

    public List<String> getCurrentTaNotifications() {
        List<String> notifications = new ArrayList<String>();
        for (ApplicationRecord application : getApplicationsForCurrentTa()) {
            JobPosting job = getJobById(application.getJobId());
            String title = job == null ? "job" : job.getTitle();
            notifications.add(title + " status is " + application.getStatus() + ".");
        }
        int remaining = MAX_APPLICATIONS - countCurrentTaApplications();
        notifications.add("You can still apply for " + Math.max(remaining, 0) + " more job(s).");
        return notifications;
    }

    public OperationResult saveCurrentTaProfile(TAProfile updatedProfile) {
        if (isBlank(updatedProfile.getFullName())
            || isBlank(updatedProfile.getStudentNumber())
            || isBlank(updatedProfile.getEmail())
            || isBlank(updatedProfile.getSkills())
            || isBlank(updatedProfile.getAvailability())) {
            return OperationResult.failure("Please complete all required profile fields before saving.");
        }

        List<TAProfile> profiles = store.loadProfiles();
        for (int index = 0; index < profiles.size(); index++) {
            if (CURRENT_TA_ID.equals(profiles.get(index).getId())) {
                updatedProfile.setId(CURRENT_TA_ID);
                if (isBlank(updatedProfile.getCvPath())) {
                    updatedProfile.setCvPath(profiles.get(index).getCvPath());
                }
                profiles.set(index, updatedProfile);
                store.saveProfiles(profiles);
                return OperationResult.success("Profile saved successfully.");
            }
        }

        updatedProfile.setId(CURRENT_TA_ID);
        profiles.add(updatedProfile);
        store.saveProfiles(profiles);
        return OperationResult.success("Profile created successfully.");
    }

    public OperationResult uploadCurrentTaCv(Part part) {
        if (part == null || part.getSize() == 0) {
            return OperationResult.failure("Please choose a CV file before uploading.");
        }

        String submittedName = extractFileName(part);
        String lowerName = submittedName.toLowerCase(Locale.ENGLISH);
        if (!(lowerName.endsWith(".pdf") || lowerName.endsWith(".doc") || lowerName.endsWith(".docx"))) {
            return OperationResult.failure("Invalid file type. Please upload a PDF, DOC, or DOCX file.");
        }

        TAProfile profile = getCurrentTaProfile();
        if (profile == null) {
            return OperationResult.failure("TA profile was not found.");
        }

        String finalName = profile.getFullName().replaceAll("[^A-Za-z0-9]", "") + "_" + submittedName.replaceAll("\\s+", "_");

        try (InputStream inputStream = part.getInputStream()) {
            String path = store.saveCvFile(inputStream, finalName);
            profile.setCvPath(path);
            saveCurrentTaProfile(profile);
            return OperationResult.success("CV uploaded successfully. Stored locally and linked through JSON metadata.");
        } catch (IOException exception) {
            return OperationResult.failure("Unable to store the uploaded file locally.");
        }
    }

    public OperationResult submitCurrentTaApplication(String jobId, String priority, String notes) {
        JobPosting job = getJobById(jobId);
        if (job == null) {
            return OperationResult.failure("The selected job posting does not exist.");
        }
        if (!"Open".equals(job.getStatus())) {
            return OperationResult.failure("This posting is closed and cannot accept new applications.");
        }
        if (countCurrentTaApplications() >= MAX_APPLICATIONS) {
            return OperationResult.failure("Application blocked. A TA can apply for at most three jobs.");
        }
        for (ApplicationRecord application : getApplicationsForCurrentTa()) {
            if (jobId.equals(application.getJobId())) {
                return OperationResult.failure("You have already applied for this job.");
            }
        }

        ApplicationRecord application = new ApplicationRecord();
        application.setId("app-" + UUID.randomUUID().toString().substring(0, 8));
        application.setJobId(jobId);
        application.setTaId(CURRENT_TA_ID);
        application.setPriority(isBlank(priority) ? "Priority 3" : priority);
        application.setStatus("Submitted");
        application.setNotes(isBlank(notes) ? "TA application submitted." : notes.trim());
        application.setSubmittedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).format(new Date()));

        List<ApplicationRecord> applications = store.loadApplications();
        applications.add(application);
        store.saveApplications(applications);
        return OperationResult.success("Application submitted. Status is now Submitted.");
    }

    public OperationResult saveJobPosting(JobPosting draft) {
        if (isBlank(draft.getModuleCode()) || isBlank(draft.getTitle()) || isBlank(draft.getSkills())
            || isBlank(draft.getRequirements()) || isBlank(draft.getWorkload()) || isBlank(draft.getDeadline())) {
            return OperationResult.failure("Please complete all required job posting fields.");
        }

        List<JobPosting> jobs = store.loadJobs();
        boolean updated = false;
        for (int index = 0; index < jobs.size(); index++) {
            if (draft.getId() != null && draft.getId().equals(jobs.get(index).getId())) {
                draft.setMoId(CURRENT_MO_ID);
                if (isBlank(draft.getStatus())) {
                    draft.setStatus("Open");
                }
                jobs.set(index, draft);
                updated = true;
                break;
            }
        }

        if (!updated) {
            draft.setId("job-" + UUID.randomUUID().toString().substring(0, 8));
            draft.setMoId(CURRENT_MO_ID);
            if (isBlank(draft.getStatus())) {
                draft.setStatus("Open");
            }
            jobs.add(draft);
        }

        store.saveJobs(jobs);
        return OperationResult.success(updated ? "Job posting updated successfully." : "Job posting created successfully.");
    }

    public OperationResult closeJobPosting(String jobId) {
        List<JobPosting> jobs = store.loadJobs();
        for (JobPosting job : jobs) {
            if (job.getId().equals(jobId)) {
                job.setStatus("Closed");
                store.saveJobs(jobs);
                return OperationResult.success("Posting closed. New TA applications are now blocked.");
            }
        }
        return OperationResult.failure("Unable to find the selected job posting.");
    }

    public OperationResult updateApplicationStatus(String applicationId, String status, String notes) {
        List<ApplicationRecord> applications = store.loadApplications();
        for (ApplicationRecord application : applications) {
            if (application.getId().equals(applicationId)) {
                if ("Accepted".equals(status) && countAcceptedJobsForTa(application.getTaId()) >= MAX_ACCEPTED_JOBS
                    && !"Accepted".equals(application.getStatus())) {
                    return OperationResult.failure("Acceptance would exceed the TA workload cap.");
                }
                application.setStatus(status);
                application.setNotes(isBlank(notes) ? buildDefaultStatusNote(status) : notes.trim());
                store.saveApplications(applications);
                return OperationResult.success("Applicant status updated. TA dashboard and applications view now reflect the decision.");
            }
        }
        return OperationResult.failure("Application record not found.");
    }

    public List<WorkloadSummary> getWorkloadSummaries() {
        Map<String, WorkloadSummary> summaries = new LinkedHashMap<String, WorkloadSummary>();
        for (TAProfile profile : store.loadProfiles()) {
            WorkloadSummary summary = new WorkloadSummary();
            summary.setTaId(profile.getId());
            summary.setTaName(profile.getFullName());
            summaries.put(profile.getId(), summary);
        }

        for (ApplicationRecord application : store.loadApplications()) {
            if (!"Accepted".equals(application.getStatus())) {
                continue;
            }
            WorkloadSummary summary = summaries.get(application.getTaId());
            if (summary == null) {
                continue;
            }
            JobPosting job = getJobById(application.getJobId());
            if (job != null) {
                summary.getAcceptedModules().add(job.getModuleCode());
            }
        }

        for (WorkloadSummary summary : summaries.values()) {
            summary.setAcceptedCount(summary.getAcceptedModules().size());
            summary.setOverloadFlag(summary.getAcceptedCount() >= MAX_ACCEPTED_JOBS);
        }

        return new ArrayList<WorkloadSummary>(summaries.values());
    }

    public int countOverloadSummaries() {
        int overloads = 0;
        for (WorkloadSummary summary : getWorkloadSummaries()) {
            if (summary.isOverloadFlag()) {
                overloads++;
            }
        }
        return overloads;
    }

    public List<String> getAiTodoNotes() {
        List<String> todoNotes = new ArrayList<String>();
        todoNotes.add("TODO: Add AI skill-fit scoring as an optional module.");
        todoNotes.add("TODO: Add missing-skill suggestions before TA submission.");
        todoNotes.add("TODO: Add workload balancing advice for Admin and MO dashboards.");
        return todoNotes;
    }

    public String getJobTitle(String jobId) {
        JobPosting job = getJobById(jobId);
        return job == null ? "Unknown job" : job.getTitle();
    }

    public String getJobModuleCode(String jobId) {
        JobPosting job = getJobById(jobId);
        return job == null ? "-" : job.getModuleCode();
    }

    public int countApplicantsForJob(String jobId) {
        return getApplicationsForJob(jobId).size();
    }

    private int countAcceptedJobsForTa(String taId) {
        int count = 0;
        for (ApplicationRecord application : store.loadApplications()) {
            if (taId.equals(application.getTaId()) && "Accepted".equals(application.getStatus())) {
                count++;
            }
        }
        return count;
    }

    private String extractFileName(Part part) {
        String submittedName = part.getSubmittedFileName();
        return isBlank(submittedName) ? "uploaded_cv.pdf" : submittedName;
    }

    private String buildDefaultStatusNote(String status) {
        if ("Accepted".equals(status)) {
            return "Accepted by MO after workload check.";
        }
        if ("Rejected".equals(status)) {
            return "Rejected by MO. Status remains visible to the TA.";
        }
        if ("Under Review".equals(status)) {
            return "MO is reviewing skills and workload.";
        }
        return "Status updated by MO.";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
