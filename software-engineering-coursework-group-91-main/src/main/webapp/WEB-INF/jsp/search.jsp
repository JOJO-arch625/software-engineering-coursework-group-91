<%@ page import="java.util.List,com.group91.tars.model.JobPosting,com.group91.tars.model.ApplicationRecord,com.group91.tars.model.TAProfile,com.group91.tars.service.TarsService" %>
<%
    TarsService service = TarsService.getInstance();
    String query = (String) request.getAttribute("query");
    List<JobPosting> jobResults = (List<JobPosting>) request.getAttribute("jobResults");
    List<ApplicationRecord> applicationResults = (List<ApplicationRecord>) request.getAttribute("applicationResults");
    List<TAProfile> profileResults = (List<TAProfile>) request.getAttribute("profileResults");
%>
<%@ include file="fragments/pageStart.jspf" %>
<section class="view active">
    <article class="panel">
        <div class="panel-header">
            <h4>Global Search</h4>
            <p>Find modules, applications, and people across the TA recruitment workspace.</p>
        </div>
        <form class="search-form" action="<%= request.getContextPath() %>/search" method="GET">
            <input type="text" name="q" placeholder="Enter keywords..." value="<%= query == null ? "" : query %>" required>
            <button class="primary-button" type="submit">Search</button>
        </form>

        <% if (query != null && !query.trim().isEmpty()) { %>
        <div class="results-container" style="margin-top: 24px;">
            <%-- Job Results --%>
            <article class="panel">
                <div class="panel-header">
                    <h5>Modules and Jobs (<%= jobResults == null ? 0 : jobResults.size() %>)</h5>
                </div>
                <div class="grid one-col">
                    <% if (jobResults != null && !jobResults.isEmpty()) { %>
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
                            <div class="button-row">
                                <a class="secondary-button" href="<%= request.getContextPath() %>/ta/job?id=<%= job.getId() %>">View detail</a>
                            </div>
                        </article>
                        <% } %>
                    <% } else { %>
                        <p class="muted">No matching jobs found.</p>
                    <% } %>
                </div>
            </article>

            <%-- Application Results --%>
            <article class="panel" style="margin-top: 24px;">
                <div class="panel-header">
                    <h5>Applications (<%= applicationResults == null ? 0 : applicationResults.size() %>)</h5>
                </div>
                <div class="grid one-col">
                    <% if (applicationResults != null && !applicationResults.isEmpty()) { %>
                        <% for (ApplicationRecord app : applicationResults) { %>
                        <article class="job-card">
                            <header>
                                <div>
                                    <p class="eyebrow"><%= service.getJobModuleCode(app.getJobId()) %></p>
                                    <h5><%= service.getTaName(app.getTaId()) %></h5>
                                </div>
                                <span class="status-chip <%= "Accepted".equals(app.getStatus()) ? "status-open" : ("Rejected".equals(app.getStatus()) ? "status-rejected" : "status-neutral") %>"><%= app.getStatus() %></span>
                            </header>
                            <div class="job-meta">
                                <span class="pill pill-neutral">Priority: <%= app.getPriority() %></span>
                                <span class="pill pill-neutral">Submitted: <%= app.getSubmittedAt() %></span>
                            </div>
                        </article>
                        <% } %>
                    <% } else { %>
                        <p class="muted">No matching applications found.</p>
                    <% } %>
                </div>
            </article>

            <%-- Profile Results (Admin/MO only) --%>
            <% if (profileResults != null) { %>
            <article class="panel" style="margin-top: 24px;">
                <div class="panel-header">
                    <h5>TA Profiles (<%= profileResults.size() %>)</h5>
                </div>
                <div class="grid one-col">
                    <% if (!profileResults.isEmpty()) { %>
                        <% for (TAProfile profile : profileResults) { %>
                        <article class="job-card">
                            <header>
                                <div>
                                    <p class="eyebrow"><%= profile.getStudentNumber() %></p>
                                    <h5><%= profile.getFullName() %></h5>
                                </div>
                            </header>
                            <p class="muted"><%= profile.getEmail() %></p>
                            <div class="job-meta">
                                <span class="pill pill-neutral"><%= profile.getSkills() %></span>
                            </div>
                            <% if ("MO".equals(currentRole)) { %>
                            <div class="button-row">
                                <a class="secondary-button" href="<%= request.getContextPath() %>/mo/dashboard">Review in Dashboard</a>
                            </div>
                            <% } %>
                        </article>
                        <% } %>
                    <% } else { %>
                        <p class="muted">No matching profiles found.</p>
                    <% } %>
                </div>
            </article>
            <% } %>
        </div>
        <% } %>
    </article>
</section>
<%@ include file="fragments/pageEnd.jspf" %>
