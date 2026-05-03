<%@ page import="java.util.List,com.group91.tars.model.ApplicationRecord,com.group91.tars.model.JobPosting,com.group91.tars.model.TAProfile,com.group91.tars.service.TarsService" %>
<%
    List<JobPosting> jobs = (List<JobPosting>) request.getAttribute("jobs");
    Integer pendingCount = (Integer) request.getAttribute("pendingCount");
    Integer applicantCount = (Integer) request.getAttribute("applicantCount");
    List<ApplicationRecord> allApplications = (List<ApplicationRecord>) request.getAttribute("allApplications");
    TarsService pageService = TarsService.getInstance();
%>
<%@ include file="../fragments/pageStart.jspf" %>
<section class="view active">
    <div class="grid three-col">
        <article class="metric-card">
            <span><%= i18n.t("mo.dashboard.open-postings") %></span>
            <strong><%= request.getAttribute("openJobCount") %></strong>
            <small><%= i18n.t("mo.dashboard.open-postings-subtitle") %></small>
        </article>
        <article class="metric-card">
            <span><%= i18n.t("mo.dashboard.total-applicants") %></span>
            <strong><%= applicantCount == null ? 0 : applicantCount %></strong>
            <small><%= i18n.t("mo.dashboard.total-applicants-subtitle") %></small>
        </article>
        <article class="metric-card">
            <span><%= i18n.t("mo.dashboard.pending-review") %></span>
            <strong><%= pendingCount == null ? 0 : pendingCount %></strong>
            <small><%= i18n.t("mo.dashboard.pending-review-subtitle") %></small>
        </article>
    </div>

    <article class="panel" style="margin-top: 18px;">
        <div class="panel-header">
            <h4><%= i18n.t("mo.dashboard.panel-heading") %></h4>
            <p><%= i18n.t("mo.dashboard.description") %></p>
        </div>
        <% if (allApplications == null || allApplications.isEmpty()) { %>
        <div class="alert info"><%= i18n.t("mo.dashboard.no-applications") %></div>
        <% } else { %>
        <div class="table-shell">
            <table>
                <thead>
                <tr>
                    <th><%= i18n.t("mo.dashboard.applicant") %></th>
                    <th><%= i18n.t("mo.dashboard.position") %></th>
                    <th><%= i18n.t("mo.dashboard.skills") %></th>
                    <th><%= i18n.t("mo.dashboard.priority") %></th>
                    <th><%= i18n.t("mo.dashboard.ai-fit") %></th>
                    <th><%= i18n.t("mo.dashboard.status") %></th>
                    <th><%= i18n.t("mo.dashboard.submitted") %></th>
                </tr>
                </thead>
                <tbody>
                <% for (ApplicationRecord app : allApplications) {
                    JobPosting job = pageService.getJobById(app.getJobId());
                    TAProfile applicant = pageService.getProfileById(app.getTaId());
                    String statusClass = "Submitted".equals(app.getStatus()) ? "status-open"
                        : ("Under Review".equals(app.getStatus()) ? "status-review"
                        : ("Accepted".equals(app.getStatus()) ? "status-accepted" : "status-rejected"));
                    int fitScore = job == null ? 0 : pageService.calculateFitScore(app.getTaId(), job.getId());
                    String skills = app.getApplicantSkills() == null || app.getApplicantSkills().isEmpty()
                        ? (applicant == null ? "0" : applicant.getSkills()) : app.getApplicantSkills();
                    if (skills != null && skills.length() > 30) {
                        skills = skills.substring(0, 30) + "...";
                    }
                %>
                <tr>
                    <td><a class="table-link" href="<%= request.getContextPath() %>/mo/review?jobId=<%= app.getJobId() %>&appId=<%= app.getId() %>"><%= applicant == null ? app.getTaId() : applicant.getFullName() %></a></td>
                    <td><%= job == null ? "0" : job.getTitle().replace("Software Engineering TA - ", "") %></td>
                    <td><span class="muted"><%= skills %></span></td>
                    <td><%= app.getPriority() %></td>
                    <td><strong><%= fitScore %>%</strong></td>
                    <td><span class="status-chip <%= statusClass %>"><%= i18n.t("status." + app.getStatus().toLowerCase().replace(" ", "-")) %></span></td>
                    <td><%= app.getSubmittedAt() == null ? "0" : app.getSubmittedAt() %></td>
                </tr>
                <% } %>
                </tbody>
            </table>
        </div>
        <% } %>
    </article>
</section>
<%@ include file="../fragments/pageEnd.jspf" %>
