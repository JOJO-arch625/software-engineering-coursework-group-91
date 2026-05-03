<%@ page import="java.util.List,com.group91.tars.model.JobPosting" %>
<%
    List<JobPosting> jobs = (List<JobPosting>) request.getAttribute("jobs");
%>
<%@ include file="../fragments/pageStart.jspf" %>
<section class="view active">
    <article class="panel">
        <div class="panel-header">
            <h4><%= i18n.t("ta.jobs.heading") %></h4>
            <p><%= i18n.t("ta.jobs.description") %></p>
        </div>
        <div class="legend">
            <span class="pill pill-neutral"><%= i18n.t("ta.jobs.max-applications") %></span>
            <span class="pill pill-neutral"><%= i18n.t("ta.jobs.skills-visible") %></span>
        </div>
        <div class="grid one-col" style="margin-top: 18px;">
            <% for (JobPosting jobItem : jobs) { %>
            <article class="job-card">
                <header>
                    <div>
                        <p class="eyebrow"><%= jobItem.getModuleCode() %></p>
                        <h5><%= jobItem.getTitle() %></h5>
                    </div>
                    <span class="status-chip <%= "Open".equals(jobItem.getStatus()) ? "status-open" : "status-rejected" %>"><%= i18n.t("status." + jobItem.getStatus().toLowerCase().replace(" ", "-")) %></span>
                </header>
                <p class="muted"><%= jobItem.getDescription() %></p>
                <div class="job-meta">
                    <span class="pill pill-neutral"><%= jobItem.getSkills() %></span>
                    <span class="pill pill-neutral"><%= jobItem.getWorkload() %></span>
                    <span class="pill pill-neutral"><%= i18n.t("ta.job.detail.deadline") %>: <%= jobItem.getDeadline() %></span>
                </div>
                <div class="button-row">
                    <a class="secondary-button" href="<%= request.getContextPath() %>/ta/job?id=<%= jobItem.getId() %>"><%= i18n.t("common.view-detail") %></a>
                </div>
            </article>
            <% } %>
        </div>
    </article>
</section>
<%@ include file="../fragments/pageEnd.jspf" %>
