package com.group91.tars.servlet;

import javax.servlet.ServletException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Renders the AI assist concept panel at {@code /ai/assist},
 * displaying AI feature status notes and guidance.
 */
@WebServlet("/ai/assist")
public class AiAssistServlet extends BasePageServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        if (!requireAuthenticated(request, response)) {
            return;
        }
        preparePage(request, "ai-assist", "Optional", "AI Assist Concept");
        request.setAttribute("aiTodos", service.getAiTodoNotes());
        forward(request, response, "/WEB-INF/jsp/ai/assist.jsp");
    }
}
