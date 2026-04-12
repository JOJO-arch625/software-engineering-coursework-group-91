package com.group91.tars.model;

public class Notification {
    private String id;
    private String userId;
    private String role;
    private String title;
    private String message;
    private String timestamp;
    private boolean isRead;

    public Notification() {}

    public Notification(String id, String userId, String role, String title, String message, String timestamp) {
        this.id = id;
        this.userId = userId;
        this.role = role;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.isRead = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
