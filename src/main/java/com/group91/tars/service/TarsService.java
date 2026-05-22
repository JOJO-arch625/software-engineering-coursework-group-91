package com.group91.tars.service;

import com.group91.tars.model.ApplicationRecord;
import com.group91.tars.model.JobRecommendation;
import com.group91.tars.model.JobPosting;
import com.group91.tars.model.Notification;
import com.group91.tars.model.OperationResult;
import com.group91.tars.model.TAProfile;
import com.group91.tars.model.UserAccount;
import com.group91.tars.model.WorkloadSummary;
import com.group91.tars.model.ai.AiCandidateSummary;
import com.group91.tars.model.ai.AiChatMemory;
import com.group91.tars.model.ai.AiFitResult;
import com.group91.tars.model.ai.AiWorkloadAdvice;
import com.group91.tars.service.ai.AiAgentService;
import com.group91.tars.service.ai.tool.ToolCallingResult;
import com.group91.tars.storage.JsonDataStore;

import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Central business logic singleton for the ISTARS TA Recruitment System.
 * All core operations (authentication, profile management, job posting,
 * application processing, workload monitoring, notifications, search,
 * and AI scoring) are routed through this service layer.
 */
public class TarsService {
    public static final String CURRENT_TA_ID = "ta-1";
    public static final String CURRENT_MO_ID = "mo-1";
    public static final String ROLE_TA = "TA";
    public static final String ROLE_MO = "MO";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String MO_COURSE_CODE = "EBU6304";
    public static final String MO_COURSE_TITLE = "Software Engineering";
    public static final String MO_COURSE_LABEL = "EBU6304 - Software Engineering";
    public static final int MAX_APPLICATIONS = 3;
    public static final int MAX_ACCEPTED_JOBS = 3;
    public static final long MAX_CV_FILE_SIZE = 10L * 1024L * 1024L;

    private static final TarsService INSTANCE = new TarsService();

    private final JsonDataStore store = JsonDataStore.getInstance();
    private final AiAgentService aiAgentService = AiAgentService.getInstance();

    private TarsService() {
    }

    /**
     * Returns the singleton instance of TarsService.
     *
     * @return the shared TarsService instance
     */
    public static TarsService getInstance() {
        return INSTANCE;
    }

    /**
     * Validates that the given module code is EBU6304. Used by MoJobEditServlet
     * to reject any attempt to submit a different course code.
     */
    public boolean isValidMoCourseCode(String moduleCode) {
        return MO_COURSE_CODE.equals(moduleCode);
    }

    /**
     * Initialises the underlying JSON data store, creating seed data files
     * and upload directories if they do not already exist.
     */
    public void initializeStorage() {
        store.initialize();
    }

    /**
     * Authenticates a user by matching the supplied username (case-insensitive)
     * and password (case-sensitive, exact match) against stored accounts.
     *
     * @param username the username submitted at login
     * @param password the password submitted at login
     * @return the matching UserAccount if credentials are valid, or null if authentication fails
     */
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

    /**
     * Returns the default home page path for the given user role.
     *
     * @param role the role identifier (TA, MO, or ADMIN)
     * @return the home path for that role
     */
    public String getHomePathForRole(String role) {
        if (ROLE_MO.equals(role)) {
            return "/mo/dashboard";
        }
        if (ROLE_ADMIN.equals(role)) {
            return "/admin/workload";
        }
        return "/ta/dashboard";
    }

    /**
     * Returns the TA profile for the hardcoded demo TA ID.
     *
     * @return the demo TA profile, or null if not found
     * @deprecated Use {@link #getTaProfile(String)} with the actual TA ID instead
     */
    public TAProfile getCurrentTaProfile() {
        return getProfileById(CURRENT_TA_ID);
    }

    /**
     * Retrieves a TA profile by its unique identifier.
     *
     * @param taId the TA profile identifier
     * @return the matching TAProfile, or null if no profile exists with the given ID
     */
    public TAProfile getTaProfile(String taId) {
        return getProfileById(taId);
    }

    /**
     * Looks up a TA profile by its unique identifier from the data store.
     *
     * @param taId the TA profile identifier to search for
     * @return the matching TAProfile, or null if not found
     */
    public TAProfile getProfileById(String taId) {
        for (TAProfile profile : store.loadProfiles()) {
            if (profile.getId().equals(taId)) {
                return profile;
            }
        }
        return null;
    }

    /**
     * Returns all job postings sorted by deadline ascending, with module code
     * as a stable tie breaker.
     *
     * @return a sorted list of all JobPosting records
     */
    public List<JobPosting> getAllJobs() {
        List<JobPosting> jobs = store.loadJobs();
        Collections.sort(jobs, new Comparator<JobPosting>() {
            public int compare(JobPosting left, JobPosting right) {
                int deadlineCompare = safeText(left.getDeadline()).compareToIgnoreCase(safeText(right.getDeadline()));
                if (deadlineCompare != 0) {
                    return deadlineCompare;
                }
                return safeText(left.getModuleCode()).compareToIgnoreCase(safeText(right.getModuleCode()));
            }
        });
        return jobs;
    }

    public String getMoDisplayName(String moId) {
        if (isBlank(moId)) {
            return "-";
        }
        for (UserAccount account : store.loadAccounts()) {
            if (moId.equals(account.getLinkedId()) || moId.equals(account.getId())) {
                return account.getDisplayName();
            }
        }
        return moId;
    }

