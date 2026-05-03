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
                <h4><%= i18n.t("inbox.heading") %></h4>
                <p><%= i18n.t("inbox.description") %></p>
            </div>
            <div class="legend">
                <span class="pill pill-warning"><%= i18n.t("inbox.unread", unreadCount == null ? 0 : unreadCount) %></span>
                <span class="pill pill-neutral"><%= i18n.t("inbox.total", notifications == null ? 0 : notifications.size()) %></span>
            </div>
            <% if (unreadCount != null && unreadCount > 0) { %>
            <form method="post" action="<%= request.getContextPath() %>/inbox" style="margin-top: 14px;">
                <input type="hidden" name="action" value="markAllRead">
                <button class="secondary-button" type="submit"><%= i18n.t("common.mark-all-read") %></button>
            </form>
            <% } %>
            <div class="grid one-col" style="margin-top: 18px;">
                <% if (notifications == null || notifications.isEmpty()) { %>
                <div class="alert info"><%= i18n.t("inbox.empty") %></div>
                <% } else { %>
                <% for (Notification notification : notifications) {
                    String categoryClass = "status".equals(notification.getCategory()) ? "status-open"
                        : ("overload".equals(notification.getCategory()) ? "status-overload"
                        : ("review".equals(notification.getCategory()) ? "status-review"
                        : ("deadline".equals(notification.getCategory()) ? "status-near" : "status-open")));
                    String categoryLabel = i18n.t("status.category." + notification.getCategory());
                %>
                <article class="notification-card <%= notification.isRead() ? "notification-read" : "notification-unread" %>">
                    <header>
                        <div>
                            <span class="status-chip <%= categoryClass %>"><%= categoryLabel %></span>
                            <% if (!notification.isRead()) { %>
                            <span class="pill pill-warning" style="margin-left: 8px;"><%= i18n.t("status.new") %></span>
                            <% } %>
                        </div>
                        <span class="muted" style="font-size: 13px;"><%= notification.getCreatedAt() %></span>
                    </header>
                    <p><%= notification.getMessage() %></p>
                    <div class="button-row">
                        <% if (notification.getLink() != null && !notification.getLink().isEmpty()) { %>
                        <a class="secondary-button" href="<%= request.getContextPath() %><%= notification.getLink() %>"><%= i18n.t("inbox.view-details") %></a>
                        <% } %>
                        <% if (!notification.isRead()) { %>
                        <form method="post" action="<%= request.getContextPath() %>/inbox" style="display: inline;">
                            <input type="hidden" name="action" value="markRead">
                            <input type="hidden" name="notificationId" value="<%= notification.getId() %>">
                            <button class="ghost-button" type="submit"><%= i18n.t("common.mark-read") %></button>
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
                <h4><%= i18n.t("inbox.categories-heading") %></h4>
            </div>
            <ul class="feature-list">
                <li><span class="status-chip status-open"><%= i18n.t("status.category.status") %></span> <%= i18n.t("inbox.categories-status") %></li>
                <li><span class="status-chip status-review"><%= i18n.t("status.category.review") %></span> <%= i18n.t("inbox.categories-review") %></li>
                <li><span class="status-chip status-overload"><%= i18n.t("status.category.overload") %></span> <%= i18n.t("inbox.categories-overload") %></li>
                <li><span class="status-chip status-near"><%= i18n.t("status.category.deadline") %></span> <%= i18n.t("inbox.categories-deadline") %></li>
            </ul>
            <div class="alert info" style="margin-top: 18px;">
                <%= i18n.t("inbox.categories-alert") %>
            </div>
        </article>
    </div>
</section>
<%@ include file="fragments/pageEnd.jspf" %>
