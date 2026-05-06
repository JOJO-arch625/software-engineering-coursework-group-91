package com.group91.tars.servlet;

import com.group91.tars.service.TarsService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Renders the Admin workload dashboard at {@code /admin/workload},
 * displaying all TA workload summaries and overload alerts.
 */
@WebServlet("/admin/workload")
public class AdminWorkloadServlet extends BasePageServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        if (!requireRole(request, response, TarsService.ROLE_ADMIN)) {
            return;
        }
        preparePage(request, "admin-dashboard", "Admin Flow", "Admin Workload Dashboard");
        request.setAttribute("summaries", service.getWorkloadSummaries());
        request.setAttribute("overloadCount", service.countOverloadSummaries());
        forward(request, response, "/WEB-INF/jsp/admin/workload.jsp");
    }
}
