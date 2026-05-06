package com.group91.tars.servlet;

import com.group91.tars.model.UserAccount;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles user authentication at {@code /login}. On GET, renders the login form;
 * on POST, validates credentials and redirects to the role-specific home page.
 */
@WebServlet("/login")
public class LoginServlet extends BasePageServlet {
    /**
     * Renders the login page. If the user is already authenticated, redirects
     * to their role-specific home page.
     */
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

    /**
     * Processes login form submission. Authenticates the user and, on success,
     * creates a session and redirects to the role-specific dashboard.
     * On failure, flashes an error and redirects back to the login form.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        UserAccount account = service.authenticate(request.getParameter("username"), request.getParameter("password"));
        if (account == null) {
            flashI18n(request, "error", "flash.login.invalid");
            redirect(request, response, "/login");
            return;
        }

        request.getSession(true).setAttribute(SESSION_USER, account);
        flashI18n(request, "success", "flash.login.welcome", account.getDisplayName());
        redirect(request, response, service.getHomePathForRole(account.getRole()));
    }
}
