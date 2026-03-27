package com.group91.tars.servlet;

import com.group91.tars.model.ApplicationRecord;
import com.group91.tars.model.JobPosting;
import com.group91.tars.model.OperationResult;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/mo/review")
public class MoReviewServlet extends BasePageServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        preparePage(request, "review", "MO Flow", "Applicant Review");
        JobPosting selectedJob = resolveJob(request);
        List<ApplicationRecord> applications = selectedJob == null
            ? new ArrayList<ApplicationRecord>()
            : service.getApplicationsForJob(selectedJob.getId());
        ApplicationRecord selectedApplication = resolveApplication(request, applications);
        request.setAttribute("selectedJob", selectedJob);
        request.setAttribute("applications", applications);
        request.setAttribute("selectedApplication", selectedApplication);
        request.setAttribute("selectedApplicant", selectedApplication == null ? null : service.getProfileById(selectedApplication.getTaId()));
        request.setAttribute("aiTodos", service.getAiTodoNotes());
        forward(request, response, "/WEB-INF/jsp/mo/review.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        OperationResult result = service.updateApplicationStatus(
            request.getParameter("applicationId"),
            request.getParameter("status"),
            request.getParameter("notes")
        );
        flash(request, result.isSuccess() ? "success" : "error", result.getMessage());
        String redirectTarget = "/mo/review?jobId=" + request.getParameter("jobId") + "&appId=" + request.getParameter("applicationId");
        redirect(request, response, redirectTarget);
    }

    private JobPosting resolveJob(HttpServletRequest request) {
        String jobId = request.getParameter("jobId");
        if (jobId != null) {
            JobPosting explicit = service.getJobById(jobId);
            if (explicit != null) {
                return explicit;
            }
        }
        if (!service.getJobsForCurrentMo().isEmpty()) {
            return service.getJobsForCurrentMo().get(0);
        }
        return null;
    }

    private ApplicationRecord resolveApplication(HttpServletRequest request, List<ApplicationRecord> applications) {
        String appId = request.getParameter("appId");
        if (appId != null) {
            for (ApplicationRecord application : applications) {
                if (appId.equals(application.getId())) {
                    return application;
                }
            }
        }
        return applications.isEmpty() ? null : applications.get(0);
    }
}
