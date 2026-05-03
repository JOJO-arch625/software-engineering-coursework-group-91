package com.group91.tars.servlet;

import com.group91.tars.model.JobPosting;
import com.group91.tars.model.OperationResult;
import com.group91.tars.service.TarsService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/mo/jobs/edit")
public class MoJobEditServlet extends BasePageServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        if (!requireRole(request, response, TarsService.ROLE_MO)) {
            return;
        }
        preparePage(request, "job-editor", "MO Flow", "Create Or Edit Job Posting");
        request.setAttribute("job", resolveDraft(request));
        request.setAttribute("myJobs", service.getJobsForMo(getCurrentUser(request).getLinkedId()));
        forward(request, response, "/WEB-INF/jsp/mo/job-editor.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        if (!requireRole(request, response, TarsService.ROLE_MO)) {
            return;
        }
        String action = request.getParameter("action");
        OperationResult result;
        if ("close".equals(action)) {
            result = service.closeJobPosting(request.getParameter("id"));
        } else {
            JobPosting draft = new JobPosting();
            draft.setId(request.getParameter("id"));
            draft.setModuleCode(request.getParameter("moduleCode"));
            draft.setTitle(request.getParameter("title"));
            draft.setSkills(request.getParameter("skills"));
            draft.setRequirements(request.getParameter("requirements"));
            draft.setWorkload(request.getParameter("workload"));
            draft.setDeadline(request.getParameter("deadline"));
            draft.setDescription(request.getParameter("description"));
            draft.setStatus(request.getParameter("status"));
            result = service.saveJobPosting(getCurrentUser(request).getLinkedId(), draft);
        }
        flash(request, result.isSuccess() ? "success" : "error", result.getMessage());
        redirect(request, response, "/mo/jobs/edit" + buildQuerySuffix(request.getParameter("id")));
    }

    private JobPosting resolveDraft(HttpServletRequest request) {
        String id = request.getParameter("id");
        JobPosting existing = id == null ? null : service.getJobById(id);
        if (existing != null) {
            return existing;
        }
        JobPosting blank = new JobPosting();
        blank.setStatus("Open");
        return blank;
    }

    private String buildQuerySuffix(String id) {
        return (id == null || id.trim().isEmpty()) ? "" : "?id=" + id;
    }
}
