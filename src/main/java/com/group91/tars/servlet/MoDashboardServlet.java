package com.group91.tars.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/mo/dashboard")
public class MoDashboardServlet extends BasePageServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        preparePage(request, "mo-dashboard", "MO Flow", "MO Dashboard");
        request.setAttribute("jobs", service.getJobsForCurrentMo());
        request.setAttribute("pendingCount", service.countPendingApplications());
        request.setAttribute("applicantCount", service.countAllApplications());
        forward(request, response, "/WEB-INF/jsp/mo/dashboard.jsp");
    }
}
