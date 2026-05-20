package com.group91.tars.service.ai.tool;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class AgentToolRegistry {
    private final Map<String, AgentTool> tools = new LinkedHashMap<String, AgentTool>();

    public void register(AgentTool tool) {
        if (tools.containsKey(tool.getName())) {
            throw new IllegalArgumentException("Duplicate AI tool: " + tool.getName());
        }
        tools.put(tool.getName(), tool);
    }

    public AgentToolResult execute(String name, JsonObject arguments) {
        AgentTool tool = tools.get(name);
        if (tool == null) {
            return AgentToolResult.failure(name, "Unknown tool: " + name);
        }
        return tool.execute(arguments == null ? new JsonObject() : arguments);
    }

    public JsonArray describeTools() {
        JsonArray array = new JsonArray();
        for (AgentTool tool : tools.values()) {
            JsonObject item = new JsonObject();
            item.addProperty("name", tool.getName());
            item.addProperty("description", tool.getDescription());
            item.add("parameters", tool.getParametersSchema());
            array.add(item);
        }
        return array;
    }
}
