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
 * Handles applicant review at {@code /mo/review}. GET shows all EBU6304
 * applicants in a unified list; POST processes status updates.
 * Only EBU6304 - Software Engineering jobs are visible to the MO.
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
        String moId = getCurrentUser(request).getLinkedId();
        List<JobPosting> moJobs = service.getJobsForMo(moId);
        List<ApplicationRecord> allApplications = collectMoApplications(moJobs);
        ApplicationRecord selectedApplication = resolveApplication(request, allApplications);
        request.setAttribute("moJobs", moJobs);
        request.setAttribute("allApplications", allApplications);
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
            String applicationId = request.getParameter("applicationId");
            String jobId = request.getParameter("jobId");
            if (!canReviewApplication(applicationId, jobId, request)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            result = service.updateApplicationStatus(
                applicationId,
                request.getParameter("status"),
                request.getParameter("notes")
            );
        }

        if (result.isSuccess()) {
            flashI18n(request, "success", result.getMessageKey() != null ? result.getMessageKey() : "flash.review.updated");
        } else {
            flashI18n(request, "error", result.getMessageKey() != null ? result.getMessageKey() : "flash.review.not-found");
        }
        redirect(request, response, "/mo/review");
    }

    private List<ApplicationRecord> collectMoApplications(List<JobPosting> moJobs) {
        List<ApplicationRecord> all = new ArrayList<ApplicationRecord>();
        for (JobPosting job : moJobs) {
            all.addAll(service.getApplicationsForJob(job.getId()));
        }
        return all;
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

    private boolean canReviewApplication(String applicationId, String jobId, HttpServletRequest request) {
        if (applicationId == null) {
            return false;
        }
        List<JobPosting> moJobs = service.getJobsForMo(getCurrentUser(request).getLinkedId());
        for (JobPosting job : moJobs) {
            if (jobId != null && !jobId.equals(job.getId())) {
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
        List<JobPosting> moJobs = service.getJobsForMo(getCurrentUser(request).getLinkedId());
        for (JobPosting job : moJobs) {
            if (jobId.equals(job.getId())) {
                return true;
            }
        }
        return false;
    }
}
