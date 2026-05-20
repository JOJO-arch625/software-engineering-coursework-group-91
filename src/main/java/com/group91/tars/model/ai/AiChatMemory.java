package com.group91.tars.model.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AiChatMemory implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int MAX_MESSAGES = 10;

    private final List<AiChatMessage> messages = new ArrayList<AiChatMessage>();
    private String latestToolTraceJson = "[]";
    private String role;
    private String userId;

    public void ensureOwner(String nextRole, String nextUserId) {
        if (role == null && userId == null) {
            role = nextRole;
            userId = nextUserId;
            return;
        }
        if (!same(role, nextRole) || !same(userId, nextUserId)) {
            clear();
            role = nextRole;
            userId = nextUserId;
        }
    }

    public void addMessage(String role, String content) {
        if (content == null || content.trim().isEmpty()) {
            return;
        }
        messages.add(new AiChatMessage(role, content, now()));
        trimMessages();
    }

    public void setLatestToolTrace(List<JsonObject> trace) {
        JsonArray array = new JsonArray();
        if (trace == null) {
            latestToolTraceJson = array.toString();
            return;
        }
        for (JsonObject item : trace) {
            if (item != null) {
                array.add(item);
            }
        }
        latestToolTraceJson = array.toString();
    }

    public void clear() {
        messages.clear();
        latestToolTraceJson = "[]";
    }

    public JsonArray toMessagesJson() {
        JsonArray array = new JsonArray();
        for (AiChatMessage message : messages) {
            JsonObject item = new JsonObject();
            item.addProperty("role", message.getRole());
            item.addProperty("content", message.getContent());
            item.addProperty("createdAt", message.getCreatedAt());
            array.add(item);
        }
        return array;
    }

    public JsonArray toToolTraceJson() {
        try {
            return new JsonParser().parse(latestToolTraceJson).getAsJsonArray();
        } catch (RuntimeException exception) {
            return new JsonArray();
        }
    }

    private void trimMessages() {
        while (messages.size() > MAX_MESSAGES) {
            messages.remove(0);
        }
    }

    private String now() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(new Date());
    }

    private boolean same(String left, String right) {
        if (left == null) {
            return right == null;
        }
        return left.equals(right);
    }
}
