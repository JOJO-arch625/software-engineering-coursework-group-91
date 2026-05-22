package com.group91.tars.servlet;

import com.group91.tars.model.ApplicationRecord;
import com.group91.tars.model.JobPosting;
import com.group91.tars.model.OperationResult;
import com.group91.tars.service.TarsService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles applicant review at {@code /mo/review}. GET shows the split-panel
 * review interface; POST processes accept/reject/under-review decisions.
 */
@WebServlet("/mo/review")
public class MoReviewServlet extends BasePageServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        if (!requireRole(request, response, TarsService.ROLE_MO)) {
            return;
        }
        preparePage(request, "review", "MO Flow", "Applicant Review");
        List<JobPosting> moJobs = service.getJobsForMo(getCurrentUser(request).getLinkedId());
        JobPosting selectedJob = resolveJob(request, moJobs);
        List<ApplicationRecord> applications = selectedJob == null
            ? new ArrayList<ApplicationRecord>()
            : service.getApplicationsForJob(selectedJob.getId());
        ApplicationRecord selectedApplication = resolveApplication(request, applications);
        request.setAttribute("moJobs", moJobs);
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
    if (!requireRole(request, response, TarsService.ROLE_MO)) {
        return;
    }

    String action = request.getParameter("action");
    OperationResult result;
    if ("bulkShortlist".equals(action)) {
        if (!canReviewJob(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        result = service.bulkShortlistApplications(
            request.getParameter("jobId"),
            request.getParameter("notes")
        );
    } else {
        if (!canReviewApplication(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        result = service.updateApplicationStatus(
            request.getParameter("applicationId"),
            request.getParameter("status"),
            request.getParameter("notes")
        );
    }

    if (result.isSuccess()) {
        flashI18n(request, "success", result.getMessageKey() != null ? result.getMessageKey() : "flash.review.updated");
    } else {
        flashI18n(request, "error", result.getMessageKey() != null ? result.getMessageKey() : "flash.review.not-found");
    }
    String appId = request.getParameter("applicationId");
    String redirectTarget = "/mo/review?jobId=" + request.getParameter("jobId")
        + (appId == null || appId.trim().isEmpty() ? "" : "&appId=" + appId);
    redirect(request, response, redirectTarget);
}

    private JobPosting resolveJob(HttpServletRequest request, List<JobPosting> moJobs) {
        String jobId = request.getParameter("jobId");
        if (jobId != null) {
            for (JobPosting job : moJobs) {
                if (jobId.equals(job.getId())) {
                    return job;
                }
            }
        }
        return moJobs.isEmpty() ? null : moJobs.get(0);
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

    private boolean canReviewApplication(HttpServletRequest request) {
        String jobId = request.getParameter("jobId");
        String applicationId = request.getParameter("applicationId");
        if (jobId == null || applicationId == null) {
            return false;
        }
        for (JobPosting job : service.getJobsForMo(getCurrentUser(request).getLinkedId())) {
            if (!jobId.equals(job.getId())) {
                continue;
            }
            for (ApplicationRecord application : service.getApplicationsForJob(job.getId())) {
                if (applicationId.equals(application.getId())) {
                    return true;
                }
            }
        }
        return false;
    }
  private boolean canReviewJob(HttpServletRequest request) {
    String jobId = request.getParameter("jobId");
    if (jobId == null) {
        return false;
    }
    for (JobPosting job : service.getJobsForMo(getCurrentUser(request).getLinkedId())) {
        if (jobId.equals(job.getId())) {
            return true;
        }
    }
    return false;
}
}
