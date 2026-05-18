package com.group91.tars.service.ai.tool;

import com.google.gson.JsonObject;

public class GetWorkloadStatusTool implements AgentTool {
    private final RecruitmentToolSupport support;

    public GetWorkloadStatusTool(RecruitmentToolSupport support) {
        this.support = support;
    }

    public String getName() {
        return "get_workload_status";
    }

    public String getDescription() {
        return "Get a TA's accepted job count and workload risk.";
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
            return AgentToolResult.failure(getName(), "Permission denied for workload status: " + taId);
        }
        int acceptedJobs = support.countAcceptedJobs(taId);
        String risk = support.workloadRisk(acceptedJobs);
        JsonObject data = new JsonObject();
        data.addProperty("acceptedJobs", acceptedJobs);
        data.addProperty("risk", risk);
        data.addProperty("message", support.workloadMessage(acceptedJobs, risk));
        return AgentToolResult.success(getName(), data);
    }
}
