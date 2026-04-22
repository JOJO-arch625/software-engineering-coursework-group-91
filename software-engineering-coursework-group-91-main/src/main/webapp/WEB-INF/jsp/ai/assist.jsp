<%@ page import="java.util.List,com.group91.tars.model.ApplicationRecord,com.group91.tars.model.JobPosting,com.group91.tars.model.TAProfile,com.group91.tars.model.UserAccount,com.group91.tars.model.WorkloadSummary,com.group91.tars.service.TarsService" %>
<%
    List<String> aiTodos = (List<String>) request.getAttribute("aiTodos");
    TarsService pageService = TarsService.getInstance();
%>
<%@ include file="../fragments/pageStart.jspf" %>
<section class="view active">
    <article class="hero-card">
        <div>
            <p class="eyebrow">AI Enhancement</p>
            <h3>AI-assisted recruitment support</h3>
            <p class="muted">
                AI features provide skill-fit scoring, missing-skill suggestions, applicant shortlisting, and workload balancing advice.
                Each feature area is collapsed by default so the interface stays clean during demos.
            </p>
        </div>
        <div class="hero-meta">
            <span class="pill pill-success">Now active</span>
            <span class="pill pill-neutral">Optional panels</span>
        </div>
    </article>

    <div class="ai-layout">
        <div class="ai-main">
            <div class="accordion-stack">
                <details class="accordion-card">
                    <summary>TA-side fit scoring</summary>
                    <div class="accordion-body">
                        <% if ("TA".equals(currentRole)) {
                            TAProfile taProfile = pageService.getTaProfile(currentUser.getLinkedId());
                            List<JobPosting> openJobs = pageService.getOpenJobs();
                        %>
                        <article class="ai-card">
                            <div class="ai-insights">
                                <p><strong>Your skills:</strong> <%= taProfile == null ? "Not set" : taProfile.getSkills() %></p>
                            </div>
                        </article>
                        <% for (JobPosting job : openJobs) {
                            int score = pageService.calculateFitScore(currentUser.getLinkedId(), job.getId());
                            List<String> missing = pageService.getMissingSkills(currentUser.getLinkedId(), job.getId());
                        %>
                        <article class="ai-card" style="margin-top: 14px;">
                            <div class="ai-score-shell">
                                <span class="ai-score-label"><%= job.getModuleCode() %> - <%= job.getTitle() %></span>
                                <strong><%= score %>%</strong>
                            </div>
                            <div class="ai-insights">
                                <p><strong>Required skills:</strong> <%= job.getSkills() %></p>
                                <% if (missing.isEmpty()) { %>
                                <p><strong>Missing skills:</strong> None - full match!</p>
                                <% } else { %>
                                <p><strong>Missing skills:</strong> <%= String.join(", ", missing) %></p>
                                <% } %>
                            </div>
                        </article>
                        <% } %>
                        <% } else { %>
                        <article class="ai-card">
                            <div class="ai-insights">
                                <p>TA-side fit scoring compares a TA's declared skills against each open job's required skills.</p>
                                <p>Log in as a TA to see personalised match scores and missing-skill suggestions.</p>
                            </div>
                        </article>
                        <% } %>
                    </div>
                </details>

                <details class="accordion-card">
                    <summary>MO-side shortlist support</summary>
                    <div class="accordion-body">
                        <% if ("MO".equals(currentRole)) {
                            List<JobPosting> moJobs = pageService.getJobsForMo(currentUser.getLinkedId());
                            for (JobPosting job : moJobs) {
                                List<ApplicationRecord> apps = pageService.getApplicationsForJob(job.getId());
                                if (apps.isEmpty()) continue;
                        %>
                        <article class="ai-card" style="margin-top: 14px;">
                            <div class="panel-header">
                                <h4><%= job.getModuleCode() %> - <%= job.getTitle() %></h4>
                            </div>
                            <div class="table-shell">
                                <table>
                                    <thead>
                                    <tr>
                                        <th>Applicant</th>
                                        <th>Fit Score</th>
                                        <th>Accepted Jobs</th>
                                        <th>Status</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <% for (ApplicationRecord app : apps) {
                                        TAProfile applicant = pageService.getProfileById(app.getTaId());
                                        int fitScore = pageService.calculateFitScore(app.getTaId(), job.getId());
                                    %>
                                    <tr>
                                        <td><%= applicant == null ? app.getTaId() : applicant.getFullName() %></td>
                                        <td><strong><%= fitScore %>%</strong></td>
                                        <td><%= pageService.countAcceptedJobsForTaPublic(app.getTaId()) %> / 3</td>
                                        <td><span class="status-chip <%= "Accepted".equals(app.getStatus()) ? "status-accepted" : ("Rejected".equals(app.getStatus()) ? "status-rejected" : "status-open") %>"><%= app.getStatus() %></span></td>
                                    </tr>
                                    <% } %>
                                    </tbody>
                                </table>
                            </div>
                        </article>
                        <% } %>
                        <% } else { %>
                        <article class="ai-card">
                            <div class="ai-insights">
                                <p>MO-side shortlist ranks applicants by skill alignment and current workload before the MO reviews manually.</p>
                                <p>Log in as an MO to see applicant fit scores for your job postings.</p>
                                <p><strong>Safety:</strong> Final applicant decisions still belong to the MO.</p>
                            </div>
                        </article>
                        <% } %>
                    </div>
                </details>

                <details class="accordion-card">
                    <summary>Admin-side balancing advice</summary>
                    <div class="accordion-body">
                        <article class="ai-card">
                            <div class="ai-insights">
                                <% List<String> advice = pageService.getWorkloadBalancingAdvice();
                                    for (String line : advice) { %>
                                <p><%= line %></p>
                                <% } %>
                            </div>
                        </article>
                    </div>
                </details>
            </div>

            <div class="grid two-col ai-support-grid">
                <article class="panel">
                    <div class="panel-header">
                        <h4>AI Feature Status</h4>
                    </div>
                    <ul class="feature-list">
                        <% for (String item : aiTodos) { %>
                        <li><%= item %></li>
                        <% } %>
                    </ul>
                </article>
                <article class="panel">
                    <div class="panel-header">
                        <h4>Safe Coursework Positioning</h4>
                    </div>
                    <ul class="feature-list">
                        <li>AI output is presented as assistance, not authority</li>
                        <li>Hide optional panels until the user opens them</li>
                        <li>Final decisions always belong to the human user</li>
                        <li>AI scoring is based on skill keyword matching</li>
                    </ul>
                </article>
            </div>
        </div>

        <aside class="ai-rail">
            <article class="panel rail-panel" id="ui-todo">
                <div class="panel-header">
                    <h4>Workspace Status</h4>
                </div>
                <ul class="feature-list">
                    <li>Search module: cross-page search for jobs, applicants, and records is now available.</li>
                    <li>Inbox module: status updates, review reminders, and system alerts are now available.</li>
                    <li>AI scoring: skill-fit scoring and workload balancing advice are now active.</li>
                </ul>
            </article>

            <article class="panel rail-panel">
                <div class="panel-header">
                    <h4>Admin AI Role</h4>
                </div>
                <ul class="feature-list">
                    <li>Admin does not recruit directly; Admin monitors allocation across all accepted jobs.</li>
                    <li>AI suggests overload balancing when TAs reach the workload cap.</li>
                    <li>This keeps the Admin page practical while using AI for proactive alerts.</li>
                </ul>
            </article>
        </aside>
    </div>
</section>
<%@ include file="../fragments/pageEnd.jspf" %>
