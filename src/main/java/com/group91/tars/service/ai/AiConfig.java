package com.group91.tars.service.ai;

public class AiConfig {
    private static final String DEFAULT_BASE_URL = "https://aihubmix.com/v1";
    private static final String DEFAULT_MODEL = "gpt-4.1-mini-free";

    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final String mode;
    private final boolean toolCallingEnabled;

    public AiConfig() {
        EnvConfig env = EnvConfig.getInstance();
        this.apiKey = firstNonBlank(env.get("LLM_API_KEY"), env.get("OPENAI_API_KEY"));
        this.baseUrl = defaultIfBlank(firstNonBlank(env.get("LLM_BASE_URL"), env.get("OPENAI_BASE_URL")),
            DEFAULT_BASE_URL);
        this.model = defaultIfBlank(firstNonBlank(env.get("LLM_MODEL"), env.get("OPENAI_MODEL")),
            DEFAULT_MODEL);
        String configuredMode = defaultIfBlank(env.get("AI_MODE"), "auto").trim().toLowerCase();
        this.mode = ("llm".equals(configuredMode) || "local".equals(configuredMode)) ? configuredMode : "auto";
        this.toolCallingEnabled = "true".equalsIgnoreCase(defaultIfBlank(env.get("AI_TOOL_CALLING_ENABLED"), "false"));
    }

    public boolean isLocalOnly() {
        return "local".equals(mode);
    }

    public boolean isLlmOnly() {
        return "llm".equals(mode);
    }

    public boolean isAuto() {
        return "auto".equals(mode);
    }

    public boolean hasApiKey() {
        return !isBlank(apiKey);
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getModel() {
        return model;
    }

    public String getMode() {
        return mode;
    }

    public boolean isToolCallingEnabled() {
        return toolCallingEnabled;
    }

    private static String firstNonBlank(String first, String second) {
        return isBlank(first) ? second : first;
    }

    private static String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
