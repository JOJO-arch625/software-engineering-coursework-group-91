package com.group91.tars.servlet;

import com.group91.tars.model.FlashMessage;
import com.group91.tars.service.TarsService;
import com.group91.tars.web.FlashScope;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class BasePageServlet extends HttpServlet {
    protected final TarsService service = TarsService.getInstance();

    protected void preparePage(HttpServletRequest request, String currentView, String viewTag, String viewTitle) {
        request.setAttribute("currentView", currentView);
        request.setAttribute("viewTag", viewTag);
        request.setAttribute("viewTitle", viewTitle);
        request.setAttribute("openJobCount", service.countOpenJobs());
        request.setAttribute("applicationCount", service.countCurrentTaApplications());
        request.setAttribute("acceptedCount", service.countCurrentTaAcceptedJobs());
        request.setAttribute("flash", FlashScope.consume(request));
        request.setAttribute("navLinks", buildNavLinks());
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

    @SuppressWarnings("unchecked")
    private Map<String, String> buildNavLinks() {
        Map<String, String> links = new LinkedHashMap<String, String>();
        links.put("gateway", "/");
        links.put("ta-dashboard", "/ta/dashboard");
        links.put("ta-profile", "/ta/profile");
        links.put("job-list", "/ta/jobs");
        links.put("job-detail", "/ta/job");
        links.put("applications", "/ta/applications");
        links.put("mo-dashboard", "/mo/dashboard");
        links.put("job-editor", "/mo/jobs/edit");
        links.put("review", "/mo/review");
        links.put("admin-dashboard", "/admin/workload");
        links.put("ai-assist", "/ai/assist");
        return links;
    }
}
