package com.group91.tars.service.ai;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.group91.tars.model.JobPosting;
import com.group91.tars.model.TAProfile;
import com.group91.tars.model.ai.AiFitResult;

public class TaFitAdvisorAgent {
    private final AiConfig config;
    private final LlmClient llmClient;
    private final LocalRuleAiEngine localEngine;

    public TaFitAdvisorAgent(AiConfig config, LlmClient llmClient, LocalRuleAiEngine localEngine) {
        this.config = config;
        this.llmClient = llmClient;
        this.localEngine = localEngine;
    }

    public AiFitResult advise(TAProfile profile, JobPosting job, String cvText, int acceptedJobs) {
        LocalRuleAiEngine.SkillMatch match = localEngine.evaluateFit(profile, job, cvText, null);

        if (config.isLocalOnly() || (config.isAuto() && !config.hasApiKey())) {
            return AiFitResult.local(match.getScore(), match.getMatchedSkills(), match.getMissingSkills(),
                localEngine.buildCvEvidence(match, cvText), localEngine.buildFitAdvice(match));
        }
        if (config.isLlmOnly() && !config.hasApiKey()) {
            return AiFitResult.error(match.getScore(), match.getMatchedSkills(), match.getMissingSkills(),
                "AI_MODE=llm requires LLM_API_KEY.");
        }

        try {
            JsonObject json = parseJson(llmClient.chat(systemPrompt(), userPrompt(profile, job, cvText, acceptedJobs, match)));
            return AiFitResult.llm(match.getScore(), match.getMatchedSkills(), match.getMissingSkills(),
                getString(json, "cvEvidence", localEngine.buildCvEvidence(match, cvText)),
                getString(json, "advice", localEngine.buildFitAdvice(match)));
        } catch (Exception exception) {
            if (config.isAuto()) {
                return AiFitResult.local(match.getScore(), match.getMatchedSkills(), match.getMissingSkills(),
                    localEngine.buildCvEvidence(match, cvText), localEngine.buildFitAdvice(match));
            }
            return AiFitResult.error(match.getScore(), match.getMatchedSkills(), match.getMissingSkills(),
                exception.getMessage());
        }
    }

    private String systemPrompt() {
        return "You are an advisory assistant for a TA recruitment system.\n"
            + "Do not make final hiring decisions.\n"
            + "Do not invent facts not present in the profile, job, or CV text.\n"
            + "Return concise JSON only with keys cvEvidence and advice.";
    }

    private String userPrompt(TAProfile profile, JobPosting job, String cvText, int acceptedJobs,
                              LocalRuleAiEngine.SkillMatch match) {
        return "TA profile:\n"
            + "Name: " + safe(profile == null ? null : profile.getFullName()) + "\n"
            + "Skills: " + safe(profile == null ? null : profile.getSkills()) + "\n"
            + "Availability: " + safe(profile == null ? null : profile.getAvailability()) + "\n\n"
            + "Job posting:\n"
            + "Title: " + safe(job == null ? null : job.getTitle()) + "\n"
            + "Module: " + safe(job == null ? null : job.getModuleCode()) + "\n"
            + "Skills: " + safe(job == null ? null : job.getSkills()) + "\n"
            + "Requirements: " + safe(job == null ? null : job.getRequirements()) + "\n"
            + "Workload: " + safe(job == null ? null : job.getWorkload()) + "\n"
            + "Description: " + safe(job == null ? null : job.getDescription()) + "\n\n"
            + "Deterministic result:\n"
            + "Score: " + match.getScore() + "\n"
            + "Matched skills: " + String.join(", ", match.getMatchedSkills()) + "\n"
            + "Missing skills: " + String.join(", ", match.getMissingSkills()) + "\n"
            + "Accepted jobs: " + acceptedJobs + "\n\n"
            + "Extracted PDF CV text:\n" + safe(cvText);
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

    private String safe(String value) {
        return isBlank(value) ? "-" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
