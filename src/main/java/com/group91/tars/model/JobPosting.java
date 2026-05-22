package com.group91.tars.model;

/**
 * Represents a TA job posting created by a Module Organiser in the ISTARS system.
 * Each posting defines a TA vacancy for a specific academic module, including
 * required skills, workload, and an application deadline. Postings transition
 * from "Open" to "Closed" status either manually by the MO or automatically
 * when the deadline passes.
 */
public class JobPosting {
    /** Unique identifier (e.g. "job-1"). */
    private String id;
    /** Foreign key referencing the MO's linkedId who created this posting (e.g. "mo-1"). */
    private String moId;
    /** Academic module code (e.g. "EIE3320"). */
    private String moduleCode;
    /** Job title (e.g. "Object-Oriented Programming TA"). */
    private String title;
    /** Comma-separated list of required skills for the position. */
    private String skills;
    /** Knowledge and competency requirements description. */
    private String requirements;
    /** Weekly workload description (e.g. "6 hours / week"). */
    private String workload;
    /** Numeric weekly workload in hours. Used by Admin workload summaries. */
    private int weeklyHours;
    /** Application deadline in yyyy-MM-dd format. Must be a future date when creating a new posting. */
    private String deadline;
    /** Posting status: "Open" or "Closed". Expired postings are auto-closed by the system. */
    private String status;
    /** Free-text description of the TA role and responsibilities. */
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMoId() {
        return moId;
    }

    public void setMoId(String moId) {
        this.moId = moId;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getWorkload() {
        return workload;
    }

    public void setWorkload(String workload) {
        this.workload = workload;
    }

    public int getWeeklyHours() {
        return weeklyHours;
    }

    public void setWeeklyHours(int weeklyHours) {
        this.weeklyHours = weeklyHours;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
