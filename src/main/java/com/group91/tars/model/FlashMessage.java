package com.group91.tars.model;

/**
 * One-time flash message carried across a redirect via the HTTP session.
 * Consumed on the next GET request and displayed as an alert banner in the UI.
 * Supports three levels: success (green), info (blue), and error (red).
 */
public class FlashMessage {
    private String level;
    private String text;

    public FlashMessage() {
    }

    /**
     * Constructs a flash message with a severity level and display text.
     *
     * @param level severity level: "success", "info", or "error"
     * @param text  the message text to display
     */
    public FlashMessage(String level, String text) {
        this.level = level;
        this.text = text;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
