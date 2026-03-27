package com.group91.tars.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/ta/applications")
public class TaApplicationsServlet extends BasePageServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        preparePage(request, "applications", "TA Flow", "My Applications");
        request.setAttribute("applications", service.getApplicationsForCurrentTa());
        forward(request, response, "/WEB-INF/jsp/ta/applications.jsp");
    }
}
