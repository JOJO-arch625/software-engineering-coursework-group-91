package com.group91.tars.service.ai.tool;

import com.google.gson.JsonObject;

public class AgentToolResult {
    private final boolean success;
    private final String toolName;
    private final JsonObject data;
    private final String errorMessage;

    private AgentToolResult(boolean success, String toolName, JsonObject data, String errorMessage) {
        this.success = success;
        this.toolName = toolName;
        this.data = data;
        this.errorMessage = errorMessage;
    }

    public static AgentToolResult success(String toolName, JsonObject data) {
        return new AgentToolResult(true, toolName, data, null);
    }

    public static AgentToolResult failure(String toolName, String errorMessage) {
        return new AgentToolResult(false, toolName, null, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getToolName() {
        return toolName;
    }

    public JsonObject getData() {
        return data;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("tool", toolName);
        json.addProperty("success", success);
        if (data != null) {
            json.add("data", data);
        }
        if (errorMessage != null) {
            json.addProperty("errorMessage", errorMessage);
        }
        return json;
    }
}
