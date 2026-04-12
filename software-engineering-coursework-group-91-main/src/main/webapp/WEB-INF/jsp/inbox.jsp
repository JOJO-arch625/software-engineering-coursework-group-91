<%@ page import="java.util.List,com.group91.tars.model.Notification" %>
<%
    List<Notification> notifications = (List<Notification>) request.getAttribute("notifications");
%>
<%@ include file="../fragments/pageStart.jspf" %>
<section class="view active">
    <article class="panel">
        <div class="panel-header">
            <h4>Notification Inbox</h4>
            <p>Stay updated with status changes, review reminders, and system alerts.</p>
        </div>

        <div class="grid one-col" style="margin-top: 24px;">
            <% if (notifications != null && !notifications.isEmpty()) { %>
                <% for (Notification n : notifications) { %>
                <article class="job-card <%= n.isRead() ? "" : "unread-notification" %>" style="border-left: 4px solid <%= n.isRead() ? "var(--border)" : "var(--magenta)" %>;">
                    <header>
                        <div>
                            <p class="eyebrow"><%= n.getTimestamp() %></p>
                            <h5><%= n.getTitle() %></h5>
                        </div>
                        <% if (!n.isRead()) { %>
                            <span class="status-chip status-open">New</span>
                        <% } %>
                    </header>
                    <p class="muted"><%= n.getMessage() %></p>
                    <div class="button-row">
                        <% if (!n.isRead()) { %>
                            <a class="primary-button" href="<%= request.getContextPath() %>/inbox?action=read&id=<%= n.getId() %>">Mark as read</a>
                        <% } else { %>
                            <span class="muted">Already read</span>
                        <% } %>
                    </div>
                </article>
                <% } %>
            <% } else { %>
                <div class="panel-header">
                    <p class="muted">Your inbox is empty. We'll notify you here when there are updates.</p>
                </div>
            <% } %>
        </div>
    </article>
</section>
<%@ include file="../fragments/pageEnd.jspf" %>
