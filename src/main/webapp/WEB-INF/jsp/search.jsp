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
            <h4>Global Search</h4>
            <p>Search across jobs, applications, and applicant profiles.</p>
        </div>
        <form method="get" action="<%= request.getContextPath() %>/search" class="form-grid">
            <label class="span-two">
                Search keywords
                <input type="text" name="q" value="<%= query %>" placeholder="Enter module code, skill, name, or status..." autofocus>
            </label>
            <label>
                Category
                <select name="category">
                    <option value="all" <%= "all".equals(category) ? "selected" : "" %>>All</option>
                    <option value="jobs" <%= "jobs".equals(category) ? "selected" : "" %>>Jobs</option>
                    <option value="applications" <%= "applications".equals(category) ? "selected" : "" %>>Applications</option>
                    <% if ("MO".equals(currentRole) || "ADMIN".equals(currentRole)) { %>
                    <option value="applicants" <%= "applicants".equals(category) ? "selected" : "" %>>Applicants</option>
                    <% } %>
                </select>
            </label>
            <div class="button-row">
                <button class="primary-button" type="submit">Search</button>
                <a class="ghost-button" href="<%= request.getContextPath() %>/search">Clear</a>
            </div>
        </form>
    </article>

    <% if (query != null && !query.trim().isEmpty()) { %>
    <div class="legend" style="margin-top: 18px;">
        <span class="pill pill-neutral">Jobs: <%= jobResultCount == null ? 0 : jobResultCount %></span>
        <span class="pill pill-neutral">Applications: <%= applicationResultCount == null ? 0 : applicationResultCount %></span>
        <% if ("MO".equals(currentRole) || "ADMIN".equals(currentRole)) { %>
        <span class="pill pill-neutral">Applicants: <%= profileResultCount == null ? 0 : profileResultCount %></span>
        <% } %>
    </div>

    <% if ("TA".equals(currentRole)) { %>
    <% if (!jobResults.isEmpty()) { %>
    <article class="panel" style="margin-top: 18px;">
        <div class="panel-header">
            <h4>Available Jobs</h4>
            <p>Click to view details and apply.</p>
        </div>
        <div class="grid one-col">
            <% for (JobPosting job : jobResults) { %>
            <article class="job-card">
                <header>
                    <div>
                        <p class="eyebrow"><%= job.getModuleCode() %></p>
                        <h5><%= job.getTitle() %></h5>
                    </div>
                    <span class="status-chip status-open"><%= job.getStatus() %></span>
                </header>
                <p class="muted"><%= job.getDescription() %></p>
                <div class="job-meta">
                    <span class="pill pill-neutral"><%= job.getSkills() %></span>
                    <span class="pill pill-neutral"><%= job.getWorkload() %></span>
                    <span class="pill pill-neutral">Deadline: <%= job.getDeadline() %></span>
                </div>
                <div class="button-row">
                    <a class="primary-button" href="<%= request.getContextPath() %>/ta/job?id=<%= job.getId() %>">View &amp; Apply</a>
                </div>
            </article>
            <% } %>
        </div>
    </article>
    <% } %>
    <% if (!applicationResults.isEmpty()) { %>
    <article class="panel" style="margin-top: 18px;">
        <div class="panel-header">
            <h4>My Applications</h4>
        </div>
        <div class="table-shell">
            <table>
                <thead>
                <tr>
                    <th>Module</th>
                    <th>Job Title</th>
                    <th>Priority</th>
                    <th>Status</th>
                    <th>Notes</th>
                </tr>
                </thead>
                <tbody>
                <% for (ApplicationRecord record : applicationResults) {
                    String statusClass = "Submitted".equals(record.getStatus()) ? "status-open"
                        : ("Under Review".equals(record.getStatus()) ? "status-review"
                        : ("Accepted".equals(record.getStatus()) ? "status-accepted" : "status-rejected"));
                %>
                <tr>
                    <td><strong><%= pageService.getJobModuleCode(record.getJobId()) %></strong></td>
                    <td><%= pageService.getJobTitle(record.getJobId()) %></td>
                    <td><%= record.getPriority() %></td>
                    <td><span class="status-chip <%= statusClass %>"><%= record.getStatus() %></span></td>
                    <td><%= record.getNotes() %></td>
                </tr>
                <% } %>
                </tbody>
            </table>
        </div>
        <div class="button-row" style="margin-top: 14px;">
            <a class="secondary-button" href="<%= request.getContextPath() %>/ta/applications">View all my applications</a>
        </div>
    </article>
    <% } %>
    <% } %>

    <% if ("MO".equals(currentRole)) { %>
    <% if (!jobResults.isEmpty()) { %>
    <article class="panel" style="margin-top: 18px;">
        <div class="panel-header">
            <h4>Job Postings</h4>
            <p>Click to review applicants for each posting.</p>
        </div>
        <div class="grid one-col">
            <% for (JobPosting job : jobResults) { %>
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
                    <span class="pill pill-neutral">Applicants: <%= pageService.countApplicantsForJob(job.getId()) %></span>
                </div>
                <div class="button-row">
                    <a class="primary-button" href="<%= request.getContextPath() %>/mo/review?jobId=<%= job.getId() %>">Review Applicants</a>
                    <a class="ghost-button" href="<%= request.getContextPath() %>/mo/jobs/edit?id=<%= job.getId() %>">Edit Posting</a>
                </div>
            </article>
            <% } %>
        </div>
    </article>
    <% } %>
    <% if (!applicationResults.isEmpty()) { %>
    <article class="panel" style="margin-top: 18px;">
        <div class="panel-header">
            <h4>Applications</h4>
        </div>
        <div class="table-shell">
            <table>
                <thead>
                <tr>
                    <th>Module</th>
                    <th>Applicant</th>
                    <th>Priority</th>
                    <th>Status</th>
                    <th>Action</th>
                </tr>
                </thead>
                <tbody>
                <% for (ApplicationRecord record : applicationResults) {
                    String statusClass = "Submitted".equals(record.getStatus()) ? "status-open"
                        : ("Under Review".equals(record.getStatus()) ? "status-review"
                        : ("Accepted".equals(record.getStatus()) ? "status-accepted" : "status-rejected"));
                    TAProfile applicant = pageService.getProfileById(record.getTaId());
                %>
                <tr>
                    <td><strong><%= pageService.getJobModuleCode(record.getJobId()) %></strong><br><span class="muted"><%= pageService.getJobTitle(record.getJobId()) %></span></td>
                    <td><%= applicant == null ? record.getTaId() : applicant.getFullName() %></td>
                    <td><%= record.getPriority() %></td>
                    <td><span class="status-chip <%= statusClass %>"><%= record.getStatus() %></span></td>
                    <td><a class="secondary-button" href="<%= request.getContextPath() %>/mo/review?jobId=<%= record.getJobId() %>&appId=<%= record.getId() %>">Review</a></td>
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
            <h4>Applicant Profiles</h4>
        </div>
        <div class="table-shell">
            <table>
                <thead>
                <tr>
                    <th>Name</th>
                    <th>Student No.</th>
                    <th>Skills</th>
                    <th>Accepted Jobs</th>
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
            <h4>Job Postings</h4>
        </div>
        <div class="table-shell">
            <table>
                <thead>
                <tr>
                    <th>Module</th>
                    <th>Title</th>
                    <th>Status</th>
                    <th>Applicants</th>
                </tr>
                </thead>
                <tbody>
                <% for (JobPosting job : jobResults) { %>
                <tr>
                    <td><strong><%= job.getModuleCode() %></strong></td>
                    <td><%= job.getTitle() %></td>
                    <td><span class="status-chip <%= "Open".equals(job.getStatus()) ? "status-open" : "status-rejected" %>"><%= job.getStatus() %></span></td>
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
            <h4>Applications</h4>
        </div>
        <div class="table-shell">
            <table>
                <thead>
                <tr>
                    <th>Module</th>
                    <th>Applicant</th>
                    <th>Status</th>
                </tr>
                </thead>
                <tbody>
                <% for (ApplicationRecord record : applicationResults) {
                    String statusClass = "Submitted".equals(record.getStatus()) ? "status-open"
                        : ("Under Review".equals(record.getStatus()) ? "status-review"
                        : ("Accepted".equals(record.getStatus()) ? "status-accepted" : "status-rejected"));
                    TAProfile applicant = pageService.getProfileById(record.getTaId());
                %>
                <tr>
                    <td><strong><%= pageService.getJobModuleCode(record.getJobId()) %></strong></td>
                    <td><%= applicant == null ? record.getTaId() : applicant.getFullName() %></td>
                    <td><span class="status-chip <%= statusClass %>"><%= record.getStatus() %></span></td>
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
            <h4>Applicant Profiles</h4>
        </div>
        <div class="table-shell">
            <table>
                <thead>
                <tr>
                    <th>Name</th>
                    <th>Student No.</th>
                    <th>Skills</th>
                    <th>Workload</th>
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
        No results found for "<%= query %>". Try different keywords or change the search category.
    </div>
    <% } %>
    <% } else { %>
    <article class="panel" style="margin-top: 18px;">
        <div class="panel-header">
            <h4>Search Tips</h4>
        </div>
        <ul class="feature-list">
            <% if ("TA".equals(currentRole)) { %>
            <li>Search by module code (e.g. EIE3320) to find open job postings you can apply for</li>
            <li>Search by skill (e.g. Java, Python) to find matching positions</li>
            <li>Search by status (e.g. Accepted, Rejected) to filter your applications</li>
            <% } else if ("MO".equals(currentRole)) { %>
            <li>Search by module code (e.g. EIE3320) to find your job postings and review applicants</li>
            <li>Search by name to find applicant profiles</li>
            <li>Search by status (e.g. Submitted, Under Review) to filter applications for review</li>
            <% } else { %>
            <li>Search by module code or skill to find job postings</li>
            <li>Search by name to find applicant profiles and check workload</li>
            <li>Search by status (e.g. Accepted, Overload) to monitor allocations</li>
            <% } %>
        </ul>
    </article>
    <% } %>
</section>
<%@ include file="fragments/pageEnd.jspf" %>
