package com.group91.tars.servlet;

import com.group91.tars.model.Notification;
import com.group91.tars.model.OperationResult;
import com.group91.tars.model.UserAccount;
import com.group91.tars.model.JobPosting;
import com.group91.tars.service.TarsService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the notification inbox at {@code /inbox}. GET renders the notification
 * list; POST processes mark-read and mark-all-read actions.
 *
 * MO users only see notifications related to EBU6304 - Software Engineering jobs.
 * Non-job notifications (e.g. system alerts) are not shown to MO.
 * TA and Admin see all their own notifications.
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
        List<Notification> allNotifications = service.getNotificationsForUser(currentUser.getLinkedId());
        List<Notification> notifications = filterByRole(currentUser, allNotifications);
        int unreadCount = countUnread(notifications);
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

    private List<Notification> filterByRole(UserAccount user, List<Notification> notifications) {
        if (TarsService.ROLE_MO.equals(user.getRole())) {
            List<Notification> filtered = new ArrayList<Notification>();
            for (Notification n : notifications) {
                if (isEbU6304Related(n)) {
                    filtered.add(n);
                }
            }
            return filtered;
        }
        return notifications;
    }

    /**
     * MO only sees notifications linked to an EBU6304 job.
     * Returns true if the notification's link references an EBU6304 job.
     */
    private boolean isEbU6304Related(Notification n) {
        String link = n.getLink();
        if (link == null || link.trim().isEmpty()) {
            return false;
        }
        List<String> jobIds = extractJobIds(link);
        for (String jobId : jobIds) {
            JobPosting job = service.getJobById(jobId);
            if (job != null && TarsService.MO_COURSE_CODE.equals(job.getModuleCode())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extracts all jobId values from a URL query string.
     * Handles both ?jobId=... and &jobId=... patterns.
     */
    private List<String> extractJobIds(String link) {
        List<String> ids = new ArrayList<String>();
        int pos = 0;
        while (true) {
            int idx = link.indexOf("jobId=", pos);
            if (idx < 0) break;
            int start = idx + 6;
            int end = link.indexOf('&', start);
            if (end < 0) end = link.length();
            ids.add(link.substring(start, end));
            pos = end + 1;
        }
        return ids;
    }

    private int countUnread(List<Notification> notifications) {
        int count = 0;
        for (Notification n : notifications) {
            if (!n.isRead()) {
                count++;
            }
        }
        return count;
    }
}
