package com.group91.tars.service.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LlmClient {
    private static final int TIMEOUT_MS = 15000;

    private final AiConfig config;
    private final Gson gson = new Gson();

    public LlmClient(AiConfig config) {
        this.config = config;
    }

    public String chat(String systemPrompt, String userPrompt) throws IOException {
        URL url = new URL(trimTrailingSlash(config.getBaseUrl()) + "/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(TIMEOUT_MS);
        connection.setReadTimeout(TIMEOUT_MS);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        byte[] body = gson.toJson(buildRequestBody(systemPrompt, userPrompt)).getBytes(StandardCharsets.UTF_8);
        OutputStream outputStream = connection.getOutputStream();
        try {
            outputStream.write(body);
        } finally {
            outputStream.close();
        }

        int status = connection.getResponseCode();
        InputStream responseStream = status >= 200 && status < 300
            ? connection.getInputStream()
            : connection.getErrorStream();
        String responseBody = readFully(responseStream);
        if (status < 200 || status >= 300) {
            throw new IOException("LLM request failed with HTTP " + status + ": " + responseBody);
        }
        return extractContent(responseBody);
    }

    private Map<String, Object> buildRequestBody(String systemPrompt, String userPrompt) {
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("model", config.getModel());

        List<Map<String, String>> messages = new ArrayList<Map<String, String>>();
        messages.add(message("system", systemPrompt));
        messages.add(message("user", userPrompt));
        body.put("messages", messages);
        body.put("temperature", 0.2);
        return body;
    }

    private Map<String, String> message(String role, String content) {
        Map<String, String> message = new LinkedHashMap<String, String>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private String extractContent(String responseBody) throws IOException {
        JsonObject root = new JsonParser().parse(responseBody).getAsJsonObject();
        JsonArray choices = root.getAsJsonArray("choices");
        if (choices == null || choices.size() == 0) {
            throw new IOException("LLM response did not include choices.");
        }
        JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
        if (message == null || !message.has("content")) {
            throw new IOException("LLM response did not include message content.");
        }
        return message.get("content").getAsString();
    }

    private String readFully(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        try {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
            return builder.toString();
        } finally {
            reader.close();
        }
    }

    private String trimTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