    /**
     * Returns all job postings with "Open" status. Before returning, this method
     * automatically closes any "Open" postings whose deadline has passed, ensuring
     * expired jobs are never shown to TAs.
     *
     * @return a list of JobPosting records that are currently open and not expired
     */
    public List<JobPosting> getOpenJobs() {
        autoCloseExpiredJobs();

        List<JobPosting> openJobs = new ArrayList<JobPosting>();
        for (JobPosting job : getAllJobs()) {
            if ("Open".equals(job.getStatus())) {
                openJobs.add(job);
            }
        }
        return openJobs;
    }

    /**
     * Returns job postings belonging to the hardcoded demo MO ID.
     *
     * @return a list of JobPosting records owned by the demo MO
     * @deprecated Use {@link #getJobsForMo(String)} with the actual MO ID instead
     */
    public List<JobPosting> getJobsForCurrentMo() {
        return getJobsForMo(CURRENT_MO_ID);
    }

    /**
     * Returns all job postings for a specific Module Organiser.
     * MO users are restricted to only EBU6304 - Software Engineering jobs,
     * regardless of their moId. Other roles see all jobs matching the moId.
     *
     * @param moId the MO identifier to filter by
     * @return a list of JobPosting records owned by the specified MO and course
     */
    public List<JobPosting> getJobsForMo(String moId) {
        List<JobPosting> moJobs = new ArrayList<JobPosting>();
        for (JobPosting job : getAllJobs()) {
            boolean isMoOwned = moId.equals(job.getMoId());
            boolean isEbU6304 = MO_COURSE_CODE.equals(job.getModuleCode());
            if (isMoOwned && isEbU6304) {
                moJobs.add(job);
            }
        }
        return moJobs;
    }

    /**
     * Retrieves a job posting by its unique identifier.
     *
     * @param jobId the job posting identifier
     * @return the matching JobPosting, or null if not found
     */
    public JobPosting getJobById(String jobId) {
        for (JobPosting job : store.loadJobs()) {
            if (job.getId().equals(jobId)) {
                return job;
            }
        }
        return null;
    }

    /**
     * Returns applications for the hardcoded demo TA ID.
     *
     * @return a sorted list of ApplicationRecord records for the demo TA
     * @deprecated Use {@link #getApplicationsForTa(String)} with the actual TA ID instead
     */
    public List<ApplicationRecord> getApplicationsForCurrentTa() {
        return getApplicationsForTa(CURRENT_TA_ID);
    }

    /**
     * Returns all applications submitted by a specific TA, sorted by submission time.
     *
     * @param taId the TA identifier to filter by
     * @return a chronologically sorted list of ApplicationRecord records for the specified TA
     */
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

    /**
     * Returns all applications submitted for a specific job posting.
     *
     * @param jobId the job posting identifier to filter by
     * @return a list of ApplicationRecord records for the specified job
     */
    public List<ApplicationRecord> getApplicationsForJob(String jobId) {
        List<ApplicationRecord> matches = new ArrayList<ApplicationRecord>();
        for (ApplicationRecord application : store.loadApplications()) {
            if (jobId.equals(application.getJobId())) {
                matches.add(application);
            }
        }
        return matches;
    }

    /**
     * Retrieves an application record by its unique identifier.
     *
     * @param applicationId the application record identifier
     * @return the matching ApplicationRecord, or null if not found
     */
    public ApplicationRecord getApplicationById(String applicationId) {
        for (ApplicationRecord application : store.loadApplications()) {
            if (application.getId().equals(applicationId)) {
                return application;
            }
        }
        return null;
    }

    /**
     * Counts applications for the hardcoded demo TA ID.
     *
     * @return the number of applications submitted by the demo TA
     * @deprecated Use {@link #countApplicationsForTa(String)} with the actual TA ID instead
     */
    public int countCurrentTaApplications() {
        return countApplicationsForTa(CURRENT_TA_ID);
    }

    /**
     * Counts the total number of applications submitted by a specific TA.
     *
     * @param taId the TA identifier
     * @return the number of applications for the specified TA
     */
    public int countApplicationsForTa(String taId) {
        return getApplicationsForTa(taId).size();
    }

    /**
     * Counts accepted jobs for the hardcoded demo TA ID.
     *
     * @return the number of accepted jobs for the demo TA
     * @deprecated Use {@link #countAcceptedJobsForTaPublic(String)} with the actual TA ID instead
     */
    public int countCurrentTaAcceptedJobs() {
        return countAcceptedJobsForTa(CURRENT_TA_ID);
    }

    /**
     * Returns the number of accepted jobs for a specific TA, suitable for public display.
     *
     * @param taId the TA identifier
     * @return the number of accepted jobs for the specified TA
     */
    public int countAcceptedJobsForTaPublic(String taId) {
        return countAcceptedJobsForTa(taId);
    }

    /**
     * Counts the number of currently open job postings.
     *
     * @return the count of open jobs
     */
    public int countOpenJobs() {
        return getOpenJobs().size();
    }

