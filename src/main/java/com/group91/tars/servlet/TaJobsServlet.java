package com.group91.tars.servlet;

import com.group91.tars.service.TarsService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Renders the list of open job postings at {@code /ta/jobs} for TAs to browse.
 */
@WebServlet("/ta/jobs")
public class TaJobsServlet extends BasePageServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        if (!requireRole(request, response, TarsService.ROLE_TA)) {
            return;
        }
        preparePage(request, "job-list", "TA Flow", "Browse Job Postings");
        request.setAttribute("jobs", service.getOpenJobs());
        forward(request, response, "/WEB-INF/jsp/ta/jobs.jsp");
    }
}
