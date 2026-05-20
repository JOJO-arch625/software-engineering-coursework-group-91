package com.group91.tars.service.ai.tool;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class ToolCallingResult {
    private boolean success;
    private String reply;
    private String sourceMode;
    private List<JsonObject> toolTrace = new ArrayList<JsonObject>();
    private JsonObject finalJson;
    private String errorMessage;

    public static ToolCallingResult finalResult(JsonObject finalJson, List<JsonObject> toolTrace) {
        ToolCallingResult result = new ToolCallingResult();
        result.setSuccess(true);
        result.setSourceMode("llm_tool");
        result.setFinalJson(finalJson == null ? new JsonObject() : finalJson);
        result.setToolTrace(toolTrace);
        result.setReply(extractReply(result.getFinalJson()));
        return result;
    }

    public static ToolCallingResult error(String errorMessage, List<JsonObject> toolTrace) {
        ToolCallingResult result = new ToolCallingResult();
        result.setSuccess(false);
        result.setSourceMode("error");
        result.setErrorMessage(errorMessage);
        result.setReply(errorMessage);
        result.setToolTrace(toolTrace);
        result.setFinalJson(new JsonObject());
        return result;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("success", success);
        json.addProperty("reply", reply);
        json.addProperty("sourceMode", sourceMode);
        JsonArray trace = new JsonArray();
        for (JsonObject item : toolTrace) {
            trace.add(item);
        }
        json.add("toolTrace", trace);
        json.add("finalJson", finalJson == null ? new JsonObject() : finalJson);
        if (errorMessage != null) {
            json.addProperty("errorMessage", errorMessage);
        }
        return json;
    }

    private static String extractReply(JsonObject finalJson) {
        if (finalJson == null) {
            return "Tool-calling agent completed.";
        }
        if (hasText(finalJson, "reply")) {
            return finalJson.get("reply").getAsString();
        }
        if (hasText(finalJson, "advice")) {
            return finalJson.get("advice").getAsString();
        }
        if (hasText(finalJson, "summary")) {
            return finalJson.get("summary").getAsString();
        }
        if (hasText(finalJson, "analysis")) {
            return finalJson.get("analysis").getAsString();
        }
        if (hasText(finalJson, "workloadMessage")) {
            return finalJson.get("workloadMessage").getAsString();
        }
        return "Tool-calling agent completed.";
    }

    private static boolean hasText(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull()
            && !json.get(key).getAsString().trim().isEmpty();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getSourceMode() {
        return sourceMode;
    }

    public void setSourceMode(String sourceMode) {
        this.sourceMode = sourceMode;
    }

    public List<JsonObject> getToolTrace() {
        return toolTrace;
    }

    public void setToolTrace(List<JsonObject> toolTrace) {
        this.toolTrace = toolTrace == null ? new ArrayList<JsonObject>() : toolTrace;
    }

    public JsonObject getFinalJson() {
        return finalJson;
    }

    public void setFinalJson(JsonObject finalJson) {
        this.finalJson = finalJson;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
