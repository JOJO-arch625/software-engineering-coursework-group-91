<%@ page import="java.util.List,com.group91.tars.model.ApplicationRecord,com.group91.tars.model.JobPosting,com.group91.tars.model.TAProfile,com.group91.tars.model.UserAccount,com.group91.tars.model.WorkloadSummary,com.group91.tars.service.TarsService" %>
<%
    List<String> aiTodos = (List<String>) request.getAttribute("aiTodos");
    TarsService pageService = TarsService.getInstance();
%>
<%@ include file="../fragments/pageStart.jspf" %>
<section class="view active">
    <article class="hero-card">
        <div>
            <p class="eyebrow"><%= i18n.t("ai.assist.eyebrow") %></p>
            <h3><%= i18n.t("ai.assist.heading") %></h3>
            <p class="muted"><%= i18n.t("ai.assist.description") %></p>
        </div>
        <div class="hero-meta">
            <span class="pill pill-success"><%= i18n.t("ai.assist.pill-active") %></span>
            <span class="pill pill-neutral"><%= i18n.t("ai.assist.pill-optional") %></span>
        </div>
    </article>

    <div class="ai-layout">
        <div class="ai-main">
            <div class="accordion-stack">
                <details class="accordion-card">
                    <summary><%= i18n.t("ai.assist.ta-fit-scoring") %></summary>
                    <div class="accordion-body">
                        <% if ("TA".equals(currentRole)) {
                            TAProfile taProfile = pageService.getTaProfile(currentUser.getLinkedId());
                            List<JobPosting> openJobs = pageService.getOpenJobs();
                        %>
                        <article class="ai-card">
                            <div class="ai-insights">
                                <p><strong><%= i18n.t("ai.assist.ta-your-skills") %>:</strong> <%= taProfile == null ? i18n.t("ai.assist.ta-not-set") : taProfile.getSkills() %></p>
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
                                <p><strong><%= i18n.t("ai.assist.ta-required-skills") %>:</strong> <%= job.getSkills() %></p>
                                <% if (missing.isEmpty()) { %>
                                <p><strong><%= i18n.t("ai.assist.ta-missing-skills") %>:</strong> <%= i18n.t("ai.assist.ta-full-match") %></p>
                                <% } else { %>
                                <p><strong><%= i18n.t("ai.assist.ta-missing-skills") %>:</strong> <%= String.join(", ", missing) %></p>
                                <% } %>
                            </div>
                        </article>
                        <% } %>
                        <% } else { %>
                        <article class="ai-card">
                            <div class="ai-insights">
                                <p><%= i18n.t("ai.assist.ta-not-logged-in") %></p>
                                <p><%= i18n.t("ai.assist.ta-login-prompt") %></p>
                            </div>
                        </article>
                        <% } %>
                    </div>
                </details>

                <details class="accordion-card">
                    <summary><%= i18n.t("ai.assist.mo-shortlist") %></summary>
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
                                        <th><%= i18n.t("ai.assist.mo-applicant") %></th>
                                        <th><%= i18n.t("ai.assist.mo-fit-score") %></th>
                                        <th><%= i18n.t("ai.assist.mo-accepted-jobs") %></th>
                                        <th><%= i18n.t("ai.assist.mo-status") %></th>
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
                                        <td><span class="status-chip <%= "Accepted".equals(app.getStatus()) ? "status-accepted" : ("Rejected".equals(app.getStatus()) ? "status-rejected" : "status-open") %>"><%= i18n.t("status." + app.getStatus().toLowerCase().replace(" ", "-")) %></span></td>
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
                                <p><%= i18n.t("ai.assist.mo-not-logged-in") %></p>
                                <p><%= i18n.t("ai.assist.mo-login-prompt") %></p>
                                <p><strong><%= i18n.t("ai.assist.mo-safety") %></strong></p>
                            </div>
                        </article>
                        <% } %>
                    </div>
                </details>

                <details class="accordion-card">
                    <summary><%= i18n.t("ai.assist.admin-balancing") %></summary>
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
                        <h4><%= i18n.t("ai.assist.status-heading") %></h4>
                    </div>
                    <ul class="feature-list">
                        <% for (String item : aiTodos) { %>
                        <li><%= item %></li>
                        <% } %>
                    </ul>
                </article>
                <article class="panel">
                    <div class="panel-header">
                        <h4><%= i18n.t("ai.assist.safety-heading") %></h4>
                    </div>
                    <ul class="feature-list">
                        <li><%= i18n.t("ai.assist.safety-1") %></li>
                        <li><%= i18n.t("ai.assist.safety-2") %></li>
                        <li><%= i18n.t("ai.assist.safety-3") %></li>
                        <li><%= i18n.t("ai.assist.safety-4") %></li>
                    </ul>
                </article>
            </div>
        </div>

        <aside class="ai-rail">
            <article class="panel rail-panel" id="ui-todo">
                <div class="panel-header">
                    <h4><%= i18n.t("ai.assist.workspace-heading") %></h4>
                </div>
                <ul class="feature-list">
                    <li><%= i18n.t("ai.assist.workspace-1") %></li>
                    <li><%= i18n.t("ai.assist.workspace-2") %></li>
                    <li><%= i18n.t("ai.assist.workspace-3") %></li>
                </ul>
            </article>

            <article class="panel rail-panel">
                <div class="panel-header">
                    <h4><%= i18n.t("ai.assist.admin-heading") %></h4>
                </div>
                <ul class="feature-list">
                    <li><%= i18n.t("ai.assist.admin-1") %></li>
                    <li><%= i18n.t("ai.assist.admin-2") %></li>
                    <li><%= i18n.t("ai.assist.admin-3") %></li>
                </ul>
            </article>
        </aside>
    </div>
</section>
<%@ include file="../fragments/pageEnd.jspf" %>
