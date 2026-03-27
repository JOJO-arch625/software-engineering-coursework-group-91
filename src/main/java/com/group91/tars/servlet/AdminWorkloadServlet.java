package com.group91.tars.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/workload")
public class AdminWorkloadServlet extends BasePageServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        preparePage(request, "admin-dashboard", "Admin Flow", "Admin Workload Dashboard");
        request.setAttribute("summaries", service.getWorkloadSummaries());
        request.setAttribute("overloadCount", service.countOverloadSummaries());
        forward(request, response, "/WEB-INF/jsp/admin/workload.jsp");
    }
}
