package com.group91.tars.service.ai.tool;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.group91.tars.model.JobPosting;

public class ListManagedJobsTool implements AgentTool {
    private final RecruitmentToolSupport support;

    public ListManagedJobsTool(RecruitmentToolSupport support) {
        this.support = support;
    }

    public String getName() {
        return "list_managed_jobs";
    }

    public String getDescription() {
        return "List job postings managed by the current MO. Use this to resolve module code/title references before listing applicants.";
    }

    public JsonObject getParametersSchema() {
        return support.schema();
    }

    public AgentToolResult execute(JsonObject arguments) {
        if (!support.canListManagedJobs()) {
            return AgentToolResult.failure(getName(), "Permission denied for listing managed jobs.");
        }
        JsonArray jobs = new JsonArray();
        for (JobPosting job : support.getStore().loadJobs()) {
            if (!support.canReadJob(job.getId())) {
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
            item.addProperty("status", job.getStatus());
            item.addProperty("description", job.getDescription());
            jobs.add(item);
        }
        JsonObject data = new JsonObject();
        data.add("jobs", jobs);
        data.addProperty("count", jobs.size());
        return AgentToolResult.success(getName(), data);
    }
}
