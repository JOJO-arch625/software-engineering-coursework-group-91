<%@ page import="com.group91.tars.model.TAProfile,java.util.List" %>
<%
    TAProfile profile = (TAProfile) request.getAttribute("profile");
    List<String> notifications = (List<String>) request.getAttribute("notifications");
%>
<%@ include file="../fragments/pageStart.jspf" %>
<section class="view active">
    <div class="grid three-col">
        <article class="metric-card">
            <span>Profile status</span>
            <strong><%= profile != null && profile.getCvPath() != null ? "Complete" : "Incomplete" %></strong>
            <small>Ready for TA applications</small>
        </article>
        <article class="metric-card">
            <span>Applications used</span>
            <strong><%= request.getAttribute("applicationCount") %> / 3</strong>
            <small>Maximum three applications allowed</small>
        </article>
        <article class="metric-card">
            <span>Accepted offers</span>
            <strong><%= request.getAttribute("acceptedCount") %> / 3</strong>
            <small>Visible workload cap for transparency</small>
        </article>
    </div>

    <div class="grid two-col">
        <article class="panel">
            <div class="panel-header">
                <h4>Quick Actions</h4>
            </div>
            <div class="button-row">
                <a class="secondary-button" href="<%= request.getContextPath() %>/ta/profile">Update profile</a>
                <a class="secondary-button" href="<%= request.getContextPath() %>/ta/jobs">Browse jobs</a>
                <a class="secondary-button" href="<%= request.getContextPath() %>/ta/applications">View applications</a>
            </div>
            <p class="muted">Login is intentionally simplified in this checkpoint. Role-based entry stands in for authentication.</p>
        </article>
        <article class="panel">
            <div class="panel-header">
                <h4>Latest Notifications</h4>
            </div>
            <ul class="feature-list">
                <% for (String item : notifications) { %>
                <li><%= item %></li>
                <% } %>
            </ul>
        </article>
    </div>
</section>
<%@ include file="../fragments/pageEnd.jspf" %>
