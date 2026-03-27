<%@ page import="java.util.List,com.group91.tars.model.JobPosting" %>
<%
    List<JobPosting> jobs = (List<JobPosting>) request.getAttribute("jobs");
%>
<%@ include file="../fragments/pageStart.jspf" %>
<section class="view active">
    <article class="panel">
        <div class="panel-header">
            <h4>Available Jobs</h4>
            <p>Only open postings can be applied for, but closed roles remain visible for transparency.</p>
        </div>
        <div class="legend">
            <span class="pill pill-neutral">Max applications: 3</span>
            <span class="pill pill-neutral">Skills visible before applying</span>
        </div>
        <div class="grid one-col" style="margin-top: 18px;">
            <% for (JobPosting job : jobs) { %>
            <article class="job-card">
                <header>
                    <div>
                        <p class="eyebrow"><%= job.getModuleCode() %></p>
                        <h5><%= job.getTitle() %></h5>
                    </div>
                    <span class="status-chip <%= "Open".equals(job.getStatus()) ? "status-open" : "status-rejected" %>"><%= job.getStatus() %></span>
                </header>
                <p class="muted"><%= job.getDescription() %></p>
                <div class="job-meta">
                    <span class="pill pill-neutral"><%= job.getSkills() %></span>
                    <span class="pill pill-neutral"><%= job.getWorkload() %></span>
                    <span class="pill pill-neutral">Deadline: <%= job.getDeadline() %></span>
                </div>
                <div class="button-row">
                    <a class="secondary-button" href="<%= request.getContextPath() %>/ta/job?id=<%= job.getId() %>">View detail</a>
                </div>
            </article>
            <% } %>
        </div>
    </article>
</section>
<%@ include file="../fragments/pageEnd.jspf" %>
