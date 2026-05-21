package com.group91.tars.model;

/**
 * Represents a TA application record in the ISTARS recruitment system.
 * An application links a TA (via taId) to a job posting (via jobId) and
 * tracks the application lifecycle: Submitted - Under Review - Shortlisted -
 * Accepted / Rejected. The notes field stores the TA's motivation note, while
 * reviewerNotes stores MO review feedback independently.
 */
public class ApplicationRecord {
    /** Unique identifier (e.g. "app-1"). */
    private String id;
    /** Foreign key referencing the applied JobPosting's id. */
    private String jobId;
    /** Foreign key referencing the applying TAProfile's id. */
    private String taId;
    /** Preference ranking: "Priority 1", "Priority 2", or "Priority 3". */
    private String priority;
    /** Current status: "Submitted", "Under Review", "Shortlisted", "Accepted", or "Rejected". */
    private String status;
    /** Motivation note from the TA. */
    private String notes;
    /** Review feedback entered by the MO. */
    private String reviewerNotes;
    /** Timestamp of submission in "yyyy-MM-dd HH:mm" format. */
    private String submittedAt;
    /** Skills declared by the TA at the time of application. */
    private String applicantSkills;
    /** Self-description provided by the TA at the time of application. */
    private String applicantDescription;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getTaId() {
        return taId;
    }

    public void setTaId(String taId) {
        this.taId = taId;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getReviewerNotes() {
        return reviewerNotes;
    }

    public void setReviewerNotes(String reviewerNotes) {
        this.reviewerNotes = reviewerNotes;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(String submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getApplicantSkills() {
        return applicantSkills;
    }

    public void setApplicantSkills(String applicantSkills) {
        this.applicantSkills = applicantSkills;
    }

    public String getApplicantDescription() {
        return applicantDescription;
    }

    public void setApplicantDescription(String applicantDescription) {
        this.applicantDescription = applicantDescription;
    }
}
