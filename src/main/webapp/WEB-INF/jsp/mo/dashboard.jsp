<%@ page import="java.util.List,com.group91.tars.model.JobPosting,com.group91.tars.service.TarsService" %>
<%
    List<JobPosting> jobs = (List<JobPosting>) request.getAttribute("jobs");
    Integer pendingCount = (Integer) request.getAttribute("pendingCount");
    Integer applicantCount = (Integer) request.getAttribute("applicantCount");
    TarsService pageService = TarsService.getInstance();
%>
<%@ include file="../fragments/pageStart.jspf" %>
<section class="view active">
    <div class="grid three-col">
        <article class="metric-card">
            <span>Open postings</span>
            <strong><%= request.getAttribute("openJobCount") %></strong>
            <small>Visible to TA applicants</small>
        </article>
        <article class="metric-card">
            <span>Total applicants</span>
            <strong><%= applicantCount == null ? 0 : applicantCount %></strong>
            <small>Across active modules</small>
        </article>
        <article class="metric-card">
            <span>Pending review</span>
            <strong><%= pendingCount == null ? 0 : pendingCount %></strong>
            <small>Submitted or under review</small>
        </article>
    </div>

    <div class="grid two-col">
        <article class="panel">
            <div class="panel-header">
                <h4>My Job Postings</h4>
            </div>
            <div class="button-row">
                <a class="secondary-button" href="<%= request.getContextPath() %>/mo/jobs/edit">Create posting</a>
            </div>
            <div class="grid one-col">
                <% for (JobPosting job : jobs) { %>
                <article class="posting-card">
                    <header>
                        <div>
                            <p class="eyebrow"><%= job.getModuleCode() %></p>
                            <h5><%= job.getTitle() %></h5>
                        </div>
                        <span class="status-chip <%= "Open".equals(job.getStatus()) ? "status-open" : "status-rejected" %>"><%= job.getStatus() %></span>
                    </header>
                    <p class="muted"><%= job.getSkills() %></p>
                    <p><%= pageService.countApplicantsForJob(job.getId()) %> applicants</p>
                    <div class="button-row">
                        <a class="secondary-button" href="<%= request.getContextPath() %>/mo/jobs/edit?id=<%= job.getId() %>">Open detail</a>
                        <a class="ghost-button" href="<%= request.getContextPath() %>/mo/review?jobId=<%= job.getId() %>">Review applicants</a>
                    </div>
                </article>
                <% } %>
            </div>
        </article>
        <article class="panel">
            <div class="panel-header">
                <h4>Decision Support</h4>
            </div>
            <ul class="feature-list">
                <li>See applicant skill summary beside each posting</li>
                <li>Check current accepted job count before accepting a TA</li>
                <li>Close a posting to stop new applications</li>
                <li>Use clear reject status instead of silent rejection</li>
            </ul>
        </article>
    </div>
</section>
<%@ include file="../fragments/pageEnd.jspf" %>
