package com.group91.tars.service.ai.tool;

import com.google.gson.JsonObject;

public interface AgentTool {
    String getName();

    String getDescription();

    JsonObject getParametersSchema();

    AgentToolResult execute(JsonObject arguments);
}
