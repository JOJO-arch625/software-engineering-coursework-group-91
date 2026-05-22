package com.group91.tars.service.ai.tool;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.group91.tars.model.JobPosting;

public class ListOpenJobsTool implements AgentTool {
    private final RecruitmentToolSupport support;

    public ListOpenJobsTool(RecruitmentToolSupport support) {
        this.support = support;
    }

    public String getName() {
        return "list_open_jobs";
    }

    public String getDescription() {
        return "List open job postings available to TA applicants. Use this for TA self-service job discovery.";
    }

    public JsonObject getParametersSchema() {
        return support.schema();
    }

    public AgentToolResult execute(JsonObject arguments) {
        if (!support.canListOpenJobs()) {
            return AgentToolResult.failure(getName(), "Permission denied for listing open jobs.");
        }
        JsonArray jobs = new JsonArray();
        for (JobPosting job : support.getStore().loadJobs()) {
            if (!"Open".equals(job.getStatus())) {
                continue;
            }
            JsonObject item = new JsonObject();
            item.addProperty("jobId", job.getId());
            item.addProperty("title", job.getTitle());
            item.addProperty("moduleCode", job.getModuleCode());
            item.addProperty("skills", job.getSkills());
            item.addProperty("requirements", job.getRequirements());
            item.addProperty("workload", job.getWorkload());
            item.addProperty("deadline", job.getDeadline());
            item.addProperty("description", job.getDescription());
            jobs.add(item);
        }
        JsonObject data = new JsonObject();
        data.add("jobs", jobs);
        data.addProperty("count", jobs.size());
        return AgentToolResult.success(getName(), data);
    }
}
