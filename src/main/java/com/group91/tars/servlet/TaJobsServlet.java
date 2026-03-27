package com.group91.tars.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/ta/jobs")
public class TaJobsServlet extends BasePageServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        preparePage(request, "job-list", "TA Flow", "Browse Job Postings");
        request.setAttribute("jobs", service.getAllJobs());
        forward(request, response, "/WEB-INF/jsp/ta/jobs.jsp");
    }
}
