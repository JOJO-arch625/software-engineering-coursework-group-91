package com.group91.tars.service.ai.tool;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.group91.tars.model.ApplicationRecord;
import com.group91.tars.model.TAProfile;

public class ListJobApplicantsTool implements AgentTool {
    private final RecruitmentToolSupport support;

    public ListJobApplicantsTool(RecruitmentToolSupport support) {
        this.support = support;
    }

    public String getName() {
        return "list_job_applicants";
    }

    public String getDescription() {
        return "List applicants for a job posting. Intended for MO shortlist analysis.";
    }

    public JsonObject getParametersSchema() {
        return support.schema("jobId");
    }

    public AgentToolResult execute(JsonObject arguments) {
        String jobId = support.requiredString(arguments, "jobId");
        if (jobId == null) {
            return AgentToolResult.failure(getName(), "Missing required argument: jobId");
        }
        if (!support.canListApplicants(jobId)) {
            return AgentToolResult.failure(getName(), "Permission denied for job applicants: " + jobId);
        }
        JsonArray applicants = new JsonArray();
        for (ApplicationRecord application : support.getStore().loadApplications()) {
            if (!jobId.equals(application.getJobId())) {
                continue;
            }
            JsonObject item = new JsonObject();
            item.addProperty("taId", application.getTaId());
            item.addProperty("applicationId", application.getId());
            item.addProperty("status", application.getStatus());
            item.addProperty("priority", application.getPriority());
            item.addProperty("notes", application.getNotes());
            item.addProperty("submittedAt", application.getSubmittedAt());
            item.addProperty("applicantSkills", application.getApplicantSkills());
            item.addProperty("applicantDescription", application.getApplicantDescription());
            TAProfile profile = support.findProfile(application.getTaId());
            if (profile != null && support.canReadTa(profile.getId())) {
                item.addProperty("fullName", profile.getFullName());
                item.addProperty("skills", profile.getSkills());
                item.addProperty("availability", profile.getAvailability());
                item.addProperty("hasCv", profile.getCvPath() != null && !profile.getCvPath().trim().isEmpty());
            }
            applicants.add(item);
        }
        JsonObject data = new JsonObject();
        data.addProperty("jobId", jobId);
        data.add("applicants", applicants);
        return AgentToolResult.success(getName(), data);
    }
}
