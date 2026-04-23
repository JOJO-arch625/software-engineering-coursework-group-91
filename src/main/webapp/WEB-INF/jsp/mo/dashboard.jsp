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
            <span>Open postings</span>
            <strong><%= request.getAttribute("openJobCount") %></strong>
            <small>EBU6304 Software Engineering</small>
        </article>
        <article class="metric-card">
            <span>Total applicants</span>
            <strong><%= applicantCount == null ? 0 : applicantCount %></strong>
            <small>Across all positions</small>
        </article>
        <article class="metric-card">
            <span>Pending review</span>
            <strong><%= pendingCount == null ? 0 : pendingCount %></strong>
            <small>Submitted or under review</small>
        </article>
    </div>

    <article class="panel" style="margin-top: 18px;">
        <div class="panel-header">
            <h4>EBU6304 Software Engineering - Applications</h4>
            <p>Click an applicant to view details and make a decision.</p>
        </div>
        <% if (allApplications == null || allApplications.isEmpty()) { %>
        <div class="alert info">No applications have been submitted yet.</div>
        <% } else { %>
        <div class="table-shell">
            <table>
                <thead>
                <tr>
                    <th>Applicant</th>
                    <th>Position</th>
                    <th>Skills</th>
                    <th>Priority</th>
                    <th>AI Fit</th>
                    <th>Status</th>
                    <th>Submitted</th>
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
                    <td><span class="status-chip <%= statusClass %>"><%= app.getStatus() %></span></td>
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
