package com.group91.tars.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Notification {
    private String id;
    private String userId;
    private String category;
    private String message;
    private String link;
    private boolean read;
    private String createdAt;

    public Notification() {
    }

    public Notification(String id, String userId, String category, String message, String link, boolean read, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.message = message;
        this.link = link;
        this.read = read;
        this.createdAt = createdAt;
    }

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
