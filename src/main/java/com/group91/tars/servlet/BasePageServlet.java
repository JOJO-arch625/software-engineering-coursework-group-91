package com.group91.tars.servlet;

import com.group91.tars.i18n.I18n;
import com.group91.tars.i18n.LocaleFilter;
import com.group91.tars.model.FlashMessage;
import com.group91.tars.model.UserAccount;
import com.group91.tars.service.TarsService;
import com.group91.tars.web.FlashScope;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;

public abstract class BasePageServlet extends HttpServlet {
    protected static final String SESSION_USER = "currentUser";
    protected final TarsService service = TarsService.getInstance();

    protected void preparePage(HttpServletRequest request, String currentView, String viewTag, String viewTitle) {
        UserAccount currentUser = getCurrentUser(request);
        HttpSession session = request.getSession(true);
        Locale locale = (Locale) session.getAttribute(LocaleFilter.SESSION_LOCALE_KEY);
        if (locale == null) {
            locale = Locale.ENGLISH;
        }
        I18n i18n = new I18n(locale);
        request.setAttribute("i18n", i18n);
        request.setAttribute("lang", locale.getLanguage());

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

    protected void flashI18n(HttpServletRequest request, String level, String key) {
        I18n i18n = resolveI18n(request);
        FlashScope.put(request, level, i18n.t(key));
    }

    protected void flashI18n(HttpServletRequest request, String level, String key, Object... args) {
        I18n i18n = resolveI18n(request);
        FlashScope.put(request, level, i18n.t(key, args));
    }

    protected I18n resolveI18n(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Locale locale = session != null ? (Locale) session.getAttribute(LocaleFilter.SESSION_LOCALE_KEY) : null;
        if (locale == null) locale = Locale.ENGLISH;
        return new I18n(locale);
    }

    protected UserAccount getCurrentUser(HttpServletRequest request) {
        return (UserAccount) request.getSession().getAttribute(SESSION_USER);
    }

    protected boolean requireAuthenticated(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        if (getCurrentUser(request) != null) {
            return true;
        }
        flashI18n(request, "error", "flash.auth.required");
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

        flashI18n(request, "error", "flash.auth.no-permission");
        redirect(request, response, service.getHomePathForRole(currentUser.getRole()));
        return false;
    }

    private String buildHomePath(UserAccount currentUser) {
        return currentUser == null ? "/login" : service.getHomePathForRole(currentUser.getRole());
    }

    private String resolveMetricOneLabel(UserAccount currentUser) {
        if (currentUser == null) return "metric.demo-accounts";
        if (TarsService.ROLE_MO.equals(currentUser.getRole())) return "metric.open-posts";
        if (TarsService.ROLE_ADMIN.equals(currentUser.getRole())) return "metric.tracked-tas";
        return "metric.open-jobs";
    }

    private int resolveMetricOneValue(UserAccount currentUser) {
        if (currentUser == null) return 3;
        if (TarsService.ROLE_ADMIN.equals(currentUser.getRole())) return service.getWorkloadSummaries().size();
        return service.countOpenJobs();
    }

    private String resolveMetricTwoLabel(UserAccount currentUser) {
        if (currentUser == null) return "metric.json-files";
        if (TarsService.ROLE_MO.equals(currentUser.getRole())) return "metric.applicants";
        if (TarsService.ROLE_ADMIN.equals(currentUser.getRole())) return "metric.accepted";
        return "metric.applications";
    }

    private int resolveMetricTwoValue(UserAccount currentUser) {
        if (currentUser == null) return 4;
        if (TarsService.ROLE_MO.equals(currentUser.getRole())) return service.countAllApplications();
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
        if (currentUser == null) return "metric.roles";
        if (TarsService.ROLE_MO.equals(currentUser.getRole())) return "metric.pending";
        if (TarsService.ROLE_ADMIN.equals(currentUser.getRole())) return "metric.alerts";
        return "metric.accepted";
    }

    private int resolveMetricThreeValue(UserAccount currentUser) {
        if (currentUser == null) return 3;
        if (TarsService.ROLE_MO.equals(currentUser.getRole())) return service.countPendingApplications();
        if (TarsService.ROLE_ADMIN.equals(currentUser.getRole())) return service.countOverloadSummaries();
        return service.countAcceptedJobsForTaPublic(currentUser.getLinkedId());
    }
}
