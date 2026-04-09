package com.group91.tars.servlet;

import com.group91.tars.model.JobPosting;
import com.group91.tars.model.OperationResult;
import com.group91.tars.service.TarsService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/ta/job")
public class TaJobDetailServlet extends BasePageServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        if (!requireRole(request, response, TarsService.ROLE_TA)) {
            return;
        }
        preparePage(request, "job-detail", "TA Flow", "Job Detail And Application");
        JobPosting job = resolveSelectedJob(request);
        request.setAttribute("job", job);
        request.setAttribute("aiTodos", service.getAiTodoNotes());
        forward(request, response, "/WEB-INF/jsp/ta/job-detail.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        if (!requireRole(request, response, TarsService.ROLE_TA)) {
            return;
        }
        String jobId = request.getParameter("jobId");
        OperationResult result = service.submitTaApplication(
            getCurrentUser(request).getLinkedId(),
            jobId,
            request.getParameter("priority"),
            request.getParameter("notes")
        );
        flash(request, result.isSuccess() ? "success" : "error", result.getMessage());
        response.sendRedirect(request.getContextPath() + "/ta/job?id=" + jobId);
    }

    private JobPosting resolveSelectedJob(HttpServletRequest request) {
        String jobId = request.getParameter("id");
        JobPosting job = jobId == null ? null : service.getJobById(jobId);
        if (job != null) {
            return job;
        }
        if (!service.getOpenJobs().isEmpty()) {
            return service.getOpenJobs().get(0);
        }
        if (!service.getAllJobs().isEmpty()) {
            return service.getAllJobs().get(0);
        }
        return null;
    }
}
