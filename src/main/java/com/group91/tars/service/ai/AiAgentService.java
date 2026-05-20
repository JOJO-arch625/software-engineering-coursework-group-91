package com.group91.tars.service.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.group91.tars.model.ApplicationRecord;
import com.group91.tars.model.JobPosting;
import com.group91.tars.model.TAProfile;
import com.group91.tars.model.UserAccount;
import com.group91.tars.model.WorkloadSummary;
import com.group91.tars.model.ai.AiCandidateSummary;
import com.group91.tars.model.ai.AiChatMemory;
import com.group91.tars.model.ai.AiFitResult;
import com.group91.tars.model.ai.AiWorkloadAdvice;
import com.group91.tars.service.ai.tool.AgentToolRegistry;
import com.group91.tars.service.ai.tool.RecruitmentToolRegistryFactory;
import com.group91.tars.service.ai.tool.ToolCallingAgent;
import com.group91.tars.service.ai.tool.ToolCallingResult;

import java.util.ArrayList;
import java.util.List;

public class AiAgentService {
    private static final AiAgentService INSTANCE = new AiAgentService();

    private final AiConfig config = new AiConfig();
    private final CvTextExtractor cvTextExtractor = new CvTextExtractor();
    private final LocalRuleAiEngine localRuleAiEngine = new LocalRuleAiEngine();
    private final LlmClient llmClient = new LlmClient(config);
    private final TaFitAdvisorAgent taFitAdvisorAgent = new TaFitAdvisorAgent(config, llmClient, localRuleAiEngine);
    private final HiringSupportAgent hiringSupportAgent = new HiringSupportAgent(config, llmClient, localRuleAiEngine);
    private final RecruitmentToolRegistryFactory toolRegistryFactory =
        new RecruitmentToolRegistryFactory(cvTextExtractor, localRuleAiEngine);

    private AiAgentService() {
    }

    public static AiAgentService getInstance() {
        return INSTANCE;
    }

    public boolean isToolCallingEnabled() {
        return config.isToolCallingEnabled() && config.hasApiKey() && !config.isLocalOnly();
    }

    public AiFitResult adviseTaFit(TAProfile profile, JobPosting job, int acceptedJobs) {
        String cvText = cvTextExtractor.extract(profile);
        return taFitAdvisorAgent.advise(profile, job, cvText, acceptedJobs);
    }

    public AiFitResult adviseTaFitWithTools(String taId, String jobId) {
        return adviseTaFitWithTools(taId, jobId, null);
    }

    public AiFitResult adviseTaFitWithTools(String taId, String jobId, UserAccount currentUser) {
        if (!isToolCallingEnabled()) {
            return AiFitResult.error(0, new ArrayList<String>(), new ArrayList<String>(),
                "Tool-calling agent requires AI_TOOL_CALLING_ENABLED=true, AI_MODE not local, and LLM_API_KEY.");
        }
        JsonObject context = new JsonObject();
        context.addProperty("taId", taId);
        context.addProperty("jobId", jobId);
        ToolCallingResult result = runToolAgent(
            "Analyze TA fit for taId=" + taId + " and jobId=" + jobId
                + ". Use tools to get profile, job, CV evidence, fit score, and workload. "
                + "Return score, matchedSkills, missingSkills, workloadRisk, workloadMessage, "
                + "cvEvidence, evidenceAttribution, advice, analysis, and sourceMode. "
                + "Separate profile evidence from CV evidence; do not say the CV confirms a skill unless "
                + "the extracted CV text itself supports it.",
            context,
            currentUser
        );
        if (!result.isSuccess()) {
            return AiFitResult.error(0, new ArrayList<String>(), new ArrayList<String>(), result.getErrorMessage());
        }
        JsonObject json = result.getFinalJson();
        AiFitResult fit = new AiFitResult();
        fit.setScore(getInt(json, "score", 0));
        fit.setMatchedSkills(getStringList(json, "matchedSkills"));
        fit.setMissingSkills(getStringList(json, "missingSkills"));
        fit.setCvEvidence(getString(json, "cvEvidence", "CV evidence was not provided by the tool-calling agent."));
        fit.setAdvice(getString(json, "advice", result.getReply()));
        fit.setSourceMode("llm_tool");
        fit.setErrorMessage(getNullableString(json, "errorMessage"));
        return fit;
    }

    public AiCandidateSummary summarizeCandidate(TAProfile profile, JobPosting job, ApplicationRecord application,
                                                 int acceptedJobs) {
        String cvText = cvTextExtractor.extract(profile);
        return hiringSupportAgent.summarize(profile, job, application, cvText, acceptedJobs);
    }

    public AiCandidateSummary summarizeCandidateWithTools(String taId, String jobId, String applicationId) {
        return summarizeCandidateWithTools(taId, jobId, applicationId, null);
    }

