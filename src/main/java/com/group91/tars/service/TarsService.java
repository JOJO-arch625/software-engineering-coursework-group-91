package com.group91.tars.service;

import com.group91.tars.model.ApplicationRecord;
import com.group91.tars.model.JobPosting;
import com.group91.tars.model.Notification;
import com.group91.tars.model.OperationResult;
import com.group91.tars.model.TAProfile;
import com.group91.tars.model.UserAccount;
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
    public static final String ROLE_TA = "TA";
    public static final String ROLE_MO = "MO";
    public static final String ROLE_ADMIN = "ADMIN";
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

    public UserAccount authenticate(String username, String password) {
        if (isBlank(username) || isBlank(password)) {
            return null;
        }

        for (UserAccount account : store.loadAccounts()) {
            if (username.trim().equalsIgnoreCase(account.getUsername())
                && password.equals(account.getPassword())) {
                return account;
            }
        }
        return null;
    }

    public String getHomePathForRole(String role) {
        if (ROLE_MO.equals(role)) {
            return "/mo/dashboard";
        }
        if (ROLE_ADMIN.equals(role)) {
            return "/admin/workload";
        }
        return "/ta/dashboard";
    }

    public TAProfile getCurrentTaProfile() {
        return getProfileById(CURRENT_TA_ID);
    }

    public TAProfile getTaProfile(String taId) {
        return getProfileById(taId);
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
        return getJobsForMo(CURRENT_MO_ID);
    }

    public List<JobPosting> getJobsForMo(String moId) {
        List<JobPosting> moJobs = new ArrayList<JobPosting>();
        for (JobPosting job : getAllJobs()) {
            if (moId.equals(job.getMoId())) {
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
        return getApplicationsForTa(CURRENT_TA_ID);
    }

    public List<ApplicationRecord> getApplicationsForTa(String taId) {
        List<ApplicationRecord> applications = new ArrayList<ApplicationRecord>();
        for (ApplicationRecord application : store.loadApplications()) {
            if (taId.equals(application.getTaId())) {
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
        return countApplicationsForTa(CURRENT_TA_ID);
    }

    public int countApplicationsForTa(String taId) {
        return getApplicationsForTa(taId).size();
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
        return getNotificationsForTa(CURRENT_TA_ID);
    }

    public List<String> getNotificationsForTa(String taId) {
        List<String> notifications = new ArrayList<String>();
        for (ApplicationRecord application : getApplicationsForTa(taId)) {
            JobPosting job = getJobById(application.getJobId());
            String title = job == null ? "job" : job.getTitle();
            notifications.add(title + " status is " + application.getStatus() + ".");
        }
        int remaining = MAX_APPLICATIONS - countApplicationsForTa(taId);
        notifications.add("You can still apply for " + Math.max(remaining, 0) + " more job(s).");
        return notifications;
    }

    public OperationResult saveCurrentTaProfile(TAProfile updatedProfile) {
        return saveTaProfile(CURRENT_TA_ID, updatedProfile);
    }

    public OperationResult saveTaProfile(String taId, TAProfile updatedProfile) {
        if (isBlank(updatedProfile.getFullName())
            || isBlank(updatedProfile.getStudentNumber())
            || isBlank(updatedProfile.getEmail())
            || isBlank(updatedProfile.getSkills())
            || isBlank(updatedProfile.getAvailability())) {
            return OperationResult.failure("Please complete all required profile fields before saving.");
        }

        List<TAProfile> profiles = store.loadProfiles();
        for (int index = 0; index < profiles.size(); index++) {
            if (taId.equals(profiles.get(index).getId())) {
                updatedProfile.setId(taId);
                if (isBlank(updatedProfile.getCvPath())) {
                    updatedProfile.setCvPath(profiles.get(index).getCvPath());
                }
                profiles.set(index, updatedProfile);
                store.saveProfiles(profiles);
                return OperationResult.success("Profile saved successfully.");
            }
        }

        updatedProfile.setId(taId);
        profiles.add(updatedProfile);
        store.saveProfiles(profiles);
        return OperationResult.success("Profile created successfully.");
    }

    public OperationResult uploadCurrentTaCv(Part part) {
        return uploadTaCv(CURRENT_TA_ID, part);
    }

    public OperationResult uploadTaCv(String taId, Part part) {
        if (part == null || part.getSize() == 0) {
            return OperationResult.failure("Please choose a CV file before uploading.");
        }

        String submittedName = extractFileName(part);
        String lowerName = submittedName.toLowerCase(Locale.ENGLISH);
        if (!(lowerName.endsWith(".pdf") || lowerName.endsWith(".doc") || lowerName.endsWith(".docx"))) {
            return OperationResult.failure("Invalid file type. Please upload a PDF, DOC, or DOCX file.");
        }

        TAProfile profile = getTaProfile(taId);
        if (profile == null) {
            return OperationResult.failure("TA profile was not found.");
        }

        String finalName = profile.getFullName().replaceAll("[^A-Za-z0-9]", "") + "_" + submittedName.replaceAll("\\s+", "_");

        try (InputStream inputStream = part.getInputStream()) {
            // Delete old CV file if it exists
            if (profile.getCvPath() != null && !profile.getCvPath().isEmpty()) {
                java.io.File oldFile = new java.io.File(profile.getCvPath());
                if (oldFile.exists()) {
                    oldFile.delete();
                }
            }
            String path = store.saveCvFile(inputStream, finalName);
            profile.setCvPath(path);
            saveTaProfile(taId, profile);
            return OperationResult.success("CV uploaded successfully. Stored locally and linked through JSON metadata.");
        } catch (IOException exception) {
            return OperationResult.failure("Unable to store the uploaded file locally.");
        }
    }

    public OperationResult submitCurrentTaApplication(String jobId, String priority, String notes, String applicantSkills, String applicantDescription) {
        return submitTaApplication(CURRENT_TA_ID, jobId, priority, notes, applicantSkills, applicantDescription);
    }

    public OperationResult submitTaApplication(String taId, String jobId, String priority, String notes, String applicantSkills, String applicantDescription) {
        JobPosting job = getJobById(jobId);
        if (job == null) {
            return OperationResult.failure("The selected job posting does not exist.");
        }
        if (!"Open".equals(job.getStatus())) {
            return OperationResult.failure("This posting is closed and cannot accept new applications.");
        }
        if (countApplicationsForTa(taId) >= MAX_APPLICATIONS) {
            return OperationResult.failure("Application blocked. A TA can apply for at most three jobs.");
        }
        for (ApplicationRecord application : getApplicationsForTa(taId)) {
            if (jobId.equals(application.getJobId())) {
                return OperationResult.failure("You have already applied for this job.");
            }
        }

        ApplicationRecord application = new ApplicationRecord();
        application.setId("app-" + UUID.randomUUID().toString().substring(0, 8));
        application.setJobId(jobId);
        application.setTaId(taId);
        application.setPriority(isBlank(priority) ? "Priority 3" : priority);
        application.setStatus("Submitted");
        application.setNotes(isBlank(notes) ? "TA application submitted." : notes.trim());
        application.setApplicantSkills(isBlank(applicantSkills) ? "" : applicantSkills.trim());
        application.setApplicantDescription(isBlank(applicantDescription) ? "" : applicantDescription.trim());
        application.setSubmittedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).format(new Date()));

        List<ApplicationRecord> applications = store.loadApplications();
        applications.add(application);
        store.saveApplications(applications);

        TAProfile taProfile = getProfileById(taId);
        String taName = taProfile == null ? taId : taProfile.getFullName();
        addNotification(job.getMoId(), "review",
            "New applicant for " + job.getTitle() + ": " + taName + ".",
            "/mo/review?jobId=" + jobId);

        return OperationResult.success("Application submitted. Status is now Submitted.");
    }

    public OperationResult saveJobPosting(JobPosting draft) {
        return saveJobPosting(CURRENT_MO_ID, draft);
    }

    public OperationResult saveJobPosting(String moId, JobPosting draft) {
        if (isBlank(draft.getModuleCode()) || isBlank(draft.getTitle()) || isBlank(draft.getSkills())
            || isBlank(draft.getRequirements()) || isBlank(draft.getWorkload()) || isBlank(draft.getDeadline())) {
            return OperationResult.failure("Please complete all required job posting fields.");
        }

        List<JobPosting> jobs = store.loadJobs();
        boolean updated = false;
        for (int index = 0; index < jobs.size(); index++) {
            if (draft.getId() != null && draft.getId().equals(jobs.get(index).getId())) {
                draft.setMoId(moId);
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
            draft.setMoId(moId);
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

                JobPosting job = getJobById(application.getJobId());
                String jobTitle = job == null ? "a job" : job.getTitle();
                addNotification(application.getTaId(), "status",
                    "Your application for " + jobTitle + " has been " + status + ".",
                    "/ta/applications");

                if ("Accepted".equals(status) && countAcceptedJobsForTa(application.getTaId()) >= MAX_ACCEPTED_JOBS) {
                    addNotification(application.getTaId(), "overload",
                        "You have reached the workload cap of " + MAX_ACCEPTED_JOBS + " accepted jobs.",
                        "/ta/applications");
                }

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
        todoNotes.add("AI skill-fit scoring is now available on job detail and review pages.");
        todoNotes.add("Missing-skill suggestions are shown before TA submission.");
        todoNotes.add("Workload balancing advice is available on Admin and MO dashboards.");
        return todoNotes;
    }

    public List<JobPosting> searchJobs(String query) {
        if (isBlank(query)) {
            return getAllJobs();
        }
        String lowerQuery = query.toLowerCase(Locale.ENGLISH);
        List<JobPosting> results = new ArrayList<JobPosting>();
        for (JobPosting job : getAllJobs()) {
            if (matchesSearch(job.getModuleCode(), lowerQuery)
                || matchesSearch(job.getTitle(), lowerQuery)
                || matchesSearch(job.getSkills(), lowerQuery)
                || matchesSearch(job.getDescription(), lowerQuery)
                || matchesSearch(job.getRequirements(), lowerQuery)
                || matchesSearch(job.getStatus(), lowerQuery)) {
                results.add(job);
            }
        }
        return results;
    }

    public List<ApplicationRecord> searchApplications(String query) {
        if (isBlank(query)) {
            return store.loadApplications();
        }
        String lowerQuery = query.toLowerCase(Locale.ENGLISH);
        List<ApplicationRecord> results = new ArrayList<ApplicationRecord>();
        for (ApplicationRecord app : store.loadApplications()) {
            JobPosting job = getJobById(app.getJobId());
            TAProfile profile = getProfileById(app.getTaId());
            if (matchesSearch(app.getStatus(), lowerQuery)
                || matchesSearch(app.getPriority(), lowerQuery)
                || matchesSearch(app.getNotes(), lowerQuery)
                || (job != null && (matchesSearch(job.getModuleCode(), lowerQuery) || matchesSearch(job.getTitle(), lowerQuery)))
                || (profile != null && matchesSearch(profile.getFullName(), lowerQuery))) {
                results.add(app);
            }
        }
        return results;
    }

    public List<TAProfile> searchProfiles(String query) {
        if (isBlank(query)) {
            return store.loadProfiles();
        }
        String lowerQuery = query.toLowerCase(Locale.ENGLISH);
        List<TAProfile> results = new ArrayList<TAProfile>();
        for (TAProfile profile : store.loadProfiles()) {
            if (matchesSearch(profile.getFullName(), lowerQuery)
                || matchesSearch(profile.getStudentNumber(), lowerQuery)
                || matchesSearch(profile.getEmail(), lowerQuery)
                || matchesSearch(profile.getSkills(), lowerQuery)
                || matchesSearch(profile.getAvailability(), lowerQuery)) {
                results.add(profile);
            }
        }
        return results;
    }

    private boolean matchesSearch(String text, String lowerQuery) {
        return text != null && text.toLowerCase(Locale.ENGLISH).contains(lowerQuery);
    }

    public List<Notification> getNotificationsForUser(String userId) {
        List<Notification> userNotifications = new ArrayList<Notification>();
        for (Notification notification : store.loadNotifications()) {
            if (userId.equals(notification.getUserId())) {
                userNotifications.add(notification);
            }
        }
        Collections.sort(userNotifications, new Comparator<Notification>() {
            public int compare(Notification left, Notification right) {
                return right.getCreatedAt().compareToIgnoreCase(left.getCreatedAt());
            }
        });
        return userNotifications;
    }

    public int countUnreadNotifications(String userId) {
        int count = 0;
        for (Notification notification : store.loadNotifications()) {
            if (userId.equals(notification.getUserId()) && !notification.isRead()) {
                count++;
            }
        }
        return count;
    }

    public OperationResult markNotificationRead(String notificationId) {
        List<Notification> notifications = store.loadNotifications();
        for (Notification notification : notifications) {
            if (notification.getId().equals(notificationId)) {
                notification.setRead(true);
                store.saveNotifications(notifications);
                return OperationResult.success("Notification marked as read.");
            }
        }
        return OperationResult.failure("Notification not found.");
    }

    public OperationResult markAllNotificationsRead(String userId) {
        List<Notification> notifications = store.loadNotifications();
        boolean changed = false;
        for (Notification notification : notifications) {
            if (userId.equals(notification.getUserId()) && !notification.isRead()) {
                notification.setRead(true);
                changed = true;
            }
        }
        if (changed) {
            store.saveNotifications(notifications);
        }
        return OperationResult.success("All notifications marked as read.");
    }

    public void addNotification(String userId, String category, String message, String link) {
        List<Notification> notifications = store.loadNotifications();
        notifications.add(Notification.create(userId, category, message, link));
        store.saveNotifications(notifications);
    }

    public int calculateFitScore(String taId, String jobId) {
        TAProfile profile = getProfileById(taId);
        JobPosting job = getJobById(jobId);
        if (profile == null || job == null) {
            return 0;
        }
        if (isBlank(profile.getSkills()) || isBlank(job.getSkills())) {
            return 0;
        }
        String[] taSkills = profile.getSkills().toLowerCase(Locale.ENGLISH).split("[,;\\s]+");
        String[] jobSkills = job.getSkills().toLowerCase(Locale.ENGLISH).split("[,;\\s]+");
        int matched = 0;
        int total = jobSkills.length;
        if (total == 0) {
            return 0;
        }
        for (String jobSkill : jobSkills) {
            if (isBlank(jobSkill)) {
                continue;
            }
            for (String taSkill : taSkills) {
                if (jobSkill.equals(taSkill) || taSkill.contains(jobSkill) || jobSkill.contains(taSkill)) {
                    matched++;
                    break;
                }
            }
        }
        return (int) Math.round((matched * 100.0) / total);
    }

    public List<String> getMissingSkills(String taId, String jobId) {
        TAProfile profile = getProfileById(taId);
        JobPosting job = getJobById(jobId);
        List<String> missing = new ArrayList<String>();
        if (profile == null || job == null || isBlank(profile.getSkills()) || isBlank(job.getSkills())) {
            return missing;
        }
        String[] taSkills = profile.getSkills().toLowerCase(Locale.ENGLISH).split("[,;\\s]+");
        String[] jobSkills = job.getSkills().toLowerCase(Locale.ENGLISH).split("[,;\\s]+");
        for (String jobSkill : jobSkills) {
            if (isBlank(jobSkill)) {
                continue;
            }
            boolean found = false;
            for (String taSkill : taSkills) {
                if (jobSkill.equals(taSkill) || taSkill.contains(jobSkill) || jobSkill.contains(taSkill)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                missing.add(jobSkill);
            }
        }
        return missing;
    }

    public List<String> getWorkloadBalancingAdvice() {
        List<String> advice = new ArrayList<String>();
        List<WorkloadSummary> summaries = getWorkloadSummaries();
        List<WorkloadSummary> overloaded = new ArrayList<WorkloadSummary>();
        List<WorkloadSummary> available = new ArrayList<WorkloadSummary>();
        for (WorkloadSummary summary : summaries) {
            if (summary.isOverloadFlag()) {
                overloaded.add(summary);
            } else if (summary.getAcceptedCount() < MAX_ACCEPTED_JOBS) {
                available.add(summary);
            }
        }
        if (overloaded.isEmpty()) {
            advice.add("No workload balancing issues detected. All TAs are within acceptable limits.");
            return advice;
        }
        for (WorkloadSummary over : overloaded) {
            advice.add(over.getTaName() + " is at the workload cap (" + over.getAcceptedCount() + "/" + MAX_ACCEPTED_JOBS + ") with modules: " + String.join(", ", over.getAcceptedModules()) + ".");
            if (!available.isEmpty()) {
                List<String> suggestions = new ArrayList<String>();
                for (WorkloadSummary avail : available) {
                    suggestions.add(avail.getTaName() + " (" + avail.getAcceptedCount() + "/" + MAX_ACCEPTED_JOBS + ")");
                }
                advice.add("Consider redistributing future assignments to: " + String.join(", ", suggestions) + ".");
            } else {
                advice.add("No TAs currently have capacity for redistribution. Consider hiring additional TAs.");
            }
        }
        return advice;
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
        String submittedName;
        try {
            submittedName = part.getSubmittedFileName();
        } catch (NoSuchMethodError error) {
            submittedName = extractFileNameFromHeaders(part);
        }
        if (isBlank(submittedName)) {
            submittedName = extractFileNameFromHeaders(part);
        }
        return isBlank(submittedName) ? "uploaded_cv.pdf" : submittedName;
    }

    private String extractFileNameFromHeaders(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        if (isBlank(contentDisposition)) {
            return null;
        }
        String[] segments = contentDisposition.split(";");
        for (String segment : segments) {
            String trimmed = segment.trim();
            if (trimmed.startsWith("filename=")) {
                String fileName = trimmed.substring("filename=".length()).trim().replace("\"", "");
                int separatorIndex = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
                return separatorIndex >= 0 ? fileName.substring(separatorIndex + 1) : fileName;
            }
        }
        return null;
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
