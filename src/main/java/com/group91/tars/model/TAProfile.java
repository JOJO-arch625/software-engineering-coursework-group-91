package com.group91.tars.model;

/**
 * Represents a Teaching Assistant applicant profile in the ISTARS recruitment system.
 * Each profile is linked to a {@link UserAccount} via the {@code id} field, which
 * corresponds to {@link UserAccount#getLinkedId()}. Profile data includes personal
 * information, academic details, skills, and a CV file path.
 */
public class TAProfile {
    /** Unique identifier matching the linkedId of the corresponding UserAccount (e.g. "ta-1"). */
    private String id;
    /** Full name of the TA applicant. */
    private String fullName;
    /** University student number; must be unique across all TA profiles. */
    private String studentNumber;
    /** Email address; must follow the xxx@bupt.edu.cn format. */
    private String email;
    /** Contact phone number. */
    private String phone;
    /** Comma-separated list of skills (e.g. "Java, Python, VHDL"). */
    private String skills;
    /** Free-text description of the TA's availability. */
    private String availability;
    /** Relative file path to the uploaded CV (e.g. "uploads/cv/name_CV.pdf"). */
    private String cvPath;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public String getCvPath() {
        return cvPath;
    }

    public void setCvPath(String cvPath) {
        this.cvPath = cvPath;
    }
}
