package com.group91.tars.servlet;

import com.group91.tars.model.Notification;
import com.group91.tars.model.OperationResult;
import com.group91.tars.model.UserAccount;
import com.group91.tars.service.TarsService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Handles the notification inbox at {@code /inbox}. GET renders the notification
 * list; POST processes mark-read and mark-all-read actions.
 */
@WebServlet("/inbox")
public class InboxServlet extends BasePageServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        if (!requireAuthenticated(request, response)) {
            return;
        }
        preparePage(request, "inbox", "Inbox", "Notifications");
        UserAccount currentUser = getCurrentUser(request);
        List<Notification> notifications = service.getNotificationsForUser(currentUser.getLinkedId());
        int unreadCount = service.countUnreadNotifications(currentUser.getLinkedId());
        request.setAttribute("notifications", notifications);
        request.setAttribute("unreadCount", unreadCount);
        forward(request, response, "/WEB-INF/jsp/inbox.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        if (!requireAuthenticated(request, response)) {
            return;
        }
        String action = request.getParameter("action");
        UserAccount currentUser = getCurrentUser(request);
        OperationResult result;
        if ("markRead".equals(action)) {
            result = service.markNotificationRead(request.getParameter("notificationId"));
        } else if ("markAllRead".equals(action)) {
            result = service.markAllNotificationsRead(currentUser.getLinkedId());
        } else {
            result = OperationResult.failure("flash.inbox.unknown-action", "Unknown action.");
        }
        if (result.isSuccess()) {
            flashI18n(request, "success", result.getMessageKey() != null ? result.getMessageKey() : "flash.inbox.marked-read");
        } else {
            flashI18n(request, "error", result.getMessageKey() != null ? result.getMessageKey() : "flash.inbox.unknown-action");
        }
        redirect(request, response, "/inbox");
    }
}
