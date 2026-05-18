package com.group91.tars.model.ai;

import java.io.Serializable;

public class AiChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String role;
    private String content;
    private String createdAt;

    public AiChatMessage(String role, String content, String createdAt) {
        this.role = role;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
