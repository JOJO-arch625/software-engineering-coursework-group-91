<%@ page import="java.util.List,com.group91.tars.model.JobPosting,com.group91.tars.model.ApplicationRecord,com.group91.tars.model.TAProfile,com.group91.tars.service.TarsService" %>
<%
    String query = (String) request.getAttribute("query");
    String category = (String) request.getAttribute("category");
    List<JobPosting> jobResults = (List<JobPosting>) request.getAttribute("jobResults");
    List<ApplicationRecord> applicationResults = (List<ApplicationRecord>) request.getAttribute("applicationResults");
    List<TAProfile> profileResults = (List<TAProfile>) request.getAttribute("profileResults");
    Integer jobResultCount = (Integer) request.getAttribute("jobResultCount");
    Integer applicationResultCount = (Integer) request.getAttribute("applicationResultCount");
    Integer profileResultCount = (Integer) request.getAttribute("profileResultCount");
    TarsService pageService = TarsService.getInstance();
%>
<%@ include file="fragments/pageStart.jspf" %>
<section class="view active">
    <article class="panel">
        <div class="panel-header">
            <h4><%= i18n.t("search.heading") %></h4>
            <p><%= i18n.t("search.description") %></p>
        </div>
        <form method="get" action="<%= request.getContextPath() %>/search" class="form-grid">
            <label class="span-two">
                <%= i18n.t("search.keywords") %>
                <input type="text" name="q" value="<%= query %>" placeholder="<%= i18n.t("search.keywords-placeholder") %>" autofocus>
            </label>
            <label>
                <%= i18n.t("search.category") %>
                <select name="category">
                    <option value="all" <%= "all".equals(category) ? "selected" : "" %>><%= i18n.t("search.category-all") %></option>
                    <option value="jobs" <%= "jobs".equals(category) ? "selected" : "" %>><%= i18n.t("search.category-jobs") %></option>
                    <option value="applications" <%= "applications".equals(category) ? "selected" : "" %>><%= i18n.t("search.category-applications") %></option>
                    <% if ("MO".equals(currentRole) || "ADMIN".equals(currentRole)) { %>
                    <option value="applicants" <%= "applicants".equals(category) ? "selected" : "" %>><%= i18n.t("search.category-applicants") %></option>
                    <% } %>
                </select>
            </label>
            <div class="button-row">
                <button class="primary-button" type="submit"><%= i18n.t("common.search") %></button>
                <a class="ghost-button" href="<%= request.getContextPath() %>/search"><%= i18n.t("common.clear") %></a>
            </div>
        </form>
    </article>

    <% if (query != null && !query.trim().isEmpty()) { %>
    <div class="legend" style="margin-top: 18px;">
        <span class="pill pill-neutral"><%= i18n.t("search.jobs") %>: <%= jobResultCount == null ? 0 : jobResultCount %></span>
        <span class="pill pill-neutral"><%= i18n.t("search.applications") %>: <%= applicationResultCount == null ? 0 : applicationResultCount %></span>
        <% if ("MO".equals(currentRole) || "ADMIN".equals(currentRole)) { %>
        <span class="pill pill-neutral"><%= i18n.t("search.applicants") %>: <%= profileResultCount == null ? 0 : profileResultCount %></span>
        <% } %>
    </div>

    <% if ("TA".equals(currentRole)) { %>
    <% if (!jobResults.isEmpty()) { %>
    <article class="panel" style="margin-top: 18px;">
        <div class="panel-header">
            <h4><%= i18n.t("search.available-jobs") %></h4>
            <p><%= i18n.t("search.available-jobs-desc") %></p>
        </div>
        <div class="grid one-col">
            <% for (JobPosting job : jobResults) { %>
            <article class="job-card">
                <header>
                    <div>
                        <p class="eyebrow"><%= job.getModuleCode() %></p>
                        <h5><%= job.getTitle() %></h5>
                    </div>
                    <span class="status-chip status-open"><%= i18n.t("status." + job.getStatus().toLowerCase().replace(" ", "-")) %></span>
                </header>
                <p class="muted"><%= job.getDescription() %></p>
                <div class="job-meta">
                    <span class="pill pill-neutral"><%= job.getSkills() %></span>
                    <span class="pill pill-neutral"><%= job.getWorkload() %></span>
                    <span class="pill pill-neutral"><%= i18n.t("ta.job.detail.deadline") %>: <%= job.getDeadline() %></span>
                </div>
                <div class="button-row">
                    <a class="primary-button" href="<%= request.getContextPath() %>/ta/job?id=<%= job.getId() %>"><%= i18n.t("search.view-apply") %></a>
                </div>
            </article>
            <% } %>
        </div>
    </article>
    <% } %>
    <% if (!applicationResults.isEmpty()) { %>
    <article class="panel" style="margin-top: 18px;">
        <div class="panel-header">
            <h4><%= i18n.t("search.my-applications") %></h4>
        </div>
        <div class="table-shell">
            <table>
                <thead>
                <tr>
                    <th><%= i18n.t("search.module") %></th>
                    <th><%= i18n.t("search.job-title") %></th>
                    <th><%= i18n.t("search.priority") %></th>
                    <th><%= i18n.t("search.status") %></th>
                    <th><%= i18n.t("search.notes") %></th>
                </tr>
                </thead>
                <tbody>
                <% for (ApplicationRecord record : applicationResults) {
                    String statusClass = "Submitted".equals(record.getStatus()) ? "status-open"
                        : ("Under Review".equals(record.getStatus()) ? "status-review"
                        : ("Shortlisted".equals(record.getStatus()) ? "status-shortlisted"
                        : ("Accepted".equals(record.getStatus()) ? "status-accepted" : "status-rejected")));
                %>
                <tr>
                    <td><strong><%= pageService.getJobModuleCode(record.getJobId()) %></strong></td>
                    <td><%= pageService.getJobTitle(record.getJobId()) %></td>
                    <td><%= record.getPriority() %></td>
                    <td><span class="status-chip <%= statusClass %>"><%= i18n.t("status." + record.getStatus().toLowerCase().replace(" ", "-")) %></span></td>
                    <td><%= record.getNotes() %></td>
                </tr>
                <% } %>
                </tbody>
            </table>
        </div>
        <div class="button-row" style="margin-top: 14px;">
            <a class="secondary-button" href="<%= request.getContextPath() %>/ta/applications"><%= i18n.t("search.view-all-applications") %></a>
        </div>
    </article>
    <% } %>
    <% } %>

    <% if ("MO".equals(currentRole)) { %>
    <% if (!jobResults.isEmpty()) { %>
    <article class="panel" style="margin-top: 18px;">
        <div class="panel-header">
            <h4><%= i18n.t("search.job-postings") %></h4>
            <p><%= i18n.t("search.job-postings-desc") %></p>
        </div>
        <div class="grid one-col">
            <% for (JobPosting job : jobResults) { %>
            <article class="job-card">
                <header>
                    <div>
                        <p class="eyebrow"><%= job.getModuleCode() %></p>
                        <h5><%= job.getTitle() %></h5>
                    </div>
                    <span class="status-chip <%= "Open".equals(job.getStatus()) ? "status-open" : "status-rejected" %>"><%= i18n.t("status." + job.getStatus().toLowerCase().replace(" ", "-")) %></span>
                </header>
                <p class="muted"><%= job.getDescription() %></p>
                <div class="job-meta">
                    <span class="pill pill-neutral"><%= job.getSkills() %></span>
                    <span class="pill pill-neutral"><%= job.getWorkload() %></span>
                    <span class="pill pill-neutral"><%= i18n.t("search.applicants") %>: <%= pageService.countApplicantsForJob(job.getId()) %></span>
                </div>
                <div class="button-row">
                    <a class="primary-button" href="<%= request.getContextPath() %>/mo/review?jobId=<%= job.getId() %>"><%= i18n.t("search.review-applicants") %></a>
                    <a class="ghost-button" href="<%= request.getContextPath() %>/mo/jobs/edit?id=<%= job.getId() %>"><%= i18n.t("search.edit-posting") %></a>
                </div>
            </article>
            <% } %>
        </div>
    </article>
    <% } %>
    <% if (!applicationResults.isEmpty()) { %>
    <article class="panel" style="margin-top: 18px;">
        <div class="panel-header">
            <h4><%= i18n.t("search.applications") %></h4>
        </div>
        <div class="table-shell">
            <table>
                <thead>
                <tr>
                    <th><%= i18n.t("search.module") %></th>
                    <th><%= i18n.t("search.applicant") %></th>
                    <th><%= i18n.t("search.priority") %></th>
                    <th><%= i18n.t("search.status") %></th>
                    <th><%= i18n.t("search.action") %></th>
                </tr>
                </thead>
                <tbody>
                <% for (ApplicationRecord record : applicationResults) {
                    String statusClass = "Submitted".equals(record.getStatus()) ? "status-open"
                        : ("Under Review".equals(record.getStatus()) ? "status-review"
                        : ("Shortlisted".equals(record.getStatus()) ? "status-shortlisted"
                        : ("Accepted".equals(record.getStatus()) ? "status-accepted" : "status-rejected")));
                    TAProfile applicant = pageService.getProfileById(record.getTaId());
                %>
                <tr>
                    <td><strong><%= pageService.getJobModuleCode(record.getJobId()) %></strong><br><span class="muted"><%= pageService.getJobTitle(record.getJobId()) %></span></td>
                    <td><%= applicant == null ? record.getTaId() : applicant.getFullName() %></td>
                    <td><%= record.getPriority() %></td>
                    <td><span class="status-chip <%= statusClass %>"><%= i18n.t("status." + record.getStatus().toLowerCase().replace(" ", "-")) %></span></td>
                    <td><a class="secondary-button" href="<%= request.getContextPath() %>/mo/review?jobId=<%= record.getJobId() %>&appId=<%= record.getId() %>"><%= i18n.t("search.review") %></a></td>
                </tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </article>
    <% } %>
    <% if (!profileResults.isEmpty()) { %>
    <article class="panel" style="margin-top: 18px;">
        <div class="panel-header">
            <h4><%= i18n.t("search.applicant-profiles") %></h4>
        </div>
        <div class="table-shell">
            <table>
                <thead>
                <tr>
                    <th><%= i18n.t("search.name") %></th>
                    <th><%= i18n.t("search.student-no") %></th>
                    <th><%= i18n.t("search.skills") %></th>
                    <th><%= i18n.t("search.accepted-jobs") %></th>
                </tr>
                </thead>
                <tbody>
                <% for (TAProfile profile : profileResults) { %>
                <tr>
                    <td><strong><%= profile.getFullName() %></strong></td>
                    <td><%= profile.getStudentNumber() %></td>
                    <td><%= profile.getSkills() %></td>
                    <td><%= pageService.countAcceptedJobsForTaPublic(profile.getId()) %> / 3</td>
                </tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </article>
    <% } %>
    <% } %>

    <% if ("ADMIN".equals(currentRole)) { %>
    <% if (!jobResults.isEmpty()) { %>
    <article class="panel" style="margin-top: 18px;">
        <div class="panel-header">
            <h4><%= i18n.t("search.job-postings") %></h4>
        </div>
        <div class="table-shell">
            <table>
                <thead>
                <tr>
                    <th><%= i18n.t("search.module") %></th>
                    <th><%= i18n.t("search.title") %></th>
                    <th><%= i18n.t("search.status") %></th>
                    <th><%= i18n.t("search.applicants") %></th>
                </tr>
                </thead>
                <tbody>
                <% for (JobPosting job : jobResults) { %>
                <tr>
                    <td><strong><%= job.getModuleCode() %></strong></td>
                    <td><%= job.getTitle() %></td>
                    <td><span class="status-chip <%= "Open".equals(job.getStatus()) ? "status-open" : "status-rejected" %>"><%= i18n.t("status." + job.getStatus().toLowerCase().replace(" ", "-")) %></span></td>
                    <td><%= pageService.countApplicantsForJob(job.getId()) %></td>
                </tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </article>
    <% } %>
    <% if (!applicationResults.isEmpty()) { %>
    <article class="panel" style="margin-top: 18px;">
        <div class="panel-header">
            <h4><%= i18n.t("search.applications") %></h4>
        </div>
        <div class="table-shell">
            <table>
                <thead>
                <tr>
                    <th><%= i18n.t("search.module") %></th>
                    <th><%= i18n.t("search.applicant") %></th>
                    <th><%= i18n.t("search.status") %></th>
                </tr>
                </thead>
                <tbody>
                <% for (ApplicationRecord record : applicationResults) {
                    String statusClass = "Submitted".equals(record.getStatus()) ? "status-open"
                        : ("Under Review".equals(record.getStatus()) ? "status-review"
                        : ("Shortlisted".equals(record.getStatus()) ? "status-shortlisted"
                        : ("Accepted".equals(record.getStatus()) ? "status-accepted" : "status-rejected")));
                    TAProfile applicant = pageService.getProfileById(record.getTaId());
                %>
                <tr>
                    <td><strong><%= pageService.getJobModuleCode(record.getJobId()) %></strong></td>
                    <td><%= applicant == null ? record.getTaId() : applicant.getFullName() %></td>
                    <td><span class="status-chip <%= statusClass %>"><%= i18n.t("status." + record.getStatus().toLowerCase().replace(" ", "-")) %></span></td>
                </tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </article>
    <% } %>
    <% if (!profileResults.isEmpty()) { %>
    <article class="panel" style="margin-top: 18px;">
        <div class="panel-header">
            <h4><%= i18n.t("search.applicant-profiles") %></h4>
        </div>
        <div class="table-shell">
            <table>
                <thead>
                <tr>
                    <th><%= i18n.t("search.name") %></th>
                    <th><%= i18n.t("search.student-no") %></th>
                    <th><%= i18n.t("search.skills") %></th>
                    <th><%= i18n.t("search.workload") %></th>
                </tr>
                </thead>
                <tbody>
                <% for (TAProfile profile : profileResults) { %>
                <tr>
                    <td><strong><%= profile.getFullName() %></strong></td>
                    <td><%= profile.getStudentNumber() %></td>
                    <td><%= profile.getSkills() %></td>
                    <td><%= pageService.countAcceptedJobsForTaPublic(profile.getId()) %> / 3</td>
                </tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </article>
    <% } %>
    <% } %>

    <% if (jobResults.isEmpty() && applicationResults.isEmpty() && profileResults.isEmpty()) { %>
    <div class="alert info" style="margin-top: 18px;">
        <%= i18n.t("search.no-results", query) %>
    </div>
    <% } %>
    <% } else { %>
    <article class="panel" style="margin-top: 18px;">
        <div class="panel-header">
            <h4><%= i18n.t("search.tips-heading") %></h4>
        </div>
        <ul class="feature-list">
            <% if ("TA".equals(currentRole)) { %>
            <li><%= i18n.t("search.tips-ta-1") %></li>
            <li><%= i18n.t("search.tips-ta-2") %></li>
            <li><%= i18n.t("search.tips-ta-3") %></li>
            <% } else if ("MO".equals(currentRole)) { %>
            <li><%= i18n.t("search.tips-mo-1") %></li>
            <li><%= i18n.t("search.tips-mo-2") %></li>
            <li><%= i18n.t("search.tips-mo-3") %></li>
            <% } else { %>
            <li><%= i18n.t("search.tips-admin-1") %></li>
            <li><%= i18n.t("search.tips-admin-2") %></li>
            <li><%= i18n.t("search.tips-admin-3") %></li>
            <% } %>
        </ul>
    </article>
    <% } %>
</section>
<%@ include file="fragments/pageEnd.jspf" %>
