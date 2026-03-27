package com.group91.tars.servlet;

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
        preparePage(request, "ta-dashboard", "TA Flow", "TA Dashboard");
        request.setAttribute("profile", service.getCurrentTaProfile());
        request.setAttribute("notifications", service.getCurrentTaNotifications());
        forward(request, response, "/WEB-INF/jsp/ta/dashboard.jsp");
    }
}
