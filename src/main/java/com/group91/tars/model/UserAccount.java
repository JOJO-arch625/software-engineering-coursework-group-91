package com.group91.tars.model;

import java.io.Serializable;

/**
 * Represents a user account in the ISTARS recruitment system.
 * Each account has a role (TA, MO, or ADMIN) and is linked to a role-specific
 * entity via the linkedId field. Accounts are stored in accounts.json and
 * used for session-based authentication.
 */
public class UserAccount implements Serializable {
    /** Unique account identifier (e.g. "acc-ta-1"). */
    private String id;
    /** Login username (case-insensitive during authentication). */
    private String username;
    /** Login password (stored as plaintext for this coursework prototype). */
    private String password;
    /** Human-readable display name shown in the UI. */
    private String displayName;
    /** User role: "TA", "MO", or "ADMIN". */
    private String role;
    /** Foreign key linking to the role-specific entity (e.g. "ta-1", "mo-1", "admin-1"). */
    private String linkedId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLinkedId() {
        return linkedId;
    }

    public void setLinkedId(String linkedId) {
        this.linkedId = linkedId;
    }

    /**
     * Extracts initials from the display name for use as an avatar.
     * For a single-word name, returns the first letter.
     * For a multi-word name, returns the first letters of the first and last words.
     * Falls back to the first letter of the role if the name is blank.
     *
     * @return uppercase initials string
     */
    public String getInitials() {
        if (displayName == null || displayName.trim().isEmpty()) {
            return role == null ? "U" : role.substring(0, 1).toUpperCase();
        }

        String[] parts = displayName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
