package com.group91.tars.servlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Invalidates the current session and redirects to the login page.
 * Mapped to {@code /logout}.
 */
@WebServlet("/logout")
public class LogoutServlet extends BasePageServlet {
    /** Invalidates the session, flashes a logout message, and redirects to /login. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        request.getSession().invalidate();
        flashI18n(request, "info", "flash.logout.done");
        redirect(request, response, "/login");
    }
}
