package com.group91.tars.servlet;

import com.group91.tars.model.Notification;
import com.group91.tars.model.UserAccount;
import com.group91.tars.service.TarsService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/inbox")
public class InboxServlet extends BasePageServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!requireAuthenticated(request, response)) {
            return;
        }

        UserAccount user = getCurrentUser(request);
        String action = request.getParameter("action");
        String id = request.getParameter("id");

        if ("read".equals(action) && id != null) {
            service.markNotificationAsRead(id);
            redirect(request, response, "/inbox");
            return;
        }

        preparePage(request, "inbox", "Inbox", "Your Notifications");
        List<Notification> notifications = service.getNotificationsForUser(user.getLinkedId(), user.getRole());
        request.setAttribute("notifications", notifications);

        forward(request, response, "/WEB-INF/jsp/inbox.jsp");
    }
}
