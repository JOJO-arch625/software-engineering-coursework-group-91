<%@ page import="java.util.List,com.group91.tars.model.Notification" %>
<%
    List<Notification> notifications = (List<Notification>) request.getAttribute("notifications");
    Integer unreadCount = (Integer) request.getAttribute("unreadCount");
%>
<%@ include file="fragments/pageStart.jspf" %>
<section class="view active">
    <div class="grid two-col">
        <article class="panel">
            <div class="panel-header">
                <h4>Notifications</h4>
                <p>Stay updated on application status changes, review reminders, and system alerts.</p>
            </div>
            <div class="legend">
                <span class="pill pill-warning"><%= unreadCount == null ? 0 : unreadCount %> unread</span>
                <span class="pill pill-neutral"><%= notifications == null ? 0 : notifications.size() %> total</span>
            </div>
            <% if (unreadCount != null && unreadCount > 0) { %>
            <form method="post" action="<%= request.getContextPath() %>/inbox" style="margin-top: 14px;">
                <input type="hidden" name="action" value="markAllRead">
                <button class="secondary-button" type="submit">Mark all as read</button>
            </form>
            <% } %>
            <div class="grid one-col" style="margin-top: 18px;">
                <% if (notifications == null || notifications.isEmpty()) { %>
                <div class="alert info">No notifications yet. They will appear here when your application status changes or when new events occur.</div>
                <% } else { %>
                <% for (Notification notification : notifications) {
                    String categoryClass = "status".equals(notification.getCategory()) ? "status-open"
                        : ("overload".equals(notification.getCategory()) ? "status-overload"
                        : ("review".equals(notification.getCategory()) ? "status-review"
                        : ("deadline".equals(notification.getCategory()) ? "status-near" : "status-open")));
                    String categoryLabel = "status".equals(notification.getCategory()) ? "Status"
                        : ("overload".equals(notification.getCategory()) ? "Overload"
                        : ("review".equals(notification.getCategory()) ? "Review"
                        : ("deadline".equals(notification.getCategory()) ? "Deadline" : notification.getCategory())));
                %>
                <article class="notification-card <%= notification.isRead() ? "notification-read" : "notification-unread" %>">
                    <header>
                        <div>
                            <span class="status-chip <%= categoryClass %>"><%= categoryLabel %></span>
                            <% if (!notification.isRead()) { %>
                            <span class="pill pill-warning" style="margin-left: 8px;">New</span>
                            <% } %>
                        </div>
                        <span class="muted" style="font-size: 13px;"><%= notification.getCreatedAt() %></span>
                    </header>
                    <p><%= notification.getMessage() %></p>
                    <div class="button-row">
                        <% if (notification.getLink() != null && !notification.getLink().isEmpty()) { %>
                        <a class="secondary-button" href="<%= request.getContextPath() %><%= notification.getLink() %>">View details</a>
                        <% } %>
                        <% if (!notification.isRead()) { %>
                        <form method="post" action="<%= request.getContextPath() %>/inbox" style="display: inline;">
                            <input type="hidden" name="action" value="markRead">
                            <input type="hidden" name="notificationId" value="<%= notification.getId() %>">
                            <button class="ghost-button" type="submit">Mark as read</button>
                        </form>
                        <% } %>
                    </div>
                </article>
                <% } %>
                <% } %>
            </div>
        </article>

        <article class="panel">
            <div class="panel-header">
                <h4>Notification Categories</h4>
            </div>
            <ul class="feature-list">
                <li><span class="status-chip status-open">Status</span> Application status changes (Submitted, Under Review, Accepted, Rejected)</li>
                <li><span class="status-chip status-review">Review</span> New applicants for MO review</li>
                <li><span class="status-chip status-overload">Overload</span> Workload cap alerts for TAs and Admin</li>
                <li><span class="status-chip status-near">Deadline</span> Job posting deadline reminders</li>
            </ul>
            <div class="alert info" style="margin-top: 18px;">
                Notifications are generated automatically when application statuses change or workload thresholds are reached.
            </div>
        </article>
    </div>
</section>
<%@ include file="fragments/pageEnd.jspf" %>
