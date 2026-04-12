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
import java.util.List;

@WebServlet("/search")
public class SearchServlet extends BasePageServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!requireAuthenticated(request, response)) {
            return;
        }

        UserAccount user = getCurrentUser(request);
        String query = request.getParameter("q");
        
        preparePage(request, "search", "Search", "Global Search Results");
        request.setAttribute("query", query);

        if (query != null && !query.trim().isEmpty()) {
            List<JobPosting> jobs = service.searchJobs(query);
            List<ApplicationRecord> applications = service.searchApplications(query, user.getRole(), user.getLinkedId());
            
            request.setAttribute("jobResults", jobs);
            request.setAttribute("applicationResults", applications);

            if (TarsService.ROLE_ADMIN.equals(user.getRole()) || TarsService.ROLE_MO.equals(user.getRole())) {
                List<TAProfile> profiles = service.searchProfiles(query);
                request.setAttribute("profileResults", profiles);
            }
        }

        forward(request, response, "/WEB-INF/jsp/search.jsp");
    }
}
