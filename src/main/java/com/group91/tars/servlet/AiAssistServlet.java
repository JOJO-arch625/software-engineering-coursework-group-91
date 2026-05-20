package com.group91.tars.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.group91.tars.model.UserAccount;
import com.group91.tars.model.ai.AiChatMemory;
import com.group91.tars.service.ai.tool.ToolCallingResult;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Renders the AI assist concept panel at {@code /ai/assist},
 * displaying AI feature status notes and guidance.
 */
@WebServlet(urlPatterns = {"/ai/assist", "/ai/assist/chat", "/ai/assist/chat/clear"})
public class AiAssistServlet extends BasePageServlet {
    private static final String AI_CHAT_MEMORY = "AI_CHAT_MEMORY";

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        if (isChatRequest(request)) {
            writeChatHealth(response);
            return;
        }
        if (!requireAuthenticated(request, response)) {
            return;
        }
        preparePage(request, "ai-assist", "Optional", "AI Assist Concept");
        request.setAttribute("aiTodos", service.getAiTodoNotes());
        forward(request, response, "/WEB-INF/jsp/ai/assist.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        if (!isChatRequest(request)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");

        UserAccount currentUser = getCurrentUser(request);
        if (currentUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonObject error = new JsonObject();
            error.addProperty("success", false);
            error.addProperty("sourceMode", "error");
            error.addProperty("errorMessage", "Please log in before using the AI Agent Workspace.");
            response.getWriter().write(gson.toJson(error));
            return;
        }

        AiChatMemory memory = getMemory(request, currentUser);
        if (isClearRequest(request)) {
            memory.clear();
            JsonObject cleared = new JsonObject();
            cleared.addProperty("success", true);
            cleared.addProperty("sourceMode", "local");
            cleared.addProperty("message", "AI chat memory cleared.");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(cleared));
            return;
        }

        JsonObject body;
        try {
            body = new JsonParser().parse(readBody(request)).getAsJsonObject();
        } catch (RuntimeException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject error = new JsonObject();
            error.addProperty("success", false);
            error.addProperty("sourceMode", "error");
            error.addProperty("errorMessage", "Invalid JSON request body.");
            response.getWriter().write(gson.toJson(error));
            return;
        }

        String message = body.has("message") && !body.get("message").isJsonNull()
            ? body.get("message").getAsString().trim()
            : "";
        if (message.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject error = new JsonObject();
            error.addProperty("success", false);
            error.addProperty("sourceMode", "error");
            error.addProperty("errorMessage", "Message is required.");
            response.getWriter().write(gson.toJson(error));
            return;
        }

        memory.addMessage("user", message);
        ToolCallingResult result = service.chatWithAiAgent(message, currentUser, memory);
        memory.addMessage("assistant", result.getReply());
        memory.setLatestToolTrace(result.getToolTrace());

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(gson.toJson(result.toJson()));
    }

    private boolean isChatRequest(HttpServletRequest request) {
        return "/ai/assist/chat".equals(request.getServletPath())
            || "/ai/assist/chat/clear".equals(request.getServletPath());
    }

    private boolean isClearRequest(HttpServletRequest request) {
        return "/ai/assist/chat/clear".equals(request.getServletPath());
    }

    private AiChatMemory getMemory(HttpServletRequest request, UserAccount currentUser) {
        HttpSession session = request.getSession(true);
        AiChatMemory memory = (AiChatMemory) session.getAttribute(AI_CHAT_MEMORY);
        if (memory == null) {
            memory = new AiChatMemory();
            session.setAttribute(AI_CHAT_MEMORY, memory);
        }
        memory.ensureOwner(currentUser.getRole(), currentUser.getId());
        return memory;
    }

    private void writeChatHealth(HttpServletResponse response) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        JsonObject status = new JsonObject();
        status.addProperty("success", true);
        status.addProperty("sourceMode", "local");
        status.addProperty("message", "AI assist chat endpoint is available.");
        response.getWriter().write(gson.toJson(status));
    }

    private String readBody(HttpServletRequest request) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        return builder.toString();
    }
}