    /**
     * Counts the number of open jobs for a specific MO.
     * Only counts jobs where moId matches and moduleCode is EBU6304.
     *
     * @param moId the MO identifier
     * @return the count of open EBU6304 jobs for this MO
     */
    public int countOpenJobsForMo(String moId) {
        int count = 0;
        for (JobPosting job : getJobsForMo(moId)) {
            if ("Open".equals(job.getStatus())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Counts the number of applications in "Submitted" or "Under Review" status
     * for jobs belonging to a specific MO (EBU6304 only).
     *
     * @param moId the MO identifier
     * @return the count of pending applications for this MO
     */
    public int countPendingApplicationsForMo(String moId) {
        int pending = 0;
        Set<String> moJobIds = new LinkedHashSet<String>();
        for (JobPosting job : getJobsForMo(moId)) {
            moJobIds.add(job.getId());
        }
        for (ApplicationRecord application : store.loadApplications()) {
            if (moJobIds.contains(application.getJobId())) {
                if ("Submitted".equals(application.getStatus()) || "Under Review".equals(application.getStatus())
                    || "Shortlisted".equals(application.getStatus())) {
                    pending++;
                }
            }
        }
        return pending;
    }

    /**
     * Counts the total number of applications for jobs belonging to
     * a specific MO (EBU6304 only).
     *
     * @param moId the MO identifier
     * @return the total application count for this MO
     */
    public int countApplicationsForMo(String moId) {
        Set<String> moJobIds = new LinkedHashSet<String>();
        for (JobPosting job : getJobsForMo(moId)) {
            moJobIds.add(job.getId());
        }
        int count = 0;
        for (ApplicationRecord application : store.loadApplications()) {
            if (moJobIds.contains(application.getJobId())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Counts the number of applications in "Submitted" or "Under Review" status.
     *
     * @return the count of pending applications
     */
    public int countPendingApplications() {
        int pending = 0;
        for (ApplicationRecord application : store.loadApplications()) {
            if ("Submitted".equals(application.getStatus()) || "Under Review".equals(application.getStatus())
                || "Shortlisted".equals(application.getStatus())) {
                pending++;
            }
        }
        return pending;
    }

    /**
     * Counts the total number of applications in the system.
     *
     * @return the total application count
     */
    public int countAllApplications() {
        return store.loadApplications().size();
    }

    /**
     * Returns notification strings for the hardcoded demo TA ID.
     *
     * @return a list of notification messages for the demo TA
     * @deprecated Use the Notification-based {@link #getNotificationsForUser(String)} instead
     */
    public List<String> getCurrentTaNotifications() {
        return getNotificationsForTa(CURRENT_TA_ID);
    }

    /**
     * Generates simple notification strings for a TA based on their application statuses.
     *
     * @param taId the TA identifier
     * @return a list of human-readable notification strings
     */
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

    /**
     * Saves the profile for the hardcoded demo TA ID.
     *
     * @param updatedProfile the profile data to save
     * @return an OperationResult indicating success or failure
     * @deprecated Use {@link #saveTaProfile(String, TAProfile)} with the actual TA ID instead
     */
    public OperationResult saveCurrentTaProfile(TAProfile updatedProfile) {
        return saveTaProfile(CURRENT_TA_ID, updatedProfile);
    }

    /**
     * Saves or creates a TA profile after validating all required fields,
     * duplicate student number, and email format.
     * <p>
     * Validation rules:
     * <ul>
     *   <li>fullName, studentNumber, email, skills, availability are mandatory</li>
     *   <li>studentNumber must be unique across all TA profiles (excluding the current profile)</li>
     *   <li>email must end with @bupt.edu.cn</li>
     * </ul>
     *
     * @param taId           the unique identifier of the TA whose profile is being saved
     * @param updatedProfile the TAProfile object containing updated field values
     * @return an OperationResult with success=true if saved, or success=false with an error message if validation fails
     */
    public OperationResult saveTaProfile(String taId, TAProfile updatedProfile) {
        if (isBlank(updatedProfile.getFullName())
            || isBlank(updatedProfile.getStudentNumber())
            || isBlank(updatedProfile.getEmail())
            || isBlank(updatedProfile.getSkills())
            || isBlank(updatedProfile.getAvailability())) {
            return OperationResult.failure("flash.profile.validation", "Please complete all required profile fields before saving.");
        }

        updatedProfile.setStudentNumber(updatedProfile.getStudentNumber().trim());
        updatedProfile.setEmail(updatedProfile.getEmail().trim());

        if (!isValidBuptEmail(updatedProfile.getEmail())) {
            return OperationResult.failure("flash.profile.invalidEmail", "Email must be in the format xxx@bupt.edu.cn");
        }

        if (isDuplicateStudentNumber(taId, updatedProfile.getStudentNumber())) {
            return OperationResult.failure("flash.profile.duplicateStudentId", "Student number already exists, please check");
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
                return OperationResult.success("flash.profile.saved", "Profile saved successfully.");
            }
        }

        updatedProfile.setId(taId);
        profiles.add(updatedProfile);
        store.saveProfiles(profiles);
        return OperationResult.success("flash.profile.created", "Profile created successfully.");
    }

    /**
     * Uploads a CV for the hardcoded demo TA ID.
     *
     * @param part the multipart file part from the upload request
     * @return an OperationResult indicating success or failure
     * @deprecated Use {@link #uploadTaCv(String, Part)} with the actual TA ID instead
     */
    public OperationResult uploadCurrentTaCv(Part part) {
        return uploadTaCv(CURRENT_TA_ID, part);
    }

    /**
     * Uploads and stores a CV file for a specific TA after validating file type and size.
     * <p>
     * Validation rules:
     * <ul>
     *   <li>File must not be empty</li>
     *   <li>File extension must be .pdf, .doc, or .docx</li>
     *   <li>File size must not exceed 10MB</li>
     *   <li>The TA must have an existing profile</li>
     * </ul>
     * If a CV already exists for the TA, the old file is deleted before the new one is saved.
     *
     * @param taId the TA identifier whose CV is being uploaded
     * @param part the multipart file part from the HTTP request
     * @return an OperationResult with success=true if uploaded, or success=false with an error message
     */
    public OperationResult uploadTaCv(String taId, Part part) {
        if (part == null || part.getSize() == 0) {
            return OperationResult.failure("flash.cv.no-file", "Please choose a CV file before uploading.");
        }

        if (part.getSize() > MAX_CV_FILE_SIZE) {
            return OperationResult.failure("flash.cv.oversized", "File size exceeds the 10MB limit, please upload a smaller file");
        }

        String submittedName = extractFileName(part);
        String lowerName = submittedName.toLowerCase(Locale.ENGLISH);
        if (!(lowerName.endsWith(".pdf") || lowerName.endsWith(".doc") || lowerName.endsWith(".docx"))) {
            return OperationResult.failure("flash.cv.invalid-type", "Invalid file type. Please upload a PDF, DOC, or DOCX file.");
        }

        TAProfile profile = getTaProfile(taId);
        if (profile == null) {
            return OperationResult.failure("flash.cv.not-found", "TA profile was not found.");
        }

        String finalName = profile.getFullName().replaceAll("[^A-Za-z0-9]", "") + "_" + submittedName.replaceAll("\\s+", "_");

        try (InputStream inputStream = part.getInputStream()) {
            if (profile.getCvPath() != null && !profile.getCvPath().isEmpty()) {
                java.io.File oldFile = new java.io.File(profile.getCvPath());
                if (oldFile.exists()) {
                    oldFile.delete();
                }
            }
            String path = store.saveCvFile(inputStream, finalName);
            profile.setCvPath(path);
            saveTaProfile(taId, profile);
            return OperationResult.success("flash.cv.uploaded", "CV uploaded successfully. Stored locally and linked through JSON metadata.");
        } catch (IOException exception) {
            return OperationResult.failure("flash.cv.store-error", "Unable to store the uploaded file locally.");
        }
    }

    /**
     * Submits an application for the hardcoded demo TA ID.
     *
     * @param jobId                the job posting ID to apply for
     * @param priority             the preference ranking (Priority 1/2/3)
     * @param notes                the motivation note
     * @param applicantSkills      skills declared at application time
     * @param applicantDescription description declared at application time
     * @return an OperationResult indicating success or failure
     * @deprecated Use {@link #submitTaApplication(String, String, String, String, String, String)} with the actual TA ID instead
     */
    public OperationResult submitCurrentTaApplication(String jobId, String priority, String notes, String applicantSkills, String applicantDescription) {
        return submitTaApplication(CURRENT_TA_ID, jobId, priority, notes, applicantSkills, applicantDescription);
    }

    /**
     * Submits a TA application for a specific job posting after validating business rules.
     * <p>
     * Validation rules:
     * <ul>
     *   <li>The job must exist</li>
     *   <li>The job must have "Open" status</li>
     *   <li>The TA must not exceed the maximum application limit (3)</li>
     *   <li>The TA must not have already applied to this job</li>
     * </ul>
     * On success, a notification is sent to the MO who owns the job.
     *
     * @param taId                 the TA identifier submitting the application
     * @param jobId                the job posting identifier
     * @param priority             the preference ranking (Priority 1/2/3)
     * @param notes                the motivation note
     * @param applicantSkills      skills declared at application time
     * @param applicantDescription description declared at application time
     * @return an OperationResult with success=true if submitted, or success=false with an error message
     */
    public OperationResult submitTaApplication(String taId, String jobId, String priority, String notes, String applicantSkills, String applicantDescription) {
        JobPosting job = getJobById(jobId);
        if (job == null) {
            return OperationResult.failure("flash.job.not-found", "The selected job posting does not exist.");
        }
        if (!"Open".equals(job.getStatus())) {
            return OperationResult.failure("flash.job.closed-block", "This posting is closed and cannot accept new applications.");
        }
        if (countApplicationsForTa(taId) >= MAX_APPLICATIONS) {
            return OperationResult.failure("flash.job.max-applications", "Application blocked. A TA can apply for at most three jobs.");
        }
        for (ApplicationRecord application : getApplicationsForTa(taId)) {
            if (jobId.equals(application.getJobId())) {
                return OperationResult.failure("flash.job.duplicate", "You have already applied for this job.");
            }
        }

        ApplicationRecord application = new ApplicationRecord();
        application.setId("app-" + UUID.randomUUID().toString().substring(0, 8));
        application.setJobId(jobId);
        application.setTaId(taId);
        application.setPriority(isBlank(priority) ? "Priority 3" : priority);
        application.setStatus("Submitted");
        application.setNotes(isBlank(notes) ? "TA application submitted." : notes.trim());
        application.setReviewerNotes("");
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

        return OperationResult.success("flash.job.submitted", "Application submitted. Status is now Submitted.");
    }

    /**
     * Saves a job posting for the hardcoded demo MO ID.
     *
     * @param draft the JobPosting data to save
     * @return an OperationResult indicating success or failure
     * @deprecated Use {@link #saveJobPosting(String, JobPosting)} with the actual MO ID instead
     */
    public OperationResult saveJobPosting(JobPosting draft) {
        return saveJobPosting(CURRENT_MO_ID, draft);
    }

    /**
     * Creates or updates a job posting after validating all required fields and the deadline.
     * <p>
     * Validation rules:
     * <ul>
     *   <li>moduleCode, title, skills, requirements, workload, deadline are mandatory</li>
     *   <li>deadline must be a date later than today</li>
     * </ul>
     * If the draft has no ID, a new posting is created with "Open" status.
     * If the draft has an existing ID, the matching posting is updated.
     *
     * @param moId  the MO identifier who owns this job posting
     * @param draft the JobPosting object containing field values
     * @return an OperationResult with success=true if saved, or success=false with an error message
     */
    public OperationResult saveJobPosting(String moId, JobPosting draft) {
        if (isBlank(draft.getModuleCode()) || isBlank(draft.getTitle()) || isBlank(draft.getSkills())
            || isBlank(draft.getRequirements()) || isBlank(draft.getWorkload()) || isBlank(draft.getDeadline())) {
            return OperationResult.failure("flash.job.validation", "Please complete all required job posting fields.");
        }
        if (draft.getWeeklyHours() <= 0) {
            draft.setWeeklyHours(parseWeeklyHours(draft.getWorkload()));
        }
        if (draft.getWeeklyHours() <= 0) {
            return OperationResult.failure("flash.job.validation", "Please enter a positive weekly workload.");
        }

        boolean isExistingJob = false;
        if (!isBlank(draft.getId())) {
            for (JobPosting existing : store.loadJobs()) {
                if (draft.getId().equals(existing.getId())) {
                    isExistingJob = true;
                    break;
                }
            }
        }

        if (!isExistingJob && !isValidFutureDeadline(draft.getDeadline())) {
            return OperationResult.failure("flash.job.invalidDeadline", "Deadline must be later than today");
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
        return OperationResult.success(updated ? "flash.job.updated" : "flash.job.created",
            updated ? "Job posting updated successfully." : "Job posting created successfully.");
    }

    /**
     * Manually closes a job posting, preventing new TA applications.
     *
     * @param jobId the job posting identifier to close
     * @return an OperationResult with success=true if closed, or success=false if the job was not found
     */
    public OperationResult closeJobPosting(String jobId) {
        List<JobPosting> jobs = store.loadJobs();
        for (JobPosting job : jobs) {
            if (job.getId().equals(jobId)) {
                job.setStatus("Closed");
                store.saveJobs(jobs);
                return OperationResult.success("flash.job.closed", "Posting closed. New TA applications are now blocked.");
            }
        }
        return OperationResult.failure("flash.job.not-found-close", "Unable to find the selected job posting.");
    }

    /**
     * Updates the status of an application record after validating workload constraints.
     * <p>
     * If the new status is "Accepted" and the TA has already reached the maximum
     * accepted jobs limit, the operation is rejected. On success, a notification
     * is sent to the TA. If the TA reaches the workload cap after acceptance,
     * an overload notification is also sent.
     *
     * @param applicationId the application record identifier to update
     * @param status        the new status value (Submitted, Under Review, Shortlisted, Accepted, Rejected)
     * @param notes         optional review notes from the MO; if blank, a default note is generated
     * @return an OperationResult with success=true if updated, or success=false with an error message
     */
    public OperationResult updateApplicationStatus(String applicationId, String status, String notes) {
        if (!isValidApplicationStatus(status)) {
            return OperationResult.failure("flash.review.invalid-status", "Invalid application status.");
        }
        List<ApplicationRecord> applications = store.loadApplications();
        for (ApplicationRecord application : applications) {
            if (application.getId().equals(applicationId)) {
                if ("Accepted".equals(status) && countAcceptedJobsForTa(application.getTaId()) >= MAX_ACCEPTED_JOBS
                    && !"Accepted".equals(application.getStatus())) {
                    return OperationResult.failure("flash.review.overload", "Acceptance would exceed the TA workload cap.");
                }
                application.setStatus(status);
                application.setReviewerNotes(isBlank(notes) ? buildDefaultStatusNote(status) : notes.trim());
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

                return OperationResult.success("flash.review.updated", "Applicant status updated. TA dashboard and applications view now reflect the decision.");
            }
        }
        return OperationResult.failure("flash.review.not-found", "Application record not found.");
    }

    public OperationResult bulkShortlistApplications(String jobId, String reviewerNotes) {
        List<ApplicationRecord> applications = store.loadApplications();
        int updatedCount = 0;
        for (ApplicationRecord application : applications) {
            if (jobId.equals(application.getJobId())
                && !"Accepted".equals(application.getStatus())
                && !"Rejected".equals(application.getStatus())) {
                application.setStatus("Shortlisted");
                application.setReviewerNotes(isBlank(reviewerNotes)
                    ? buildDefaultStatusNote("Shortlisted")
                    : reviewerNotes.trim());
                updatedCount++;
                addNotification(application.getTaId(), "status",
                    "Your application for " + getJobTitle(application.getJobId()) + " has been Shortlisted.",
                    "/ta/applications");
            }
        }
        store.saveApplications(applications);
        return OperationResult.success("flash.review.bulk-shortlisted",
            updatedCount + " applicant record(s) marked Shortlisted.");
    }

    /**
     * Computes workload summaries for all TAs in the system. Each summary
     * includes the TA's name, accepted module codes, accepted job count, total
     * weekly hours, and an overload flag when weekly hours exceed 10.
     *
     * @return a list of WorkloadSummary objects, one per TA profile
     */
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
                summary.setTotalWeeklyHours(summary.getTotalWeeklyHours() + getWeeklyHoursForJob(job));
            }
        }

        for (WorkloadSummary summary : summaries.values()) {
            summary.setAcceptedCount(summary.getAcceptedModules().size());
            summary.setOverloadFlag(summary.getTotalWeeklyHours() > 10);
        }

        return new ArrayList<WorkloadSummary>(summaries.values());
    }

    public List<WorkloadSummary> getWorkloadSummaries(String query) {
        List<WorkloadSummary> summaries = getWorkloadSummaries();
        if (isBlank(query)) {
            return summaries;
        }
        String lowerQuery = query.toLowerCase(Locale.ENGLISH);
        List<WorkloadSummary> filtered = new ArrayList<WorkloadSummary>();
        for (WorkloadSummary summary : summaries) {
            if (matchesSearch(summary.getTaId(), lowerQuery) || matchesSearch(summary.getTaName(), lowerQuery)) {
                filtered.add(summary);
            }
        }
        return filtered;
    }

    /**
     * Counts the number of TAs whose workload exceeds the overload threshold.
     *
     * @return the count of overloaded TAs
     */
    public int countOverloadSummaries() {
        int overloads = 0;
        for (WorkloadSummary summary : getWorkloadSummaries()) {
            if (summary.isOverloadFlag()) {
                overloads++;
            }
        }
        return overloads;
    }

    /**
     * Returns a list of informational notes about the current AI feature status.
     *
     * @return a list of human-readable AI feature status strings
     */
    public List<String> getAiTodoNotes() {
        List<String> todoNotes = new ArrayList<String>();
        todoNotes.add("AI agents can provide TA fit advice and hiring support summaries.");
        todoNotes.add("PDF CV text is analysed when a PDF CV is available.");
        todoNotes.add("Local rule fallback is used when LLM mode is disabled or unavailable.");
        todoNotes.add("AI output is advisory only and never accepts or rejects applications automatically.");
        return todoNotes;
    }

    public List<JobRecommendation> getRecommendedJobsForTa(String taId) {
        TAProfile profile = getProfileById(taId);
        List<JobRecommendation> recommendations = new ArrayList<JobRecommendation>();
        if (profile == null) {
            return recommendations;
        }
        Set<String> taSkills = normalizeSkillSet(profile.getSkills());
        for (JobPosting job : getOpenJobs()) {
            List<String> requiredSkills = new ArrayList<String>(normalizeSkillSet(job.getSkills()));
            if (requiredSkills.isEmpty()) {
                continue;
            }
            List<String> matchedSkills = new ArrayList<String>();
            for (String requiredSkill : requiredSkills) {
                if (taSkills.contains(requiredSkill)) {
                    matchedSkills.add(requiredSkill);
                }
            }
            int matchRate = (int) Math.round((matchedSkills.size() * 100.0) / requiredSkills.size());
            if (matchRate >= 60) {
                JobRecommendation recommendation = new JobRecommendation();
                recommendation.setJob(job);
                recommendation.setMatchedSkills(matchedSkills);
                recommendation.setMatchedCount(matchedSkills.size());
                recommendation.setTotalRequiredCount(requiredSkills.size());
                recommendation.setMatchRate(matchRate);
                recommendations.add(recommendation);
            }
        }
        Collections.sort(recommendations, new Comparator<JobRecommendation>() {
            public int compare(JobRecommendation left, JobRecommendation right) {
                int rateCompare = right.getMatchRate() - left.getMatchRate();
                if (rateCompare != 0) {
                    return rateCompare;
                }
                return safeText(left.getJob().getDeadline()).compareToIgnoreCase(safeText(right.getJob().getDeadline()));
            }
        });
        return recommendations.size() > 5 ? new ArrayList<JobRecommendation>(recommendations.subList(0, 5)) : recommendations;
    }

    /**
     * Searches job postings by keyword, matching against module code, title,
     * skills, description, requirements, and status fields.
     *
     * @param query the search query string; if blank, all jobs are returned
     * @return a list of matching JobPosting records
     */
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

    /**
     * Searches application records by keyword, matching against status, priority,
     * notes, and related job/profile fields.
     *
     * @param query the search query string; if blank, all applications are returned
     * @return a list of matching ApplicationRecord records
     */
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

    /**
     * Searches TA profiles by keyword, matching against full name, student number,
     * email, skills, and availability fields.
     *
     * @param query the search query string; if blank, all profiles are returned
     * @return a list of matching TAProfile records
     */
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

    /**
     * Checks whether a text field contains the given search query (case-insensitive).
     *
     * @param text       the text to search within
     * @param lowerQuery the lowercased search query
     * @return true if the text contains the query, false otherwise
     */
    private boolean matchesSearch(String text, String lowerQuery) {
        return text != null && text.toLowerCase(Locale.ENGLISH).contains(lowerQuery);
    }

    /**
     * Returns all notifications for a specific user, sorted by creation time
     * in descending order (newest first).
     *
     * @param userId the user identifier to filter notifications for
     * @return a sorted list of Notification objects for the specified user
     */
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

    /**
     * Counts the number of unread notifications for a specific user.
     *
     * @param userId the user identifier
     * @return the count of unread notifications
     */
    public int countUnreadNotifications(String userId) {
        int count = 0;
        for (Notification notification : store.loadNotifications()) {
            if (userId.equals(notification.getUserId()) && !notification.isRead()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Marks a single notification as read.
     *
     * @param notificationId the notification identifier to mark as read
     * @return an OperationResult with success=true if found and marked, or success=false if not found
     */
    public OperationResult markNotificationRead(String notificationId) {
        List<Notification> notifications = store.loadNotifications();
        for (Notification notification : notifications) {
            if (notification.getId().equals(notificationId)) {
                notification.setRead(true);
                store.saveNotifications(notifications);
                return OperationResult.success("flash.inbox.marked-read", "Notification marked as read.");
            }
        }
        return OperationResult.failure("flash.notif.not-found", "Notification not found.");
    }

    /**
     * Marks all unread notifications for a specific user as read.
     *
     * @param userId the user identifier whose notifications should be marked as read
     * @return an OperationResult with success=true
     */
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
        return OperationResult.success("flash.inbox.all-read", "All notifications marked as read.");
    }

    /**
     * Creates and persists a new notification for a specific user.
     *
     * @param userId   the recipient user identifier
     * @param category the notification category (status, review, overload, deadline)
     * @param message  the notification message body
     * @param link     a URL path for navigating to the related resource
     */
    public void addNotification(String userId, String category, String message, String link) {
        List<Notification> notifications = store.loadNotifications();
        notifications.add(Notification.create(userId, category, message, link));
        store.saveNotifications(notifications);
    }

    /**
     * Returns advisory AI fit guidance for a TA/job pair.
     *
     * @param taId  the TA profile identifier
     * @param jobId the job posting identifier
     * @return an advisory AI fit result
     */
    public AiFitResult getTaFitAdvice(String taId, String jobId) {
        return getTaFitAdvice(taId, jobId, null);
    }

    /**
     * Returns advisory AI fit guidance for a TA/job pair with user-scoped tool permissions.
     *
     * @param taId        the TA profile identifier
     * @param jobId       the job posting identifier
     * @param currentUser the authenticated user for tool permission checks
     * @return an advisory AI fit result
     */
    public AiFitResult getTaFitAdvice(String taId, String jobId, UserAccount currentUser) {
        if (aiAgentService.isToolCallingEnabled()) {
            return aiAgentService.adviseTaFitWithTools(taId, jobId, currentUser);
        }
        TAProfile profile = getProfileById(taId);
        JobPosting job = getJobById(jobId);
        return aiAgentService.adviseTaFit(profile, job, countAcceptedJobsForTa(taId));
    }

    /**
     * Returns advisory AI hiring support for a selected candidate.
     *
     * @param taId        the TA profile identifier
     * @param jobId       the job posting identifier
     * @param application the selected application record
     * @return an advisory AI candidate summary
     */
    public AiCandidateSummary getCandidateSummary(String taId, String jobId, ApplicationRecord application) {
        return getCandidateSummary(taId, jobId, application, null);
    }

    /**
     * Returns advisory AI hiring support for a selected candidate with user-scoped tool permissions.
     *
     * @param taId        the TA profile identifier
     * @param jobId       the job posting identifier
     * @param application the selected application record
     * @param currentUser the authenticated user for tool permission checks
     * @return an advisory AI candidate summary
     */
    public AiCandidateSummary getCandidateSummary(String taId, String jobId, ApplicationRecord application,
                                                  UserAccount currentUser) {
        if (aiAgentService.isToolCallingEnabled()) {
            return aiAgentService.summarizeCandidateWithTools(
                taId,
                jobId,
                application == null ? null : application.getId(),
                currentUser
            );
        }
        TAProfile profile = getProfileById(taId);
        JobPosting job = getJobById(jobId);
        return aiAgentService.summarizeCandidate(profile, job, application, countAcceptedJobsForTa(taId));
    }

    /**
     * Returns advisory AI workload notes for the admin workload dashboard.
     *
     * @return structured workload advice for each TA
     */
    public List<AiWorkloadAdvice> getAiWorkloadAdvice() {
        return aiAgentService.adviseWorkload(getWorkloadSummaries());
    }

    /**
     * Runs the interactive tool-calling AI chat workspace.
     *
     * @param userMessage the user's natural language request
     * @param currentUser the authenticated user for permission-scoped tools
     * @return structured chat result with tool trace and final JSON
     */
    public ToolCallingResult chatWithAiAgent(String userMessage, UserAccount currentUser) {
        return aiAgentService.chat(userMessage, currentUser);
    }

    /**
     * Runs the interactive tool-calling AI chat workspace with session memory.
     *
     * @param userMessage the user's natural language request
     * @param currentUser the authenticated user for permission-scoped tools
     * @param memory short-term session memory for resolving follow-up references
     * @return structured chat result with tool trace and final JSON
     */
    public ToolCallingResult chatWithAiAgent(String userMessage, UserAccount currentUser, AiChatMemory memory) {
        return aiAgentService.chat(userMessage, currentUser, memory);
    }

    /**
     * Calculates a skill fit score (0-100) between a TA's declared skills
     * and a job posting's required skills. The score is computed as:
     * (number of matched job skills / total job skills) * 100.
     * A skill is considered matched if the TA skill equals, contains,
     * or is contained within the job skill (case-insensitive).
     *
     * @param taId  the TA identifier whose skills are being evaluated
     * @param jobId the job posting identifier whose requirements are being matched against
     * @return an integer fit score between 0 and 100
     */
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

    /**
     * Identifies which required skills from a job posting are missing from
     * a TA's declared skill set. A skill is considered missing if no TA skill
     * equals, contains, or is contained within the job skill.
     *
     * @param taId  the TA identifier whose skills are being checked
     * @param jobId the job posting identifier whose required skills are being compared
     * @return a list of missing skill names (lowercase)
     */
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

    /**
     * Generates AI-powered workload balancing advice based on current workload data.
     * Identifies overloaded TAs and suggests available TAs for redistribution.
     *
     * @return a list of human-readable advice strings
     */
    public List<String> getWorkloadBalancingAdvice() {
        List<String> advice = new ArrayList<String>();
        List<AiWorkloadAdvice> aiAdvice = getAiWorkloadAdvice();
        boolean hasRisk = false;
        for (AiWorkloadAdvice item : aiAdvice) {
            if ("at_cap".equals(item.getWorkloadRisk()) || "caution".equals(item.getWorkloadRisk())) {
                hasRisk = true;
                advice.add(item.getTaName() + ": " + item.getAdvice());
            }
        }
        if (!hasRisk) {
            advice.add("No workload balancing issues detected. All TAs are within acceptable limits.");
        }
        return advice;
    }

    /**
     * Returns the title of a job posting by its identifier.
     *
     * @param jobId the job posting identifier
     * @return the job title, or "Unknown job" if not found
     */
    public String getJobTitle(String jobId) {
        JobPosting job = getJobById(jobId);
        return job == null ? "Unknown job" : job.getTitle();
    }

    /**
     * Returns the module code of a job posting by its identifier.
     *
     * @param jobId the job posting identifier
     * @return the module code, or "-" if not found
     */
    public String getJobModuleCode(String jobId) {
        JobPosting job = getJobById(jobId);
        return job == null ? "-" : job.getModuleCode();
    }

    /**
     * Counts the number of applicants for a specific job posting.
     *
     * @param jobId the job posting identifier
     * @return the number of applications for the specified job
     */
    public int countApplicantsForJob(String jobId) {
        return getApplicationsForJob(jobId).size();
    }

    /**
     * Counts the number of accepted jobs for a specific TA.
     *
     * @param taId the TA identifier
     * @return the number of applications with "Accepted" status for the specified TA
     */
    private int countAcceptedJobsForTa(String taId) {
        if (isBlank(taId)) {
            return 0;
        }
        int count = 0;
        for (ApplicationRecord application : store.loadApplications()) {
            if (taId.equals(application.getTaId()) && "Accepted".equals(application.getStatus())) {
                count++;
            }
        }
        return count;
    }

    private int getWeeklyHoursForJob(JobPosting job) {
        if (job == null) {
            return 0;
        }
        if (job.getWeeklyHours() > 0) {
            return job.getWeeklyHours();
        }
        return parseWeeklyHours(job.getWorkload());
    }

    private int parseWeeklyHours(String workload) {
        if (isBlank(workload)) {
            return 0;
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d+)").matcher(workload);
        if (!matcher.find()) {
            return 0;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private boolean isValidApplicationStatus(String status) {
        return "Submitted".equals(status)
            || "Under Review".equals(status)
            || "Shortlisted".equals(status)
            || "Accepted".equals(status)
            || "Rejected".equals(status);
    }

    private Set<String> normalizeSkillSet(String skills) {
        Set<String> normalized = new LinkedHashSet<String>();
        if (isBlank(skills)) {
            return normalized;
        }
        String normalizedText = skills.toLowerCase(Locale.ENGLISH).replace("object-oriented programming", "oop");
        String[] tokens = normalizedText.split("[^a-z0-9+#]+");
        for (String token : tokens) {
            if (!isBlank(token)) {
                normalized.add(token.trim());
            }
        }
        return normalized;
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    /**
     * Automatically closes any job postings whose deadline has passed.
     * Iterates through all jobs with "Open" status and sets their status
     * to "Closed" if the deadline date is before today. Changes are
     * persisted to the JSON data store in a single batch save.
     */
    private void autoCloseExpiredJobs() {
        List<JobPosting> jobs = store.loadJobs();
        boolean changed = false;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        String today = dateFormat.format(new Date());

        for (JobPosting job : jobs) {
            if ("Open".equals(job.getStatus()) && !isBlank(job.getDeadline())) {
                if (job.getDeadline().compareTo(today) < 0) {
                    job.setStatus("Closed");
                    changed = true;
                }
            }
        }

        if (changed) {
            store.saveJobs(jobs);
        }
    }

    /**
     * Validates that the given email address ends with the BUPT domain.
     *
     * @param email the email address to validate
     * @return true if the email ends with "@bupt.edu.cn" (case-insensitive), false otherwise
     */
    private boolean isValidBuptEmail(String email) {
        if (isBlank(email)) {
            return false;
        }
        return email.trim().toLowerCase(Locale.ENGLISH).endsWith("@bupt.edu.cn");
    }

    /**
     * Checks whether the given student number is already used by another TA profile.
     *
     * @param currentTaId    the TA ID of the profile being saved (excluded from the check)
     * @param studentNumber  the student number to check for duplicates
     * @return true if another profile with the same student number exists, false otherwise
     */
    private boolean isDuplicateStudentNumber(String currentTaId, String studentNumber) {
        if (isBlank(studentNumber)) {
            return false;
        }
        for (TAProfile profile : store.loadProfiles()) {
            if (!currentTaId.equals(profile.getId())
                && studentNumber.trim().equals(profile.getStudentNumber())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates that the given deadline string represents a date strictly later than today.
     * The deadline must be in "yyyy-MM-dd" format.
     *
     * @param deadline the deadline string to validate
     * @return true if the deadline is a valid future date, false otherwise
     */
    private boolean isValidFutureDeadline(String deadline) {
        if (isBlank(deadline)) {
            return false;
        }
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            dateFormat.setLenient(false);
            Date deadlineDate = dateFormat.parse(deadline);
            Date today = dateFormat.parse(dateFormat.format(new Date()));
            return deadlineDate.after(today);
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * Extracts the submitted file name from a multipart Part object,
     * falling back to header parsing if the Servlet API method is unavailable.
     *
     * @param part the multipart file part
     * @return the extracted file name, or "uploaded_cv.pdf" as a last resort
     */
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

    /**
     * Extracts the file name from the Content-Disposition header of a multipart Part.
     *
     * @param part the multipart file part
     * @return the extracted file name, or null if not found
     */
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

    /**
     * Generates a default review note for a given application status change.
     *
     * @param status the new application status
     * @return a human-readable default note describing the status change
     */
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
        if ("Shortlisted".equals(status)) {
            return "Shortlisted for final MO comparison.";
        }
        return "Status updated by MO.";
    }

    /**
     * Checks whether a string value is null or contains only whitespace.
     *
     * @param value the string to check
     * @return true if the value is null or blank, false otherwise
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
