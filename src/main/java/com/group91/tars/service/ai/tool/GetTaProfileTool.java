package com.group91.tars.service.ai.tool;

import com.google.gson.JsonObject;
import com.group91.tars.model.TAProfile;

public class GetTaProfileTool implements AgentTool {
    private final RecruitmentToolSupport support;

    public GetTaProfileTool(RecruitmentToolSupport support) {
        this.support = support;
    }

    public String getName() {
        return "get_ta_profile";
    }

    public String getDescription() {
        return "Get a TA profile by taId. Returns profile skills and availability without exposing CV file path.";
    }

    public JsonObject getParametersSchema() {
        return support.schema("taId");
    }

    public AgentToolResult execute(JsonObject arguments) {
        String taId = support.requiredString(arguments, "taId");
        if (taId == null) {
            return AgentToolResult.failure(getName(), "Missing required argument: taId");
        }
        if (!support.canReadTa(taId)) {
            return AgentToolResult.failure(getName(), "Permission denied for TA profile: " + taId);
        }
        TAProfile profile = support.findProfile(taId);
        if (profile == null) {
            return AgentToolResult.failure(getName(), "TA profile not found: " + taId);
        }
        JsonObject data = new JsonObject();
        data.addProperty("taId", profile.getId());
        data.addProperty("fullName", profile.getFullName());
        data.addProperty("skills", profile.getSkills());
        data.addProperty("availability", profile.getAvailability());
        data.addProperty("hasCv", profile.getCvPath() != null && !profile.getCvPath().trim().isEmpty());
        return AgentToolResult.success(getName(), data);
    }
}
