package com.group91.tars.service.ai.tool;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.group91.tars.service.ai.LlmClient;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ToolCallingAgent {
    private static final int MAX_TOOL_STEPS = 10;
    private static final Pattern TA_ID_PATTERN = Pattern.compile("\\bta-\\d+\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern JOB_ID_PATTERN = Pattern.compile("\\bjob-\\d+\\b", Pattern.CASE_INSENSITIVE);

    private final LlmClient llmClient;
    private final AgentToolRegistry toolRegistry;
    private final Gson gson = new Gson();

    public ToolCallingAgent(LlmClient llmClient, AgentToolRegistry toolRegistry) {
        this.llmClient = llmClient;
        this.toolRegistry = toolRegistry;
    }

    public ToolCallingResult run(String task, JsonObject context) {
        List<JsonObject> trace = new ArrayList<JsonObject>();
        List<JsonObject> observations = new ArrayList<JsonObject>();

        for (int step = 0; step < MAX_TOOL_STEPS; step++) {
            try {
                JsonObject requiredToolCall = nextRequiredToolCall(task, context, observations);
                if (requiredToolCall != null) {
                    executeToolCall(requiredToolCall, observations, trace);
                    continue;
                }

                JsonObject response = requestAgentResponse(task, context, observations);
                if (!response.has("type")) {
                    return ToolCallingResult.error("invalid_llm_response: missing type", trace);
                }

                String type = response.get("type").getAsString();
                if ("final".equals(type)) {
                    JsonObject result = response.has("result") && response.get("result").isJsonObject()
                        ? response.getAsJsonObject("result")
                        : new JsonObject();
                    completeFinalJson(result, observations);
                    result.addProperty("sourceMode", "llm_tool");
                    return ToolCallingResult.finalResult(result, trace);
                }

                if ("tool_call".equals(type)) {
                    if (!response.has("tool")) {
                        return ToolCallingResult.error("invalid_llm_response: missing tool name", trace);
                    }
                    String toolName = response.get("tool").getAsString();
                    JsonObject arguments = response.has("arguments") && response.get("arguments").isJsonObject()
                        ? response.getAsJsonObject("arguments")
                        : new JsonObject();
                    executeToolCall(toolName, arguments, observations, trace);
                    continue;
                }

                return ToolCallingResult.error("invalid_llm_response: unknown type " + type, trace);
            } catch (Exception exception) {
                return ToolCallingResult.error("tool_calling_agent_failed: " + exception.getMessage(), trace);
            }
        }

        return ToolCallingResult.error("tool_call_limit_exceeded", trace);
    }

    private void executeToolCall(JsonObject toolCall, List<JsonObject> observations, List<JsonObject> trace) {
        String toolName = toolCall.get("tool").getAsString();
        JsonObject arguments = toolCall.has("arguments") && toolCall.get("arguments").isJsonObject()
            ? toolCall.getAsJsonObject("arguments")
            : new JsonObject();
        executeToolCall(toolName, arguments, observations, trace);
    }

    private void executeToolCall(
        String toolName,
        JsonObject arguments,
        List<JsonObject> observations,
        List<JsonObject> trace
    ) {
        AgentToolResult toolResult = toolRegistry.execute(toolName, arguments);
        JsonObject observation = toolResult.toJson();
        observation.add("arguments", arguments);
        observations.add(observation);
        trace.add(sanitizeTrace(observation));
    }

    private JsonObject nextRequiredToolCall(String task, JsonObject context, List<JsonObject> observations) {
        String taId = resolveId(task, context, "taId", "linkedId", TA_ID_PATTERN);
        String jobId = resolveId(task, context, "jobId", null, JOB_ID_PATTERN);
        boolean fitQuestion = asksAboutFit(task);
        boolean cvQuestion = asksAboutCv(task) || fitQuestion;
        boolean jobDiscoveryQuestion = asksAboutJobDiscovery(task);
        boolean workloadQuestion = asksAboutWorkload(task) || jobDiscoveryQuestion;

        if (taId != null && !hasObservation(observations, "get_ta_profile")) {
            return toolCall("get_ta_profile", "taId", taId);
        }
        if (jobDiscoveryQuestion && !hasObservation(observations, "list_open_jobs")) {
            return emptyToolCall("list_open_jobs");
        }
        if (jobId != null && !hasObservation(observations, "get_job_posting")) {
            return toolCall("get_job_posting", "jobId", jobId);
        }
        if (taId != null && cvQuestion && !hasObservation(observations, "extract_cv_text")) {
            return toolCall("extract_cv_text", "taId", taId);
        }
        if (taId != null && jobId != null && fitQuestion && !hasObservation(observations, "calculate_fit_score")) {
            return fitScoreToolCall(taId, jobId, observations);
        }
        if (taId != null && jobDiscoveryQuestion && fitQuestion) {
            String nextOpenJobId = nextOpenJobWithoutFit(observations);
            if (nextOpenJobId != null) {
                return fitScoreToolCall(taId, nextOpenJobId, observations);
            }
        }
        if (taId != null && workloadQuestion && !hasObservation(observations, "get_workload_status")) {
            return toolCall("get_workload_status", "taId", taId);
        }
        return null;
    }

    private boolean asksAboutFit(String task) {
        String lower = normalizeTask(task);
        return lower.contains("fit")
            || lower.contains("score")
            || lower.contains("missing skill")
            || lower.contains("missing skills")
            || lower.contains("skill")
            || lower.contains("suitable")
            || lower.contains("suitability")
            || lower.contains("analyze")
            || lower.contains("analysis")
            || lower.contains("match")
            || lower.contains("适合")
            || lower.contains("匹配")
            || lower.contains("技能")
            || lower.contains("分析");
    }

    private boolean asksAboutCv(String task) {
        String lower = normalizeTask(task);
        return lower.contains("cv")
            || lower.contains("resume")
            || lower.contains("evidence")
            || lower.contains("pdf")
            || lower.contains("简历")
            || lower.contains("证据");
    }

    private boolean asksAboutWorkload(String task) {
        String lower = normalizeTask(task);
        return lower.contains("workload")
            || lower.contains("risk")
            || lower.contains("capacity")
            || lower.contains("accepted job")
            || lower.contains("apply limit")
            || lower.contains("工作量")
            || lower.contains("负荷")
            || lower.contains("风险")
            || lower.contains("还能申请")
            || lower.contains("申请几个");
    }

    private boolean asksAboutJobDiscovery(String task) {
        String lower = normalizeTask(task);
        return lower.contains("open job")
            || lower.contains("open jobs")
            || lower.contains("available job")
            || lower.contains("available jobs")
            || lower.contains("which job")
            || lower.contains("which jobs")
            || lower.contains("jobs fit")
            || lower.contains("job fit")
            || lower.contains("apply for")
            || lower.contains("my profile")
            || lower.contains("岗位")
            || lower.contains("开放岗位")
            || lower.contains("申请")
            || lower.contains("推荐")
            || lower.contains("我的资料")
            || lower.contains("我的技能")
            || lower.contains("适合我")
            || lower.contains("匹配我");
    }

    private String normalizeTask(String task) {
        return task == null ? "" : task.toLowerCase();
    }

    private String resolveId(
        String task,
        JsonObject context,
        String primaryContextKey,
        String fallbackContextKey,
        Pattern pattern
    ) {
        String fromContext = getString(context, primaryContextKey, null);
        if (fromContext == null && fallbackContextKey != null) {
            fromContext = getString(context, fallbackContextKey, null);
        }
        if (fromContext != null && pattern.matcher(fromContext).matches()) {
            return fromContext.toLowerCase();
        }
        Matcher matcher = pattern.matcher(task == null ? "" : task);
        if (matcher.find()) {
            return matcher.group().toLowerCase();
        }
        String fromMemory = memoryText(context);
        matcher = pattern.matcher(fromMemory);
        return matcher.find() ? matcher.group().toLowerCase() : null;
    }

    private String memoryText(JsonObject context) {
        if (context == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        if (context.has("recentMessages")) {
            builder.append(gson.toJson(context.get("recentMessages"))).append(' ');
        }
        if (context.has("latestToolTrace")) {
            builder.append(gson.toJson(context.get("latestToolTrace")));
        }
        return builder.toString();
    }

    private JsonObject toolCall(String toolName, String argumentName, String argumentValue) {
        JsonObject arguments = new JsonObject();
        arguments.addProperty(argumentName, argumentValue);
        JsonObject call = new JsonObject();
        call.addProperty("tool", toolName);
        call.add("arguments", arguments);
        return call;
    }

    private JsonObject fitScoreToolCall(String taId, String jobId, List<JsonObject> observations) {
        JsonObject arguments = new JsonObject();
        arguments.addProperty("taId", taId);
        arguments.addProperty("jobId", jobId);
        String cvText = cvTextFromObservation(observations);
        if (!cvText.isEmpty()) {
            arguments.addProperty("cvText", cvText);
        }
        JsonObject call = new JsonObject();
        call.addProperty("tool", "calculate_fit_score");
        call.add("arguments", arguments);
        return call;
    }

    private String nextOpenJobWithoutFit(List<JsonObject> observations) {
        JsonObject openJobs = dataForTool(observations, "list_open_jobs");
        if (openJobs == null || !openJobs.has("jobs") || !openJobs.get("jobs").isJsonArray()) {
            return null;
        }
        JsonArray jobs = openJobs.getAsJsonArray("jobs");
        for (JsonElement element : jobs) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject job = element.getAsJsonObject();
            String jobId = getString(job, "jobId", null);
            if (jobId != null && !hasFitObservationForJob(observations, jobId)) {
                return jobId;
            }
        }
        return null;
    }

    private boolean hasFitObservationForJob(List<JsonObject> observations, String jobId) {
        for (JsonObject observation : observations) {
            if (observation == null || !"calculate_fit_score".equals(getString(observation, "tool", ""))) {
                continue;
            }
            if (observation.has("arguments") && observation.get("arguments").isJsonObject()) {
                JsonObject arguments = observation.getAsJsonObject("arguments");
                if (jobId.equals(getString(arguments, "jobId", ""))) {
                    return true;
                }
            }
        }
        return false;
    }

    private JsonObject emptyToolCall(String toolName) {
        JsonObject call = new JsonObject();
        call.addProperty("tool", toolName);
        call.add("arguments", new JsonObject());
        return call;
    }

    private boolean hasObservation(List<JsonObject> observations, String toolName) {
        for (JsonObject observation : observations) {
            if (observation != null && toolName.equals(getString(observation, "tool", ""))) {
                return true;
            }
        }
        return false;
    }

    private String cvTextFromObservation(List<JsonObject> observations) {
        JsonObject cv = dataForTool(observations, "extract_cv_text");
        return cv == null ? "" : getString(cv, "text", "");
    }

    private String systemPrompt() {
        return "You are a tool-calling AI assistant for a TA recruitment system.\n"
            + "You may call only the tools listed in the tool catalog.\n"
            + "Do not invent facts that are not present in tool results.\n"
            + "Do not accept or reject applications automatically.\n"
            + "If evidence is insufficient, return a final error object.\n"
            + "Separate evidence sources carefully: TA profile, job posting, CV text, fit-score tool, and workload tool.\n"
            + "Do not write that the CV confirms a skill unless the CV text observation itself clearly supports it. "
            + "If a skill appears only in the TA profile, say the TA profile indicates it.\n"
            + "Use recent memory only to resolve follow-up references such as he, this TA, or that job.\n"
            + "If memory conflicts with current tool results, trust current tool results.\n"
            + "Do not treat memory as evidence for CV, workload, job status, or application status.\n"
            + "Respect the roleInstruction in context. TA users are self-service applicants; do not frame TA requests as MO/Admin candidate review.\n"
            + "Return exactly one strict JSON object and no surrounding prose or Markdown.\n"
            + "For tool calls, return {\"type\":\"tool_call\",\"tool\":\"...\",\"arguments\":{}}.\n"
            + "For final answers, return {\"type\":\"final\",\"result\":{\"score\":0,"
            + "\"matchedSkills\":[],\"missingSkills\":[],\"workloadRisk\":\"unknown\","
            + "\"workloadMessage\":\"\",\"cvEvidence\":\"\",\"evidenceAttribution\":{},"
            + "\"analysis\":\"...\"}}.";
    }

    private String buildPrompt(String task, JsonObject context, List<JsonObject> observations) {
        return "Task:\n" + task + "\n\n"
            + "Context:\n" + gson.toJson(context == null ? new JsonObject() : context) + "\n\n"
            + "Role instruction:\n" + getString(context, "roleInstruction", "Use role-scoped recruitment assistance.") + "\n\n"
            + "Available tools:\n" + gson.toJson(toolRegistry.describeTools()) + "\n\n"
            + "Previous observations:\n" + gson.toJson(observations) + "\n\n"
            + "Recent conversation memory:\n" + gson.toJson(context == null ? new JsonObject() : context.get("recentMessages")) + "\n\n"
            + "Latest tool observations from previous turn:\n" + gson.toJson(context == null ? new JsonObject() : context.get("latestToolTrace")) + "\n\n"
            + "Final JSON must include score, matchedSkills, missingSkills, workloadRisk, workloadMessage, "
            + "cvEvidence, evidenceAttribution, analysis, and sourceMode when relevant. "
            + "In evidenceAttribution, separate profileEvidence, jobEvidence, cvEvidence, fitScoreEvidence, "
            + "and workloadEvidence. Output JSON only.";
    }

    private JsonObject requestAgentResponse(String task, JsonObject context, List<JsonObject> observations)
        throws IOException {
        String content = llmClient.chat(systemPrompt(), buildPrompt(task, context, observations));
        try {
            return normalizeResponse(parseJson(content));
        } catch (RuntimeException parseException) {
            String repaired = llmClient.chat(repairSystemPrompt(), repairPrompt(task, context, observations, content, parseException));
            try {
                return normalizeResponse(parseJson(repaired));
            } catch (RuntimeException repairException) {
                return finalResponseFromText(repaired, parseException, repairException);
            }
        }
    }

    private String repairSystemPrompt() {
        return "You repair invalid tool-agent responses for a TA recruitment system.\n"
            + "Return exactly one strict JSON object and no Markdown.\n"
            + "Allowed schemas are {\"type\":\"tool_call\",\"tool\":\"...\",\"arguments\":{}} "
            + "or {\"type\":\"final\",\"result\":{\"score\":0,\"matchedSkills\":[],"
            + "\"missingSkills\":[],\"workloadRisk\":\"unknown\",\"workloadMessage\":\"\","
            + "\"cvEvidence\":\"\",\"evidenceAttribution\":{},\"analysis\":\"...\"}}.\n"
            + "Do not add facts. If the invalid response is plain explanatory text, wrap it as final.result.analysis.";
    }

    private String repairPrompt(
        String task,
        JsonObject context,
        List<JsonObject> observations,
        String invalidContent,
        RuntimeException parseException
    ) {
        return "The previous model response could not be parsed as strict JSON.\n\n"
            + "Parse error:\n" + compactError(parseException.getMessage()) + "\n\n"
            + "Original task:\n" + task + "\n\n"
            + "Context:\n" + gson.toJson(context == null ? new JsonObject() : context) + "\n\n"
            + "Available tools:\n" + gson.toJson(toolRegistry.describeTools()) + "\n\n"
            + "Previous observations:\n" + gson.toJson(observations) + "\n\n"
            + "Invalid response:\n" + safeSnippet(invalidContent, 3000) + "\n\n"
            + "Return only the corrected JSON object.";
    }

    private JsonObject parseJson(String content) {
        String trimmed = content == null ? "{}" : content.trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end >= start) {
            trimmed = trimmed.substring(start, end + 1);
        }
        try {
            return new JsonParser().parse(trimmed).getAsJsonObject();
        } catch (RuntimeException strictException) {
            JsonReader reader = new JsonReader(new StringReader(trimmed));
            reader.setLenient(true);
            return new JsonParser().parse(reader).getAsJsonObject();
        }
    }

    private JsonObject normalizeResponse(JsonObject response) {
        if (response == null || response.has("type")) {
            return response;
        }
        if (response.has("tool")) {
            JsonObject wrapped = new JsonObject();
            wrapped.addProperty("type", "tool_call");
            wrapped.add("tool", response.get("tool"));
            wrapped.add("arguments", response.has("arguments") && response.get("arguments").isJsonObject()
                ? response.getAsJsonObject("arguments")
                : new JsonObject());
            return wrapped;
        }
        if (response.has("result") && response.get("result").isJsonObject()) {
            JsonObject wrapped = new JsonObject();
            wrapped.addProperty("type", "final");
            wrapped.add("result", response.getAsJsonObject("result"));
            return wrapped;
        }
        JsonObject wrapped = new JsonObject();
        wrapped.addProperty("type", "final");
        wrapped.add("result", response);
        return wrapped;
    }

    private JsonObject finalResponseFromText(
        String repaired,
        RuntimeException parseException,
        RuntimeException repairException
    ) {
        JsonObject result = new JsonObject();
        String text = safeSnippet(repaired, 1200);
        if (text.isEmpty()) {
            text = "The model returned an empty non-JSON response.";
        }
        result.addProperty("analysis", text);
        result.addProperty("formatWarning", "LLM response was not valid JSON after repair. "
            + "Initial parse: " + compactError(parseException.getMessage())
            + "; repair parse: " + compactError(repairException.getMessage()));

        JsonObject wrapped = new JsonObject();
        wrapped.addProperty("type", "final");
        wrapped.add("result", result);
        return wrapped;
    }

    private void completeFinalJson(JsonObject result, List<JsonObject> observations) {
        if (result == null || observations == null) {
            return;
        }
        JsonArray jobFitScores = buildJobFitScores(observations);
        if (jobFitScores.size() > 1) {
            result.remove("score");
            result.remove("matchedSkills");
            result.remove("missingSkills");
            result.add("jobFitScores", jobFitScores);
        } else if (jobFitScores.size() == 1) {
            JsonObject fit = jobFitScores.get(0).getAsJsonObject();
            addAuthoritative(result, "score", fit.get("score"));
            addAuthoritative(result, "matchedSkills", fit.get("matchedSkills"));
            addAuthoritative(result, "missingSkills", fit.get("missingSkills"));
        }

        JsonObject workload = dataForTool(observations, "get_workload_status");
        if (workload != null) {
            addAuthoritative(result, "acceptedJobs", workload.get("acceptedJobs"));
            addAuthoritative(result, "workloadRisk", workload.get("risk"));
            addAuthoritative(result, "workloadMessage", workload.get("message"));
        }

        JsonObject cv = dataForTool(observations, "extract_cv_text");
        if (cv != null && !hasText(result, "cvEvidence")) {
            result.addProperty("cvEvidence", buildCvEvidence(cv));
        }

        result.add("evidenceAttribution", buildEvidenceAttribution(observations));
    }

    private JsonObject buildEvidenceAttribution(List<JsonObject> observations) {
        JsonObject attribution = new JsonObject();

        JsonObject profile = dataForTool(observations, "get_ta_profile");
        if (profile != null) {
            JsonObject profileEvidence = new JsonObject();
            addIfPresent(profileEvidence, "taId", profile.get("taId"));
            addIfPresent(profileEvidence, "fullName", profile.get("fullName"));
            addIfPresent(profileEvidence, "skills", profile.get("skills"));
            addIfPresent(profileEvidence, "availability", profile.get("availability"));
            addIfPresent(profileEvidence, "hasCv", profile.get("hasCv"));
            attribution.add("profileEvidence", profileEvidence);
        }

        JsonObject job = dataForTool(observations, "get_job_posting");
        if (job != null) {
            JsonObject jobEvidence = new JsonObject();
            addIfPresent(jobEvidence, "jobId", job.get("jobId"));
            addIfPresent(jobEvidence, "title", job.get("title"));
            addIfPresent(jobEvidence, "moduleCode", job.get("moduleCode"));
            addIfPresent(jobEvidence, "skills", job.get("skills"));
            addIfPresent(jobEvidence, "requirements", job.get("requirements"));
            addIfPresent(jobEvidence, "workload", job.get("workload"));
            attribution.add("jobEvidence", jobEvidence);
        }

        JsonObject openJobs = dataForTool(observations, "list_open_jobs");
        if (openJobs != null) {
            JsonObject openJobEvidence = new JsonObject();
            addIfPresent(openJobEvidence, "count", openJobs.get("count"));
            addIfPresent(openJobEvidence, "jobs", openJobs.get("jobs"));
            attribution.add("openJobEvidence", openJobEvidence);
        }

        JsonObject cv = dataForTool(observations, "extract_cv_text");
        if (cv != null) {
            JsonObject cvEvidence = new JsonObject();
            addIfPresent(cvEvidence, "available", cv.get("available"));
            addIfPresent(cvEvidence, "evidenceLength", cv.get("evidenceLength"));
            String text = getString(cv, "text", "");
            cvEvidence.addProperty("textPreview", safeSnippet(text, 220));
            cvEvidence.addProperty("note", "CV evidence is based only on extracted PDF text. "
                + "Do not attribute profile-only skills to the CV.");
            attribution.add("cvEvidence", cvEvidence);
        }

        JsonObject fit = dataForTool(observations, "calculate_fit_score");
        if (fit != null) {
            JsonObject fitEvidence = new JsonObject();
            addIfPresent(fitEvidence, "score", fit.get("score"));
            addIfPresent(fitEvidence, "matchedSkills", fit.get("matchedSkills"));
            addIfPresent(fitEvidence, "missingSkills", fit.get("missingSkills"));
            fitEvidence.addProperty("note", "Deterministic fit result from calculate_fit_score.");
            attribution.add("fitScoreEvidence", fitEvidence);
        }

        JsonObject workload = dataForTool(observations, "get_workload_status");
        if (workload != null) {
            JsonObject workloadEvidence = new JsonObject();
            addIfPresent(workloadEvidence, "acceptedJobs", workload.get("acceptedJobs"));
            addIfPresent(workloadEvidence, "risk", workload.get("risk"));
            addIfPresent(workloadEvidence, "message", workload.get("message"));
            attribution.add("workloadEvidence", workloadEvidence);
        }

        return attribution;
    }

    private JsonArray buildJobFitScores(List<JsonObject> observations) {
        JsonArray array = new JsonArray();
        for (JsonObject observation : observations) {
            if (observation == null || !"calculate_fit_score".equals(getString(observation, "tool", ""))) {
                continue;
            }
            if (!observation.has("data") || !observation.get("data").isJsonObject()) {
                continue;
            }
            JsonObject item = new JsonObject();
            if (observation.has("arguments") && observation.get("arguments").isJsonObject()) {
                JsonObject arguments = observation.getAsJsonObject("arguments");
                addIfPresent(item, "jobId", arguments.get("jobId"));
            }
            JsonObject data = observation.getAsJsonObject("data");
            addIfPresent(item, "score", data.get("score"));
            addIfPresent(item, "matchedSkills", data.get("matchedSkills"));
            addIfPresent(item, "missingSkills", data.get("missingSkills"));
            array.add(item);
        }
        return array;
    }

    private String buildCvEvidence(JsonObject cv) {
        if (cv == null) {
            return "No CV evidence was returned by tools.";
        }
        boolean available = cv.has("available") && !cv.get("available").isJsonNull() && cv.get("available").getAsBoolean();
        int length = cv.has("evidenceLength") && !cv.get("evidenceLength").isJsonNull()
            ? cv.get("evidenceLength").getAsInt()
            : 0;
        String preview = safeSnippet(getString(cv, "text", ""), 220);
        if (!available) {
            return "CV text was not available for this analysis.";
        }
        return "Extracted PDF CV text is available (" + length + " characters). Preview: " + preview;
    }

    private JsonObject dataForTool(List<JsonObject> observations, String toolName) {
        for (JsonObject observation : observations) {
            if (observation == null || !toolName.equals(getString(observation, "tool", ""))) {
                continue;
            }
            if (observation.has("success")
                && !observation.get("success").isJsonNull()
                && !observation.get("success").getAsBoolean()) {
                continue;
            }
            if (observation.has("data") && observation.get("data").isJsonObject()) {
                return observation.getAsJsonObject("data");
            }
        }
        return null;
    }

    private void addIfAbsent(JsonObject target, String key, JsonElement value) {
        if (target == null || value == null || value.isJsonNull()) {
            return;
        }
        if (!target.has(key) || target.get(key).isJsonNull()) {
            target.add(key, value);
        }
    }

    private void addAuthoritative(JsonObject target, String key, JsonElement value) {
        if (target != null && value != null && !value.isJsonNull()) {
            target.add(key, value);
        }
    }

    private void addIfPresent(JsonObject target, String key, JsonElement value) {
        if (target != null && value != null && !value.isJsonNull()) {
            target.add(key, value);
        }
    }

    private boolean hasText(JsonObject json, String key) {
        return json != null
            && json.has(key)
            && !json.get(key).isJsonNull()
            && !json.get(key).getAsString().trim().isEmpty();
    }

    private String getString(JsonObject json, String key, String fallback) {
        if (json != null && json.has(key) && !json.get(key).isJsonNull()) {
            return json.get(key).getAsString();
        }
        return fallback;
    }

    private String compactError(String message) {
        if (message == null) {
            return "unknown parse error";
        }
        return safeSnippet(message.replace('\n', ' ').replace('\r', ' '), 240);
    }

    private String safeSnippet(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) + "..." : trimmed;
    }

    private JsonObject sanitizeTrace(JsonObject traceItem) {
        JsonObject sanitized = new JsonObject();
        if (traceItem == null) {
            return sanitized;
        }
        String toolName = traceItem.has("tool") && !traceItem.get("tool").isJsonNull()
            ? traceItem.get("tool").getAsString()
            : "";
        for (Map.Entry<String, JsonElement> entry : traceItem.entrySet()) {
            if ("arguments".equals(entry.getKey()) && entry.getValue().isJsonObject()) {
                sanitized.add("arguments", sanitizeArguments(entry.getValue().getAsJsonObject()));
            } else if ("data".equals(entry.getKey()) && entry.getValue().isJsonObject()) {
                sanitized.add("data", sanitizeData(toolName, entry.getValue().getAsJsonObject()));
            } else {
                sanitized.add(entry.getKey(), entry.getValue());
            }
        }
        return sanitized;
    }

    private JsonObject sanitizeData(String toolName, JsonObject data) {
        JsonObject sanitized = new JsonObject();
        if (data == null) {
            return sanitized;
        }
        for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
            if ("extract_cv_text".equals(toolName)
                && "text".equals(entry.getKey())
                && entry.getValue() != null
                && !entry.getValue().isJsonNull()) {
                String text = entry.getValue().getAsString();
                sanitized.addProperty("textPreview", text.length() > 220 ? text.substring(0, 220) + "..." : text);
                sanitized.addProperty("textLength", text.length());
            } else {
                sanitized.add(entry.getKey(), entry.getValue());
            }
        }
        return sanitized;
    }

    private JsonObject sanitizeArguments(JsonObject arguments) {
        JsonObject sanitized = new JsonObject();
        if (arguments == null) {
            return sanitized;
        }
        for (Map.Entry<String, JsonElement> entry : arguments.entrySet()) {
            if ("cvText".equals(entry.getKey()) && entry.getValue() != null && !entry.getValue().isJsonNull()) {
                String text = entry.getValue().getAsString();
                sanitized.addProperty("cvTextPreview", text.length() > 180 ? text.substring(0, 180) + "..." : text);
                sanitized.addProperty("cvTextLength", text.length());
            } else {
                sanitized.add(entry.getKey(), entry.getValue());
            }
        }
        return sanitized;
    }
}
