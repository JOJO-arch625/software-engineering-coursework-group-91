package com.group91.tars.servlet;

import com.group91.tars.model.UserAccount;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends BasePageServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        UserAccount currentUser = getCurrentUser(request);
        if (currentUser != null) {
            redirect(request, response, service.getHomePathForRole(currentUser.getRole()));
            return;
        }

        request.setAttribute("flash", com.group91.tars.web.FlashScope.consume(request));
        forward(request, response, "/WEB-INF/jsp/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        UserAccount account = service.authenticate(request.getParameter("username"), request.getParameter("password"));
        if (account == null) {
            flash(request, "error", "Invalid username or password. Please use one of the demo accounts.");
            redirect(request, response, "/login");
            return;
        }

        request.getSession(true).setAttribute(SESSION_USER, account);
        flash(request, "success", "Welcome back, " + account.getDisplayName() + ".");
        redirect(request, response, service.getHomePathForRole(account.getRole()));
    }
}