    public AiCandidateSummary summarizeCandidateWithTools(String taId, String jobId, String applicationId,
                                                          UserAccount currentUser) {
        if (!isToolCallingEnabled()) {
            AiCandidateSummary summary = new AiCandidateSummary();
            summary.setSourceMode("error");
            summary.setErrorMessage("Tool-calling agent requires AI_TOOL_CALLING_ENABLED=true, AI_MODE not local, and LLM_API_KEY.");
            return summary;
        }
        JsonObject context = new JsonObject();
        context.addProperty("taId", taId);
        context.addProperty("jobId", jobId);
        context.addProperty("applicationId", applicationId);
        ToolCallingResult result = runToolAgent(
            "Create an advisory candidate summary for taId=" + taId + ", jobId=" + jobId
                + ", applicationId=" + applicationId + ". Use tools for profile, job, CV, fit score, and workload. "
                + "Return score, matchedSkills, missingSkills, cvEvidence, acceptedJobs, workloadRisk, "
                + "workloadMessage, evidenceAttribution, shortlistRecommendation, advice, analysis, and sourceMode. "
                + "Separate profile evidence from CV evidence; do not say the CV confirms a skill unless "
                + "the extracted CV text itself supports it. Do not accept or reject.",
            context,
            currentUser
        );
        AiCandidateSummary summary = new AiCandidateSummary();
        if (!result.isSuccess()) {
            summary.setSourceMode("error");
            summary.setErrorMessage(result.getErrorMessage());
            return summary;
        }
        JsonObject json = result.getFinalJson();
        summary.setScore(getInt(json, "score", 0));
        summary.setMatchedSkills(getStringList(json, "matchedSkills"));
        summary.setMissingSkills(getStringList(json, "missingSkills"));
        summary.setCvEvidence(getString(json, "cvEvidence", "CV evidence was not provided by the tool-calling agent."));
        summary.setAcceptedJobs(getInt(json, "acceptedJobs", 0));
        summary.setWorkloadRisk(getString(json, "workloadRisk", "low"));
        summary.setShortlistRecommendation(getString(json, "shortlistRecommendation", "Consider"));
        summary.setAdvice(getString(json, "advice", result.getReply()));
        summary.setSourceMode("llm_tool");
        summary.setErrorMessage(getNullableString(json, "errorMessage"));
        return summary;
    }

    public List<AiWorkloadAdvice> adviseWorkload(List<WorkloadSummary> summaries) {
        return hiringSupportAgent.adviseWorkload(summaries);
    }

    public ToolCallingResult chat(String userMessage, UserAccount currentUser) {
        return chat(userMessage, currentUser, null);
    }

    public ToolCallingResult chat(String userMessage, UserAccount currentUser, AiChatMemory memory) {
        if (!isToolCallingEnabled()) {
            return ToolCallingResult.error(
                "Tool-calling chat requires AI_TOOL_CALLING_ENABLED=true, AI_MODE not local, and LLM_API_KEY.",
                new ArrayList<JsonObject>()
            );
        }
        JsonObject context = new JsonObject();
        if (currentUser != null) {
            context.addProperty("userId", currentUser.getId());
            context.addProperty("role", currentUser.getRole());
            context.addProperty("linkedId", currentUser.getLinkedId());
            context.addProperty("roleInstruction", roleInstruction(currentUser));
        }
        if (memory != null) {
            context.add("recentMessages", memory.toMessagesJson());
            context.add("latestToolTrace", memory.toToolTraceJson());
            context.addProperty("memoryPolicy",
                "Use memory only to resolve follow-up references. Trust current tool results over memory.");
        }
        return runToolAgent(userMessage, context, currentUser);
    }

    private String roleInstruction(UserAccount currentUser) {
        if (currentUser == null) {
            return "Unauthenticated users cannot use recruitment tools.";
        }
        if ("TA".equals(currentUser.getRole())) {
            return "You are assisting the logged-in TA as a self-service applicant. "
                + "Use the TA's own profile and CV only. Focus on open jobs, fit, missing skills, CV evidence, "
                + "application capacity, and profile improvement. Do not evaluate other TAs or provide shortlist decisions.";
        }
        if ("MO".equals(currentUser.getRole())) {
            return "You are assisting a Module Organiser. Focus on the MO's own job postings, applicants, "
                + "candidate summaries, shortlist advice, CV evidence, and applicant workload risk. "
                + "Do not modify application status.";
        }
        if ("ADMIN".equals(currentUser.getRole())) {
            return "You are assisting an administrator. Focus on aggregate workload, staffing risk, and system oversight. "
                + "Avoid reading full CV text unless a specific permitted analysis requires it.";
        }
        return "Use role-scoped recruitment tools only.";
    }

    private ToolCallingResult runToolAgent(String task, JsonObject context, UserAccount currentUser) {
        AgentToolRegistry registry = toolRegistryFactory.create(currentUser);
        ToolCallingAgent agent = new ToolCallingAgent(llmClient, registry);
        return agent.run(task, context);
    }

    private String getString(JsonObject json, String key, String fallback) {
        String value = getNullableString(json, key);
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private String getNullableString(JsonObject json, String key) {
        if (json != null && json.has(key) && !json.get(key).isJsonNull()) {
            return json.get(key).getAsString();
        }
        return null;
    }

    private int getInt(JsonObject json, String key, int fallback) {
        if (json != null && json.has(key) && !json.get(key).isJsonNull()) {
            try {
                return json.get(key).getAsInt();
            } catch (RuntimeException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private List<String> getStringList(JsonObject json, String key) {
        List<String> values = new ArrayList<String>();
        if (json == null || !json.has(key) || !json.get(key).isJsonArray()) {
            return values;
        }
        JsonArray array = json.getAsJsonArray(key);
        for (JsonElement element : array) {
            if (!element.isJsonNull()) {
                values.add(element.getAsString());
            }
        }
        return values;
    }
}
