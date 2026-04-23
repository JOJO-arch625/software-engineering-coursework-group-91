<%@ page import="com.group91.tars.model.TAProfile,java.util.List" %>
<%
    TAProfile profile = (TAProfile) request.getAttribute("profile");
    List<String> notifications = (List<String>) request.getAttribute("notifications");
    Integer applicationTotal = (Integer) request.getAttribute("topMetricTwoValue");
    Integer acceptedTotal = (Integer) request.getAttribute("topMetricThreeValue");
%>
<%@ include file="../fragments/pageStart.jspf" %>
<section class="view active">
    <div class="grid two-col">
        <article class="panel">
            <div class="panel-header">
                <h4>Profile readiness</h4>
            </div>
            <p class="dashboard-lead">
                <strong><%= profile != null && profile.getCvPath() != null ? "Complete profile" : "Profile still needs CV" %></strong>
            </p>
            <p class="muted">
                Keep your TA profile updated so module organisers can review your skills, availability, and uploaded CV in one place.
            </p>
            <div class="legend">
                <span class="pill pill-neutral">Applications: <%= applicationTotal == null ? 0 : applicationTotal %> / 3</span>
                <span class="pill pill-neutral">Accepted: <%= acceptedTotal == null ? 0 : acceptedTotal %> / 3</span>
                <span class="pill <%= profile != null && profile.getCvPath() != null ? "pill-success" : "pill-warning" %>">
                    <%= profile != null && profile.getCvPath() != null ? "CV linked" : "CV still required" %>
                </span>
            </div>
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

    <div class="grid one-col">
        <article class="panel">
            <div class="panel-header">
                <h4>Quick Actions</h4>
            </div>
            <div class="button-row">
                <a class="secondary-button" href="<%= request.getContextPath() %>/ta/profile">Update profile</a>
                <a class="secondary-button" href="<%= request.getContextPath() %>/ta/jobs">Browse jobs</a>
                <a class="secondary-button" href="<%= request.getContextPath() %>/ta/applications">View applications</a>
            </div>
            <p class="muted">This workspace now opens through role-based login, so TA tools stay separate from MO and Admin workflows.</p>
        </article>
    </div>
</section>
<%@ include file="../fragments/pageEnd.jspf" %>
