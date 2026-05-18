package com.group91.tars.service.ai;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class EnvConfig {
    private static final EnvConfig INSTANCE = new EnvConfig();

    private final Map<String, String> fileValues = new LinkedHashMap<String, String>();

    private EnvConfig() {
        loadDotEnv();
    }

    public static EnvConfig getInstance() {
        return INSTANCE;
    }

    public String get(String key) {
        String value = System.getenv(key);
        if (!isBlank(value)) {
            return value;
        }
        return fileValues.get(key);
    }

    private void loadDotEnv() {
        File file = new File(System.getProperty("user.dir"), ".env");
        if (!file.isFile()) {
            return;
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                parseLine(line);
            }
        } catch (IOException ignored) {
            // Missing or unreadable .env should not prevent the app from starting.
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                    // Nothing useful to do here.
                }
            }
        }
    }

    private void parseLine(String line) {
        if (line == null) {
            return;
        }
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return;
        }
        int separator = trimmed.indexOf('=');
        if (separator <= 0) {
            return;
        }
        String key = trimmed.substring(0, separator).trim();
        String value = trimmed.substring(separator + 1).trim();
        if ((value.startsWith("\"") && value.endsWith("\""))
            || (value.startsWith("'") && value.endsWith("'"))) {
            value = value.substring(1, value.length() - 1);
        }
        if (!key.isEmpty()) {
            fileValues.put(key, value);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
