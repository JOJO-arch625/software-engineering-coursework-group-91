package com.group91.tars.model;

public class FlashMessage {
    private String level;
    private String text;

    public FlashMessage() {
    }

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
