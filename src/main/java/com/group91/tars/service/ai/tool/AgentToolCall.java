package com.group91.tars.service.ai.tool;

import com.google.gson.JsonObject;

public class AgentToolCall {
    private String tool;
    private JsonObject arguments;

    public String getTool() {
        return tool;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }

    public JsonObject getArguments() {
        return arguments;
    }

    public void setArguments(JsonObject arguments) {
        this.arguments = arguments;
    }
}
