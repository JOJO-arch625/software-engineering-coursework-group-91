package com.group91.tars.service.ai;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.group91.tars.model.ApplicationRecord;
import com.group91.tars.model.JobPosting;
import com.group91.tars.model.TAProfile;
import com.group91.tars.model.WorkloadSummary;
import com.group91.tars.model.ai.AiCandidateSummary;
import com.group91.tars.model.ai.AiWorkloadAdvice;

import java.util.List;

public class HiringSupportAgent {
    private final AiConfig config;
    private final LlmClient llmClient;
    private final LocalRuleAiEngine localEngine;

    public HiringSupportAgent(AiConfig config, LlmClient llmClient, LocalRuleAiEngine localEngine) {
        this.config = config;
        this.llmClient = llmClient;
        this.localEngine = localEngine;
    }

    public AiCandidateSummary summarize(TAProfile profile, JobPosting job, ApplicationRecord application,
                                        String cvText, int acceptedJobs) {
        String applicationText = buildApplicationText(application);
        AiCandidateSummary local = localEngine.buildLocalCandidateSummary(profile, job, cvText, applicationText, acceptedJobs);

        if (config.isLocalOnly() || (config.isAuto() && !config.hasApiKey())) {
            return local;
        }
        if (config.isLlmOnly() && !config.hasApiKey()) {
            local.setSourceMode("error");
            local.setErrorMessage("AI_MODE=llm requires LLM_API_KEY.");
            return local;
        }

        try {
            JsonObject json = parseJson(llmClient.chat(systemPrompt(), userPrompt(profile, job, application, cvText, local)));
            local.setCvEvidence(getString(json, "cvEvidence", local.getCvEvidence()));
            local.setAdvice(getString(json, "advice", local.getAdvice()));
            String recommendation = getString(json, "shortlistRecommendation", local.getShortlistRecommendation());
            if (isAllowedRecommendation(recommendation)) {
                local.setShortlistRecommendation(recommendation);
            }
            local.setSourceMode("llm");
            return local;
        } catch (Exception exception) {
            if (config.isAuto()) {
                local.setSourceMode("local");
                return local;
            }
            local.setSourceMode("error");
            local.setErrorMessage(exception.getMessage());
            return local;
        }
    }

    public List<AiWorkloadAdvice> adviseWorkload(List<WorkloadSummary> summaries) {
        return localEngine.buildWorkloadAdvice(summaries);
    }

    private String systemPrompt() {
        return "You are an advisory hiring support assistant for a TA recruitment system.\n"
            + "Never accept or reject applications automatically.\n"
            + "Do not invent facts not present in the profile, application, job, or CV text.\n"
            + "Return concise JSON only with keys cvEvidence, advice, and shortlistRecommendation.";
    }

    private String userPrompt(TAProfile profile, JobPosting job, ApplicationRecord application, String cvText,
                              AiCandidateSummary local) {
        return "Candidate:\n"
            + "Name: " + safe(profile == null ? null : profile.getFullName()) + "\n"
            + "Profile skills: " + safe(profile == null ? null : profile.getSkills()) + "\n"
            + "Availability: " + safe(profile == null ? null : profile.getAvailability()) + "\n\n"
            + "Application:\n"
            + "Priority: " + safe(application == null ? null : application.getPriority()) + "\n"
            + "Applicant skills: " + safe(application == null ? null : application.getApplicantSkills()) + "\n"
            + "Applicant description: " + safe(application == null ? null : application.getApplicantDescription()) + "\n"
            + "Notes: " + safe(application == null ? null : application.getNotes()) + "\n\n"
            + "Job:\n"
            + "Title: " + safe(job == null ? null : job.getTitle()) + "\n"
            + "Module: " + safe(job == null ? null : job.getModuleCode()) + "\n"
            + "Required skills: " + safe(job == null ? null : job.getSkills()) + "\n"
            + "Requirements: " + safe(job == null ? null : job.getRequirements()) + "\n"
            + "Workload: " + safe(job == null ? null : job.getWorkload()) + "\n\n"
            + "Deterministic summary:\n"
            + "Score: " + local.getScore() + "\n"
            + "Matched skills: " + String.join(", ", local.getMatchedSkills()) + "\n"
            + "Missing skills: " + String.join(", ", local.getMissingSkills()) + "\n"
            + "Accepted jobs: " + local.getAcceptedJobs() + "\n"
            + "Workload risk: " + local.getWorkloadRisk() + "\n"
            + "Local recommendation: " + local.getShortlistRecommendation() + "\n\n"
            + "Extracted PDF CV text:\n" + safe(cvText);
    }

    private String buildApplicationText(ApplicationRecord application) {
        if (application == null) {
            return "";
        }
        return safe(application.getApplicantSkills()) + " "
            + safe(application.getApplicantDescription()) + " "
            + safe(application.getNotes());
    }

    private JsonObject parseJson(String content) {
        String trimmed = content == null ? "{}" : content.trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end >= start) {
            trimmed = trimmed.substring(start, end + 1);
        }
        return new JsonParser().parse(trimmed).getAsJsonObject();
    }

    private String getString(JsonObject json, String key, String fallback) {
        if (json != null && json.has(key) && !json.get(key).isJsonNull()) {
            String value = json.get(key).getAsString();
            if (!isBlank(value)) {
                return value;
            }
        }
        return fallback;
    }

    private boolean isAllowedRecommendation(String value) {
        return "Strong".equals(value) || "Consider".equals(value) || "Weak".equals(value);
    }

    private String safe(String value) {
        return isBlank(value) ? "-" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
