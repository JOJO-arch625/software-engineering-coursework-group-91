package com.group91.tars.servlet;

import com.group91.tars.service.TarsService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/ta/dashboard")
public class TaDashboardServlet extends BasePageServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        if (!requireRole(request, response, TarsService.ROLE_TA)) {
            return;
        }
        preparePage(request, "ta-dashboard", "TA Flow", "TA Dashboard");
        request.setAttribute("profile", service.getTaProfile(getCurrentUser(request).getLinkedId()));
        request.setAttribute("notifications", service.getNotificationsForTa(getCurrentUser(request).getLinkedId()));
        forward(request, response, "/WEB-INF/jsp/ta/dashboard.jsp");
    }
}
