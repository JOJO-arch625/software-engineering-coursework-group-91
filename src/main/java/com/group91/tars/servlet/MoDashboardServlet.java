package com.group91.tars.servlet;

import com.group91.tars.model.ApplicationRecord;
import com.group91.tars.model.JobPosting;
import com.group91.tars.service.TarsService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/mo/dashboard")
public class MoDashboardServlet extends BasePageServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        if (!requireRole(request, response, TarsService.ROLE_MO)) {
            return;
        }
        preparePage(request, "mo-dashboard", "MO Flow", "MO Dashboard");
        String moId = getCurrentUser(request).getLinkedId();
        List<JobPosting> jobs = service.getJobsForMo(moId);
        request.setAttribute("jobs", jobs);
        request.setAttribute("openJobCount", service.countOpenJobs());
        request.setAttribute("pendingCount", service.countPendingApplications());
        request.setAttribute("applicantCount", service.countAllApplications());

        List<ApplicationRecord> allApplications = new ArrayList<ApplicationRecord>();
        for (JobPosting job : jobs) {
            allApplications.addAll(service.getApplicationsForJob(job.getId()));
        }
        request.setAttribute("allApplications", allApplications);
        forward(request, response, "/WEB-INF/jsp/mo/dashboard.jsp");
    }
}
