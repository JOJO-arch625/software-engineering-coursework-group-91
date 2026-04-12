<%@ page import="java.util.List,com.group91.tars.model.Notification" %>
<%
    List<String> notifications = (List<String>) request.getAttribute("notifications");
    List<Notification> recentNotifications = (List<Notification>) request.getAttribute("recentNotifications");
    List<String> aiTodos = (List<String>) request.getAttribute("aiTodos");
%>
<%@ include file="fragments/pageStart.jspf" %>
<section class="view active" id="gateway">
    <article class="hero-card">
        <div>
            <p class="eyebrow">Workflow Entry</p>
            <h3>Transparent TA recruitment without database complexity</h3>
            <p class="muted">
                This working build turns the approved prototype into a Java Servlet/JSP application with local JSON storage and local CV files.
            </p>
        </div>
        <div class="hero-meta">
            <span class="pill pill-neutral">Servlet / JSP runtime</span>
            <span class="pill pill-neutral">JSON + local uploads</span>
            <span class="pill pill-warning">AI reserved for teammates</span>
        </div>
    </article>

    <div class="role-grid">
        <article class="role-card accent-blue">
            <p class="eyebrow">TA</p>
            <h4>Apply with profile, CV, and ranked preferences</h4>
            <p>Track application progress directly instead of waiting for hidden spreadsheet updates.</p>
            <div class="button-row">
                <a class="primary-button" href="<%= request.getContextPath() %>/ta/dashboard">Open TA flow</a>
            </div>
        </article>
        <article class="role-card accent-amber">
            <p class="eyebrow">MO</p>
            <h4>Publish roles and review applicants in one place</h4>
            <p>Check skills, CV references, and current TA workload before making a decision.</p>
            <div class="button-row">
                <a class="primary-button" href="<%= request.getContextPath() %>/mo/dashboard">Open MO flow</a>
            </div>
        </article>
        <article class="role-card accent-green">
            <p class="eyebrow">Admin</p>
            <h4>Monitor accepted jobs and workload balance</h4>
            <p>See all accepted allocations and identify overload risk before more offers are confirmed.</p>
            <div class="button-row">
                <a class="primary-button" href="<%= request.getContextPath() %>/admin/workload">Open Admin flow</a>
            </div>
        </article>
    </div>

    <div class="grid two-col">
        <article class="panel">
            <div class="panel-header">
                <h4>Current Working Build Coverage</h4>
            </div>
            <ul class="feature-list">
                <li>TA profile maintenance and local CV upload</li>
                <li>Job browsing, job detail, and capped application submission</li>
                <li>MO posting creation, editing, and closure</li>
                <li>MO applicant review with status updates</li>
                <li>Admin workload summary based on accepted applications</li>
            </ul>
        </article>
        <article class="panel">
            <div class="panel-header">
                <h4>Recent Inbox Notifications</h4>
            </div>
            <ul class="feature-list">
                <% if (recentNotifications != null && !recentNotifications.isEmpty()) { %>
                    <% int count = 0; for (Notification n : recentNotifications) { if (count++ >= 5) break; %>
                    <li><strong><%= n.getTitle() %></strong>: <%= n.getMessage() %> (<%= n.getTimestamp() %>)</li>
                    <% } %>
                    <li style="list-style: none; margin-top: 8px;"><a href="<%= request.getContextPath() %>/inbox" style="color: var(--magenta); font-weight: 700;">View all in Inbox &rarr;</a></li>
                <% } else { %>
                    <% for (String item : notifications) { %>
                    <li><%= item %></li>
                    <% } %>
                <% } %>
            </ul>
        </article>
    </div>

    <div class="grid two-col">
        <article class="panel">
            <div class="panel-header">
                <h4>Core Rules Already Enforced</h4>
            </div>
            <ul class="feature-list">
                <li>TA can apply to at most three jobs</li>
                <li>Closed postings reject new applications</li>
                <li>Rejected applications remain visible to the TA</li>
                <li>Admin highlights workload risk at the threshold</li>
            </ul>
        </article>
        <article class="panel">
            <div class="panel-header">
                <h4>Reserved TODO Space</h4>
            </div>
            <ul class="feature-list">
                <% for (String item : aiTodos) { %>
                <li><%= item %></li>
                <% } %>
            </ul>
        </article>
    </div>
</section>
<%@ include file="fragments/pageEnd.jspf" %>
