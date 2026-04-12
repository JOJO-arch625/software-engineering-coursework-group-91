package com.group91.tars.servlet;

import com.group91.tars.model.FlashMessage;
import com.group91.tars.model.UserAccount;
import com.group91.tars.service.TarsService;
import com.group91.tars.web.FlashScope;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class BasePageServlet extends HttpServlet {
    protected static final String SESSION_USER = "currentUser";
    protected final TarsService service = TarsService.getInstance();

    protected void preparePage(HttpServletRequest request, String currentView, String viewTag, String viewTitle) {
        UserAccount currentUser = getCurrentUser(request);
        request.setAttribute("currentView", currentView);
        request.setAttribute("viewTag", viewTag);
        request.setAttribute("viewTitle", viewTitle);
        request.setAttribute("flash", FlashScope.consume(request));
        request.setAttribute("currentUser", currentUser);
        request.setAttribute("isAuthenticated", currentUser != null);
        request.setAttribute("currentRole", currentUser == null ? null : currentUser.getRole());
        request.setAttribute("sidebarHomePath", buildHomePath(currentUser));
        request.setAttribute("topMetricOneLabel", resolveMetricOneLabel(currentUser));
        request.setAttribute("topMetricOneValue", resolveMetricOneValue(currentUser));
        request.setAttribute("topMetricTwoLabel", resolveMetricTwoLabel(currentUser));
        request.setAttribute("topMetricTwoValue", resolveMetricTwoValue(currentUser));
        request.setAttribute("topMetricThreeLabel", resolveMetricThreeLabel(currentUser));
        request.setAttribute("topMetricThreeValue", resolveMetricThreeValue(currentUser));
    }

    protected void forward(HttpServletRequest request, HttpServletResponse response, String jspPath)
        throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(jspPath);
        dispatcher.forward(request, response);
    }

    protected void redirect(HttpServletRequest request, HttpServletResponse response, String target)
        throws IOException {
        response.sendRedirect(request.getContextPath() + target);
    }

    protected void flash(HttpServletRequest request, String level, String text) {
        FlashScope.put(request, level, text);
    }

    protected UserAccount getCurrentUser(HttpServletRequest request) {
        return (UserAccount) request.getSession().getAttribute(SESSION_USER);
    }

    protected boolean requireAuthenticated(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        if (getCurrentUser(request) != null) {
            return true;
        }
        flash(request, "error", "Please log in before accessing the recruitment workspace.");
        redirect(request, response, "/login");
        return false;
    }

    protected boolean requireRole(HttpServletRequest request, HttpServletResponse response, String requiredRole)
        throws IOException {
        if (!requireAuthenticated(request, response)) {
            return false;
        }

        UserAccount currentUser = getCurrentUser(request);
        if (requiredRole.equals(currentUser.getRole())) {
            return true;
        }

        flash(request, "error", "Your account does not have permission to open that page.");
        redirect(request, response, service.getHomePathForRole(currentUser.getRole()));
        return false;
    }

    private String buildHomePath(UserAccount currentUser) {
        return currentUser == null ? "/login" : service.getHomePathForRole(currentUser.getRole());
    }

    private String resolveMetricOneLabel(UserAccount currentUser) {
        if (currentUser == null) {
            return "Demo Accounts";
        }
        if (TarsService.ROLE_MO.equals(currentUser.getRole())) {
            return "Open Posts";
        }
        if (TarsService.ROLE_ADMIN.equals(currentUser.getRole())) {
            return "Tracked TAs";
        }
        return "Open Jobs";
    }

    private int resolveMetricOneValue(UserAccount currentUser) {
        if (currentUser == null) {
            return 3;
        }
        if (TarsService.ROLE_ADMIN.equals(currentUser.getRole())) {
            return service.getWorkloadSummaries().size();
        }
        return service.countOpenJobs();
    }

    private String resolveMetricTwoLabel(UserAccount currentUser) {
        if (currentUser == null) {
            return "JSON Files";
        }
        if (TarsService.ROLE_MO.equals(currentUser.getRole())) {
            return "Applicants";
        }
        if (TarsService.ROLE_ADMIN.equals(currentUser.getRole())) {
            return "Accepted";
        }
        return "Applications";
    }

    private int resolveMetricTwoValue(UserAccount currentUser) {
        if (currentUser == null) {
            return 4;
        }
        if (TarsService.ROLE_MO.equals(currentUser.getRole())) {
            return service.countAllApplications();
        }
        if (TarsService.ROLE_ADMIN.equals(currentUser.getRole())) {
            int totalAccepted = 0;
            for (com.group91.tars.model.WorkloadSummary summary : service.getWorkloadSummaries()) {
                totalAccepted += summary.getAcceptedCount();
            }
            return totalAccepted;
        }
        return service.countApplicationsForTa(currentUser.getLinkedId());
    }

    private String resolveMetricThreeLabel(UserAccount currentUser) {
        if (currentUser == null) {
            return "Roles";
        }
        if (TarsService.ROLE_MO.equals(currentUser.getRole())) {
            return "Pending";
        }
        if (TarsService.ROLE_ADMIN.equals(currentUser.getRole())) {
            return "Alerts";
        }
        return "Accepted";
    }

    private int resolveMetricThreeValue(UserAccount currentUser) {
        if (currentUser == null) {
            return 3;
        }
        if (TarsService.ROLE_MO.equals(currentUser.getRole())) {
            return service.countPendingApplications();
        }
        if (TarsService.ROLE_ADMIN.equals(currentUser.getRole())) {
            return service.countOverloadSummaries();
        }
        return service.countAcceptedJobsForTaPublic(currentUser.getLinkedId());
    }
}
