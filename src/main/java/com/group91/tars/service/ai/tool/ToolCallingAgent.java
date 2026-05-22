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
    private static final int MAX_TOOL_STEPS = 16;
    private static final Pattern TA_ID_PATTERN = Pattern.compile("\\bta-\\d+\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern JOB_ID_PATTERN = Pattern.compile("\\bjob-\\d+\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern MODULE_CODE_PATTERN = Pattern.compile("\\b[A-Z]{2,}\\d{4}\\b", Pattern.CASE_INSENSITIVE);

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
                    JsonObject missingRequiredTool = finalGuardToolCall(task, context, observations, result);
                    if (missingRequiredTool != null) {
                        executeToolCall(missingRequiredTool, observations, trace);
                        continue;
                    }
                    completeFinalJson(task, context, result, observations);
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
        String taId = resolveTaId(task, context, observations);
        String jobId = resolveJobId(task, context, observations);
        boolean fitQuestion = asksAboutFit(task);
        boolean cvQuestion = asksAboutCv(task) || fitQuestion;
        boolean jobDiscoveryQuestion = asksAboutJobDiscovery(task);
        boolean applicantQuestion = asksAboutApplicants(task);
        boolean jobInfoQuestion = asksAboutJobInfo(task);
        boolean workloadQuestion = asksAboutWorkload(task) || jobDiscoveryQuestion;
        boolean taRole = isRole(context, "TA");
        boolean moRole = isRole(context, "MO");
        boolean adminRole = isRole(context, "ADMIN");
        boolean cvOnlyQuestion = asksAboutCv(task)
            && !hasExplicitJobReference(task)
            && !applicantQuestion
            && !jobDiscoveryQuestion
            && !asksAboutWorkload(task);

        if (cvOnlyQuestion) {
            fitQuestion = false;
            jobInfoQuestion = false;
            workloadQuestion = false;
            jobId = null;
        }

        if (taId != null && !hasToolObservationForArgument(observations, "get_ta_profile", "taId", taId)) {
            return toolCall("get_ta_profile", "taId", taId);
        }
        if (taRole && jobDiscoveryQuestion && !hasObservation(observations, "list_open_jobs")) {
            return emptyToolCall("list_open_jobs");
        }
        if ((moRole || adminRole) && (applicantQuestion || jobInfoQuestion)
            && (jobId == null || hasExplicitJobReference(task))
            && !hasObservation(observations, "list_managed_jobs")) {
            return emptyToolCall("list_managed_jobs");
        }

        jobId = cvOnlyQuestion ? null : resolveJobId(task, context, observations);
        JsonObject applicantListCall = requiredApplicantListToolCall(task, context, observations, jobId);
        if (applicantListCall != null) {
            return applicantListCall;
        }
        if (jobId != null && (jobInfoQuestion || applicantQuestion || fitQuestion)
            && !hasToolObservationForArgument(observations, "get_job_posting", "jobId", jobId)) {
            return toolCall("get_job_posting", "jobId", jobId);
        }
        if (jobId != null && applicantQuestion) {
            String nextApplicantTaId = nextApplicantWithoutCvObservation(observations, jobId);
            if (nextApplicantTaId != null) {
                return toolCall("extract_cv_text", "taId", nextApplicantTaId);
            }
        }
        if ((moRole || adminRole) && applicantQuestion && jobId == null) {
            String nextManagedJobId = nextManagedJobWithoutApplicants(observations);
            if (nextManagedJobId != null) {
                return toolCall("list_job_applicants", "jobId", nextManagedJobId);
            }
        }
        if (taId != null && cvQuestion && !hasToolObservationForArgument(observations, "extract_cv_text", "taId", taId)) {
            return toolCall("extract_cv_text", "taId", taId);
        }
        if (taId != null && jobId != null && fitQuestion && !hasFitObservationForJob(observations, jobId)) {
            return fitScoreToolCall(taId, jobId, observations);
        }
        if (taId != null && jobDiscoveryQuestion && fitQuestion) {
            String nextOpenJobId = nextOpenJobWithoutFit(observations);
            if (nextOpenJobId != null) {
                return fitScoreToolCall(taId, nextOpenJobId, observations);
            }
        }
        if (taId != null && workloadQuestion && !hasToolObservationForArgument(observations, "get_workload_status", "taId", taId)) {
            return toolCall("get_workload_status", "taId", taId);
        }
        return null;
    }

    private String resolveTaId(String task, JsonObject context, List<JsonObject> observations) {
        String direct = resolveIdFromContextOrTask(task, context, "taId", null, TA_ID_PATTERN);
        if (direct != null) {
            return direct;
        }
        if (isRole(context, "TA")) {
            String linkedId = getString(context, "linkedId", null);
            if (linkedId != null && TA_ID_PATTERN.matcher(linkedId).matches()) {
                return linkedId.toLowerCase();
            }
        }
        String byName = resolveTaIdFromApplicantLists(task, observations);
        if (byName != null) {
            return byName;
        }
        byName = resolveTaIdFromLatestToolTrace(task, context);
        if (byName != null) {
            return byName;
        }
        if (hasExplicitPersonReference(task)) {
            return null;
        }
        String fromMemory = memoryText(context);
        Matcher matcher = TA_ID_PATTERN.matcher(fromMemory);
        return matcher.find() ? matcher.group().toLowerCase() : null;
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
            || lower.contains("\u9002\u5408")
            || lower.contains("\u5339\u914d")
            || lower.contains("\u6280\u80fd")
            || lower.contains("\u5206\u6790");
    }

    private boolean asksAboutCv(String task) {
        String lower = normalizeTask(task);
        return lower.contains("cv")
            || lower.contains("resume")
            || lower.contains("evidence")
            || lower.contains("pdf")
            || lower.contains("\u7b80\u5386")
            || lower.contains("\u8bc1\u636e");
    }

    private boolean asksAboutWorkload(String task) {
        String lower = normalizeTask(task);
        return lower.contains("workload")
            || lower.contains("risk")
            || lower.contains("capacity")
            || lower.contains("accepted job")
            || lower.contains("apply limit")
            || lower.contains("\u5de5\u4f5c\u91cf")
            || lower.contains("\u8d1f\u8377")
            || lower.contains("\u98ce\u9669")
            || lower.contains("\u8fd8\u80fd\u7533\u8bf7")
            || lower.contains("\u7533\u8bf7\u51e0\u4e2a");
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
            || lower.contains("\u5c97\u4f4d")
            || lower.contains("\u5f00\u653e\u5c97\u4f4d")
            || lower.contains("\u7533\u8bf7")
            || lower.contains("\u63a8\u8350")
            || lower.contains("\u6211\u7684\u8d44\u6599")
            || lower.contains("\u6211\u7684\u6280\u80fd")
            || lower.contains("\u9002\u5408\u6211")
            || lower.contains("\u5339\u914d\u6211");
    }

    private boolean asksAboutApplicants(String task) {
        String lower = normalizeTask(task);
        return lower.contains("applicant")
            || lower.contains("applicants")
            || lower.contains("candidate")
            || lower.contains("candidates")
            || lower.contains("shortlist")
            || lower.contains("application")
            || lower.contains("accepted")
            || lower.contains("hired")
            || lower.contains("assigned")
            || lower.contains("selected")
            || lower.contains("who")
            || lower.contains("basic information")
            || lower.contains("basic info")
            || lower.contains("list")
            || lower.contains("\u7533\u8bf7\u4eba")
            || lower.contains("\u5019\u9009\u4eba")
            || lower.contains("\u7533\u8bf7\u8005")
            || lower.contains("\u7533\u8bf7\u5217\u8868")
            || lower.contains("\u63a8\u8350\u540d\u5355")
            || lower.contains("\u7b5b\u9009")
            || lower.contains("\u5f55\u7528")
            || lower.contains("\u5df2\u5f55\u7528")
            || lower.contains("\u5f55\u53d6")
            || lower.contains("\u51e0\u4e2a\u4eba")
            || lower.contains("\u5206\u522b\u662f\u8c01")
            || lower.contains("\u8c01")
            || lower.contains("\u57fa\u672c\u4fe1\u606f")
            || lower.contains("\u5217\u51fa")
            || lower.contains("\u540d\u5355")
            || lower.contains("\u4eba\u5458");
    }

    private boolean asksAboutJobInfo(String task) {
        String lower = normalizeTask(task);
        return lower.contains("information")
            || lower.contains("info")
            || lower.contains("detail")
            || lower.contains("details")
            || lower.contains("posting")
            || lower.contains("module")
            || lower.contains("course")
            || lower.contains("job")
            || lower.contains("ta")
            || lower.contains("\u4fe1\u606f")
            || lower.contains("\u8be6\u60c5")
            || lower.contains("\u8bfe\u7a0b")
            || lower.contains("\u5c97\u4f4d")
            || lower.contains("\u52a9\u6559");
    }

    private String normalizeTask(String task) {
        return task == null ? "" : task.toLowerCase();
    }

    private boolean isRole(JsonObject context, String role) {
        return role.equalsIgnoreCase(getString(context, "role", ""));
    }

    private boolean hasExplicitJobReference(String task) {
        String lower = normalizeTask(task);
        return JOB_ID_PATTERN.matcher(task == null ? "" : task).find()
            || MODULE_CODE_PATTERN.matcher(task == null ? "" : task).find()
            || lower.contains(" ta")
            || lower.contains("digital systems")
            || lower.contains("data analytics")
            || lower.contains("object-oriented")
            || lower.contains("object oriented")
            || lower.contains("\u52a9\u6559");
    }

    private boolean hasExplicitPersonReference(String task) {
        String value = task == null ? "" : task;
        String lower = normalizeTask(value);
        return TA_ID_PATTERN.matcher(value).find()
            || lower.contains("siyu")
            || lower.contains("chen")
            || lower.contains("yuyanchen")
            || lower.contains("ming")
            || lower.contains("lidarou")
            || lower.contains("\u7b80\u5386")
            || lower.contains("cv")
            || lower.contains("resume");
    }

    private String resolveTaIdFromApplicantLists(String task, List<JsonObject> observations) {
        String taskText = normalizeName(task);
        for (JsonObject observation : observations) {
            String taId = resolveTaIdFromToolObservation(taskText, observation);
            if (taId != null) {
                return taId;
            }
        }
        return null;
    }

    private String resolveTaIdFromLatestToolTrace(String task, JsonObject context) {
        if (context == null || !context.has("latestToolTrace") || !context.get("latestToolTrace").isJsonArray()) {
            return null;
        }
        String taskText = normalizeName(task);
        JsonArray trace = context.getAsJsonArray("latestToolTrace");
        for (JsonElement element : trace) {
            if (!element.isJsonObject()) {
                continue;
            }
            String taId = resolveTaIdFromToolObservation(taskText, element.getAsJsonObject());
            if (taId != null) {
                return taId;
            }
        }
        return null;
    }

    private String resolveTaIdFromToolObservation(String taskText, JsonObject observation) {
        if (observation == null) {
            return null;
        }
        if (!observation.has("data") || !observation.get("data").isJsonObject()) {
            return null;
        }
        JsonObject data = observation.getAsJsonObject("data");
        if ("get_ta_profile".equals(getString(observation, "tool", ""))) {
            String taId = getString(data, "taId", null);
            if (taId != null && (nameMatchesTask(taskText, getString(data, "fullName", ""))
                || nameMatchesTask(taskText, taId))) {
                return taId.toLowerCase();
            }
            return null;
        }
        if (!"list_job_applicants".equals(getString(observation, "tool", ""))) {
            return null;
        }
        if (!data.has("applicants") || !data.get("applicants").isJsonArray()) {
            return null;
        }
        JsonArray applicants = data.getAsJsonArray("applicants");
        for (JsonElement element : applicants) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject applicant = element.getAsJsonObject();
            String taId = getString(applicant, "taId", null);
            if (taId == null) {
                continue;
            }
            if (nameMatchesTask(taskText, getString(applicant, "fullName", ""))
                || nameMatchesTask(taskText, taId)) {
                return taId.toLowerCase();
            }
        }
        return null;
    }

    private boolean nameMatchesTask(String taskText, String name) {
        String normalizedName = normalizeName(name);
        if (normalizedName.isEmpty()) {
            return false;
        }
        if (taskText.contains(normalizedName)) {
            return true;
        }
        String[] parts = normalizedName.split(" ");
        int matched = 0;
        for (String part : parts) {
            if (part.length() < 2) {
                continue;
            }
            if (taskText.contains(part)) {
                matched++;
            }
        }
        return matched > 0 && matched == meaningfulNamePartCount(parts);
    }

    private int meaningfulNamePartCount(String[] parts) {
        int count = 0;
        for (String part : parts) {
            if (part.length() >= 2) {
                count++;
            }
        }
        return count;
    }

    private String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase()
            .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]+", " ")
            .replaceAll("\\s+", " ")
            .trim();
    }

    private String resolveJobId(String task, JsonObject context, List<JsonObject> observations) {
        String direct = resolveIdFromContextOrTask(task, context, "jobId", null, JOB_ID_PATTERN);
        if (direct != null) {
            return direct;
        }
        String fromManagedJobs = resolveJobIdFromJobs(task, context, observations, "list_managed_jobs");
        if (fromManagedJobs != null) {
            return fromManagedJobs;
        }
        String fromOpenJobs = resolveJobIdFromJobs(task, context, observations, "list_open_jobs");
        if (fromOpenJobs != null) {
            return fromOpenJobs;
        }
        String fromObservedJobPosting = resolveJobIdFromJobPosting(task, observations);
        if (fromObservedJobPosting != null) {
            return fromObservedJobPosting;
        }
        if (hasExplicitJobReference(task)) {
            return null;
        }
        String fromMemory = memoryText(context);
        Matcher matcher = JOB_ID_PATTERN.matcher(fromMemory);
        return matcher.find() ? matcher.group().toLowerCase() : null;
    }

    private String resolveJobIdFromJobs(
        String task,
        JsonObject context,
        List<JsonObject> observations,
        String toolName
    ) {
        JsonObject data = dataForTool(observations, toolName);
        if (data == null || !data.has("jobs") || !data.get("jobs").isJsonArray()) {
            return null;
        }
        String taskText = normalizeTask(task == null ? "" : task);
        JsonArray jobs = data.getAsJsonArray("jobs");
        if (hasExplicitJobReference(task)) {
            String fromCurrentTask = resolveJobIdFromJobArray(taskText, jobs);
            if (fromCurrentTask != null) {
                return fromCurrentTask;
            }
            return null;
        }
        String referenceText = normalizeTask(taskText + " " + memoryText(context));
        return resolveJobIdFromJobArray(referenceText, jobs);
    }

    private String resolveJobIdFromJobArray(String referenceText, JsonArray jobs) {
        for (JsonElement element : jobs) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject job = element.getAsJsonObject();
            if (jobMatchesReference(referenceText, job)) {
                return getString(job, "jobId", null);
            }
        }
        return null;
    }

    private boolean jobMatchesReference(String referenceText, JsonObject job) {
        String moduleCode = normalizeTask(getString(job, "moduleCode", ""));
        String title = normalizeTask(getString(job, "title", ""));
        String jobId = normalizeTask(getString(job, "jobId", ""));
        if (!jobId.isEmpty() && referenceText.contains(jobId)) {
            return true;
        }
        if (!moduleCode.isEmpty() && referenceText.contains(moduleCode)) {
            return true;
        }
        if (!title.isEmpty() && referenceText.contains(title)) {
            return true;
        }
        int matchedTitleWords = 0;
        for (String word : title.split("[^a-z0-9]+")) {
            if (word.length() < 3 || "the".equals(word)) {
                continue;
            }
            if (referenceText.contains(word)) {
                matchedTitleWords++;
            }
        }
        return matchedTitleWords >= 2;
    }

    private String resolveJobIdFromJobPosting(String task, List<JsonObject> observations) {
        String taskText = normalizeTask(task == null ? "" : task);
        for (JsonObject observation : observations) {
            if (observation == null || !"get_job_posting".equals(getString(observation, "tool", ""))) {
                continue;
            }
            if (!observation.has("data") || !observation.get("data").isJsonObject()) {
                continue;
            }
            JsonObject job = observation.getAsJsonObject("data");
            String jobId = getString(job, "jobId", null);
            if (jobId == null) {
                continue;
            }
            if (!hasExplicitJobReference(task) || jobMatchesReference(taskText, job)) {
                return jobId;
            }
        }
        return null;
    }

    private String resolveId(
        String task,
        JsonObject context,
        String primaryContextKey,
        String fallbackContextKey,
        Pattern pattern
    ) {
        String fromContextOrTask = resolveIdFromContextOrTask(task, context, primaryContextKey, fallbackContextKey, pattern);
        if (fromContextOrTask != null) {
            return fromContextOrTask;
        }
        String fromMemory = memoryText(context);
        Matcher matcher = pattern.matcher(fromMemory);
        return matcher.find() ? matcher.group().toLowerCase() : null;
    }

    private String resolveIdFromContextOrTask(
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
        return null;
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
        String cvText = cvTextFromObservation(observations, taId);
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

    private JsonObject finalGuardToolCall(
        String task,
        JsonObject context,
        List<JsonObject> observations,
        JsonObject pendingResult
    ) {
        String jobId = resolveJobId(task, context, observations);
        JsonObject applicantListCall = requiredApplicantListToolCall(task, context, observations, jobId);
        if (applicantListCall != null) {
            return applicantListCall;
        }
        String analysis = getString(pendingResult, "analysis", "");
        boolean mentionsMissingApplicantTool = normalizeTask(analysis).contains("list_job_applicants")
            || normalizeTask(analysis).contains("applicant data");
        return mentionsMissingApplicantTool && jobId != null && !hasApplicantsObservationForJob(observations, jobId)
            ? toolCall("list_job_applicants", "jobId", jobId)
            : null;
    }

    private JsonObject requiredApplicantListToolCall(
        String task,
        JsonObject context,
        List<JsonObject> observations,
        String jobId
    ) {
        boolean scopedReviewer = isRole(context, "MO") || isRole(context, "ADMIN");
        if (!scopedReviewer || jobId == null || !asksAboutApplicants(task)) {
            return null;
        }
        if (hasApplicantsObservationForJob(observations, jobId)) {
            return null;
        }
        return toolCall("list_job_applicants", "jobId", jobId);
    }

    private boolean hasObservation(List<JsonObject> observations, String toolName) {
        for (JsonObject observation : observations) {
            if (observation != null && toolName.equals(getString(observation, "tool", ""))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasToolObservationForArgument(
        List<JsonObject> observations,
        String toolName,
        String argumentName,
        String argumentValue
    ) {
        for (JsonObject observation : observations) {
            if (observation == null || !toolName.equals(getString(observation, "tool", ""))) {
                continue;
            }
            if (!observation.has("arguments") || !observation.get("arguments").isJsonObject()) {
                continue;
            }
            JsonObject arguments = observation.getAsJsonObject("arguments");
            if (argumentValue.equalsIgnoreCase(getString(arguments, argumentName, ""))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasApplicantsObservationForJob(List<JsonObject> observations, String jobId) {
        return hasToolObservationForArgument(observations, "list_job_applicants", "jobId", jobId);
    }

    private String nextManagedJobWithoutApplicants(List<JsonObject> observations) {
        JsonObject managedJobs = dataForTool(observations, "list_managed_jobs");
        if (managedJobs == null || !managedJobs.has("jobs") || !managedJobs.get("jobs").isJsonArray()) {
            return null;
        }
        JsonArray jobs = managedJobs.getAsJsonArray("jobs");
        for (JsonElement element : jobs) {
            if (!element.isJsonObject()) {
                continue;
            }
            String jobId = getString(element.getAsJsonObject(), "jobId", null);
            if (jobId != null && !hasApplicantsObservationForJob(observations, jobId)) {
                return jobId;
            }
        }
        return null;
    }

    private String nextApplicantWithoutCvObservation(List<JsonObject> observations, String jobId) {
        JsonObject applicantsData = dataForTool(observations, "list_job_applicants", "jobId", jobId);
        if (applicantsData == null || !applicantsData.has("applicants")
            || !applicantsData.get("applicants").isJsonArray()) {
            return null;
        }
        JsonArray applicants = applicantsData.getAsJsonArray("applicants");
        for (JsonElement element : applicants) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject applicant = element.getAsJsonObject();
            String taId = getString(applicant, "taId", null);
            if (taId != null && !hasToolObservationForArgument(observations, "extract_cv_text", "taId", taId)) {
                return taId;
            }
        }
        return null;
    }

    private String cvTextFromObservation(List<JsonObject> observations, String taId) {
        JsonObject cv = taId == null
            ? dataForTool(observations, "extract_cv_text")
            : dataForTool(observations, "extract_cv_text", "taId", taId);
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
        boolean cvOnlyQuestion = asksAboutCv(task)
            && !hasExplicitJobReference(task)
            && !asksAboutApplicants(task)
            && !asksAboutJobDiscovery(task)
            && !asksAboutWorkload(task);
        return "Task:\n" + task + "\n\n"
            + "Context:\n" + gson.toJson(context == null ? new JsonObject() : context) + "\n\n"
            + "Role instruction:\n" + getString(context, "roleInstruction", "Use role-scoped recruitment assistance.") + "\n\n"
            + "Current-task guardrail:\n"
            + (cvOnlyQuestion
                ? "This is a CV-only question. Use only current TA profile/CV observations. "
                    + "Do not discuss job fit, job postings, shortlist decisions, or workload unless the current observations include those tools.\n\n"
                : "Use current observations as the primary source of truth. Do not import job/TA facts from memory unless needed to resolve a pronoun or omitted ID.\n\n")
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

    private void completeFinalJson(
        String task,
        JsonObject context,
        JsonObject result,
        List<JsonObject> observations
    ) {
        if (result == null || observations == null) {
            return;
        }
        String targetTaId = resolveTaId(task, context, observations);
        boolean cvOnlyQuestion = asksAboutCv(task)
            && !hasExplicitJobReference(task)
            && !asksAboutApplicants(task)
            && !asksAboutJobDiscovery(task)
            && !asksAboutWorkload(task);
        String targetJobId = cvOnlyQuestion ? null : resolveJobId(task, context, observations);
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

        JsonObject workload = targetTaId == null
            ? dataForTool(observations, "get_workload_status")
            : dataForTool(observations, "get_workload_status", "taId", targetTaId);
        if (workload != null) {
            addAuthoritative(result, "acceptedJobs", workload.get("acceptedJobs"));
            addAuthoritative(result, "workloadRisk", workload.get("risk"));
            addAuthoritative(result, "workloadMessage", workload.get("message"));
        }

        JsonObject cv = targetTaId == null
            ? dataForTool(observations, "extract_cv_text")
            : dataForTool(observations, "extract_cv_text", "taId", targetTaId);
        if (cv != null && !hasText(result, "cvEvidence")) {
            result.addProperty("cvEvidence", buildCvEvidence(cv));
        }

        result.add("evidenceAttribution", buildEvidenceAttribution(observations, targetTaId, targetJobId));
    }

    private JsonObject buildEvidenceAttribution(List<JsonObject> observations, String targetTaId, String targetJobId) {
        JsonObject attribution = new JsonObject();

        JsonObject profile = targetTaId == null
            ? dataForTool(observations, "get_ta_profile")
            : dataForTool(observations, "get_ta_profile", "taId", targetTaId);
        if (profile != null) {
            JsonObject profileEvidence = new JsonObject();
            addIfPresent(profileEvidence, "taId", profile.get("taId"));
            addIfPresent(profileEvidence, "fullName", profile.get("fullName"));
            addIfPresent(profileEvidence, "skills", profile.get("skills"));
            addIfPresent(profileEvidence, "availability", profile.get("availability"));
            addIfPresent(profileEvidence, "hasCv", profile.get("hasCv"));
            attribution.add("profileEvidence", profileEvidence);
        }

        JsonObject job = targetJobId == null
            ? dataForTool(observations, "get_job_posting")
            : dataForTool(observations, "get_job_posting", "jobId", targetJobId);
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

        JsonObject managedJobs = dataForTool(observations, "list_managed_jobs");
        if (managedJobs != null) {
            JsonObject managedJobEvidence = new JsonObject();
            addIfPresent(managedJobEvidence, "count", managedJobs.get("count"));
            addIfPresent(managedJobEvidence, "jobs", managedJobs.get("jobs"));
            attribution.add("managedJobEvidence", managedJobEvidence);
        }

        JsonArray applicantEvidence = buildApplicantEvidence(observations);
        if (applicantEvidence.size() > 0) {
            attribution.add("applicantEvidence", applicantEvidence);
        }

        JsonObject cv = targetTaId == null
            ? dataForTool(observations, "extract_cv_text")
            : dataForTool(observations, "extract_cv_text", "taId", targetTaId);
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

        JsonObject workload = targetTaId == null
            ? dataForTool(observations, "get_workload_status")
            : dataForTool(observations, "get_workload_status", "taId", targetTaId);
        if (workload != null) {
            JsonObject workloadEvidence = new JsonObject();
            addIfPresent(workloadEvidence, "acceptedJobs", workload.get("acceptedJobs"));
            addIfPresent(workloadEvidence, "risk", workload.get("risk"));
            addIfPresent(workloadEvidence, "message", workload.get("message"));
            attribution.add("workloadEvidence", workloadEvidence);
        }

        return attribution;
    }

    private JsonArray buildApplicantEvidence(List<JsonObject> observations) {
        JsonArray array = new JsonArray();
        for (JsonObject observation : observations) {
            if (observation == null || !"list_job_applicants".equals(getString(observation, "tool", ""))) {
                continue;
            }
            if (!observation.has("data") || !observation.get("data").isJsonObject()) {
                continue;
            }
            JsonObject data = observation.getAsJsonObject("data");
            JsonObject item = new JsonObject();
            addIfPresent(item, "jobId", data.get("jobId"));
            addIfPresent(item, "applicants", data.get("applicants"));
            array.add(item);
        }
        return array;
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

    private JsonObject dataForTool(
        List<JsonObject> observations,
        String toolName,
        String argumentName,
        String argumentValue
    ) {
        for (JsonObject observation : observations) {
            if (observation == null || !toolName.equals(getString(observation, "tool", ""))) {
                continue;
            }
            if (!observation.has("arguments") || !observation.get("arguments").isJsonObject()) {
                continue;
            }
            JsonObject arguments = observation.getAsJsonObject("arguments");
            if (!argumentValue.equalsIgnoreCase(getString(arguments, argumentName, ""))) {
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
