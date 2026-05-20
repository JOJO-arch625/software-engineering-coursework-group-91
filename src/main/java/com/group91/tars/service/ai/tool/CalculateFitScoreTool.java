package com.group91.tars.service.ai.tool;

import com.google.gson.JsonObject;
import com.group91.tars.model.JobPosting;
import com.group91.tars.model.TAProfile;
import com.group91.tars.service.ai.LocalRuleAiEngine;

public class CalculateFitScoreTool implements AgentTool {
    private final RecruitmentToolSupport support;
    private final LocalRuleAiEngine localRuleAiEngine;

    public CalculateFitScoreTool(RecruitmentToolSupport support, LocalRuleAiEngine localRuleAiEngine) {
        this.support = support;
        this.localRuleAiEngine = localRuleAiEngine;
    }

    public String getName() {
        return "calculate_fit_score";
    }

    public String getDescription() {
        return "Calculate deterministic skill fit score, matched skills, and missing skills for a TA and job.";
    }

    public JsonObject getParametersSchema() {
        JsonObject schema = support.schema("taId", "jobId");
        JsonObject properties = schema.getAsJsonObject("properties");
        JsonObject cvText = new JsonObject();
        cvText.addProperty("type", "string");
        properties.add("cvText", cvText);
        return schema;
    }

    public AgentToolResult execute(JsonObject arguments) {
        String taId = support.requiredString(arguments, "taId");
        String jobId = support.requiredString(arguments, "jobId");
        if (taId == null) {
            return AgentToolResult.failure(getName(), "Missing required argument: taId");
        }
        if (jobId == null) {
            return AgentToolResult.failure(getName(), "Missing required argument: jobId");
        }
        if (!support.canReadTa(taId) || !support.canReadJob(jobId)) {
            return AgentToolResult.failure(getName(), "Permission denied for fit calculation.");
        }
        TAProfile profile = support.findProfile(taId);
        JobPosting job = support.findJob(jobId);
        if (profile == null) {
            return AgentToolResult.failure(getName(), "TA profile not found: " + taId);
        }
        if (job == null) {
            return AgentToolResult.failure(getName(), "Job posting not found: " + jobId);
        }
        String cvText = arguments.has("cvText") && !arguments.get("cvText").isJsonNull()
            ? arguments.get("cvText").getAsString()
            : "";
        LocalRuleAiEngine.SkillMatch match = localRuleAiEngine.evaluateFit(profile, job, cvText, null);
        JsonObject data = new JsonObject();
        data.addProperty("score", match.getScore());
        data.add("matchedSkills", support.toJsonArray(match.getMatchedSkills()));
        data.add("missingSkills", support.toJsonArray(match.getMissingSkills()));
        return AgentToolResult.success(getName(), data);
    }
}
