package com.group91.tars.servlet;

import com.group91.tars.service.TarsService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Renders the TA's application tracking page at {@code /ta/applications},
 * showing all submitted applications with their current statuses.
 */
@WebServlet("/ta/applications")
public class TaApplicationsServlet extends BasePageServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        if (!requireRole(request, response, TarsService.ROLE_TA)) {
            return;
        }
        preparePage(request, "applications", "TA Flow", "My Applications");
        request.setAttribute("applications", service.getApplicationsForTa(getCurrentUser(request).getLinkedId()));
        forward(request, response, "/WEB-INF/jsp/ta/applications.jsp");
    }
}
