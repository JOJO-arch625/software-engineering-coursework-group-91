package com.group91.tars.service.ai.tool;

import com.google.gson.JsonObject;
import com.group91.tars.model.JobPosting;

public class GetJobPostingTool implements AgentTool {
    private final RecruitmentToolSupport support;

    public GetJobPostingTool(RecruitmentToolSupport support) {
        this.support = support;
    }

    public String getName() {
        return "get_job_posting";
    }

    public String getDescription() {
        return "Get a job posting by jobId, including title, module code, skills, requirements, workload, and status.";
    }

    public JsonObject getParametersSchema() {
        return support.schema("jobId");
    }

    public AgentToolResult execute(JsonObject arguments) {
        String jobId = support.requiredString(arguments, "jobId");
        if (jobId == null) {
            return AgentToolResult.failure(getName(), "Missing required argument: jobId");
        }
        if (!support.canReadJob(jobId)) {
            return AgentToolResult.failure(getName(), "Permission denied for job posting: " + jobId);
        }
        JobPosting job = support.findJob(jobId);
        if (job == null) {
            return AgentToolResult.failure(getName(), "Job posting not found: " + jobId);
        }
        JsonObject data = new JsonObject();
        data.addProperty("jobId", job.getId());
        data.addProperty("title", job.getTitle());
        data.addProperty("moduleCode", job.getModuleCode());
        data.addProperty("skills", job.getSkills());
        data.addProperty("requirements", job.getRequirements());
        data.addProperty("workload", job.getWorkload());
        data.addProperty("description", job.getDescription());
        data.addProperty("status", job.getStatus());
        return AgentToolResult.success(getName(), data);
    }
}
