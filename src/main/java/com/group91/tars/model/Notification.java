package com.group91.tars.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Represents an in-app notification delivered to a user in the ISTARS system.
 * Notifications are categorised (status, review, overload, deadline) and
 * carry a link for one-click navigation to the related resource.
 */
public class Notification {
    /** Unique identifier (e.g. "notif-123456789"). */
    private String id;
    /** Recipient user identifier (matches UserAccount.linkedId). */
    private String userId;
    /** Category: "status", "review", "overload", or "deadline". */
    private String category;
    /** Human-readable notification body. */
    private String message;
    /** URL path for navigating to the related resource. */
    private String link;
    /** Whether the notification has been read by the recipient. */
    private boolean read;
    /** Timestamp of creation in "yyyy-MM-dd HH:mm" format. */
    private String createdAt;

    public Notification() {
    }

    /**
     * Full constructor for creating a notification with all fields.
     *
     * @param id        unique identifier
     * @param userId    recipient user identifier
     * @param category  notification category
     * @param message   notification body
     * @param link      navigation URL path
     * @param read      read status
     * @param createdAt creation timestamp
     */
    public Notification(String id, String userId, String category, String message, String link, boolean read, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.message = message;
        this.link = link;
        this.read = read;
        this.createdAt = createdAt;
    }

    /**
     * Factory method that creates a new notification with an auto-generated ID
     * and the current timestamp. The read status defaults to false.
     *
     * @param userId   recipient user identifier
     * @param category notification category
     * @param message  notification body
     * @param link     navigation URL path
     * @return a new Notification instance
     */
    public static Notification create(String userId, String category, String message, String link) {
        Notification n = new Notification();
        n.setId("notif-" + System.nanoTime());
        n.setUserId(userId);
        n.setCategory(category);
        n.setMessage(message);
        n.setLink(link);
        n.setRead(false);
        n.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).format(new Date()));
        return n;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
