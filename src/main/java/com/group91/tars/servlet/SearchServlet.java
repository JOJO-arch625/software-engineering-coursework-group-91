package com.group91.tars.servlet;

import com.group91.tars.model.ApplicationRecord;
import com.group91.tars.model.JobPosting;
import com.group91.tars.model.TAProfile;
import com.group91.tars.model.UserAccount;
import com.group91.tars.service.TarsService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/search")
public class SearchServlet extends BasePageServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        if (!requireAuthenticated(request, response)) {
            return;
        }
        preparePage(request, "search", "Search", "Global Search");
        String query = request.getParameter("q");
        String category = request.getParameter("category");
        if (category == null || category.trim().isEmpty()) {
            category = "all";
        }

        UserAccount currentUser = getCurrentUser(request);
        String role = currentUser.getRole();

        List<JobPosting> jobResults = new ArrayList<JobPosting>();
        List<ApplicationRecord> applicationResults = new ArrayList<ApplicationRecord>();
        List<TAProfile> profileResults = new ArrayList<TAProfile>();

        if (query != null && !query.trim().isEmpty()) {
            if ("all".equals(category) || "jobs".equals(category)) {
                if (TarsService.ROLE_TA.equals(role)) {
                    for (JobPosting job : service.searchJobs(query)) {
                        if ("Open".equals(job.getStatus())) {
                            jobResults.add(job);
                        }
                    }
                } else {
                    jobResults = service.searchJobs(query);
                }
            }
            if ("all".equals(category) || "applications".equals(category)) {
                if (TarsService.ROLE_TA.equals(role)) {
                    for (ApplicationRecord app : service.searchApplications(query)) {
                        if (currentUser.getLinkedId().equals(app.getTaId())) {
                            applicationResults.add(app);
                        }
                    }
                } else {
                    applicationResults = service.searchApplications(query);
                }
            }
            if ("all".equals(category) || "applicants".equals(category)) {
                if (TarsService.ROLE_MO.equals(role) || TarsService.ROLE_ADMIN.equals(role)) {
                    profileResults = service.searchProfiles(query);
                }
            }
        }

        request.setAttribute("query", query == null ? "" : query);
        request.setAttribute("category", category);
        request.setAttribute("jobResults", jobResults);
        request.setAttribute("applicationResults", applicationResults);
        request.setAttribute("profileResults", profileResults);
        request.setAttribute("jobResultCount", jobResults.size());
        request.setAttribute("applicationResultCount", applicationResults.size());
        request.setAttribute("profileResultCount", profileResults.size());
        forward(request, response, "/WEB-INF/jsp/search.jsp");
    }
}
