<%@ page import="java.util.List,com.group91.tars.model.JobPosting,com.group91.tars.model.ai.AiFitResult,com.group91.tars.service.TarsService,com.group91.tars.web.AiPresentation" %>
<%
    JobPosting job = (JobPosting) request.getAttribute("job");
    List<String> aiTodos = (List<String>) request.getAttribute("aiTodos");
    TarsService pageService = TarsService.getInstance();
%>
<%@ include file="../fragments/pageStart.jspf" %>
<%
    int fitScore = 0;
    List<String> missingSkills = new java.util.ArrayList<String>();
    List<String> matchedSkills = new java.util.ArrayList<String>();
    AiFitResult aiFit = null;
    if (job != null && currentUser != null) {
        aiFit = pageService.getTaFitAdvice(currentUser.getLinkedId(), job.getId(), currentUser);
        fitScore = aiFit.getScore();
        missingSkills = aiFit.getMissingSkills();
        matchedSkills = aiFit.getMatchedSkills();
    }
    String aiSourceMode = aiFit == null ? "local" : aiFit.getSourceMode();
    String aiSourceLabel = AiPresentation.sourceLabel(i18n, aiSourceMode);
%>
<section class="view active">
    <% if (job == null) { %>
    <div class="alert danger"><%= i18n.t("ta.job.detail.no-job") %></div>
    <% } else { %>
    <div class="grid two-col">
        <article class="panel">
            <div class="panel-header">
                <p class="eyebrow"><%= job.getModuleCode() %></p>
                <h4><%= job.getTitle() %></h4>
                <span class="pill <%= "Open".equals(job.getStatus()) ? "pill-success" : "pill-warning" %>"><%= i18n.t("status." + job.getStatus().toLowerCase().replace(" ", "-")) %></span>
            </div>
            <dl class="detail-grid">
                <div>
                    <dt><%= i18n.t("ta.job.detail.required-skills") %></dt>
                    <dd><%= job.getSkills() %></dd>
                </div>
                <div>
                    <dt><%= i18n.t("ta.job.detail.expected-workload") %></dt>
                    <dd><%= job.getWorkload() %></dd>
                </div>
                <div class="span-two">
                    <dt><%= i18n.t("ta.job.detail.description") %></dt>
                    <dd><%= job.getDescription() %></dd>
                </div>
                <div class="span-two">
                    <dt><%= i18n.t("ta.job.detail.knowledge-requirements") %></dt>
                    <dd><%= job.getRequirements() %></dd>
                </div>
                <div>
                    <dt><%= i18n.t("ta.job.detail.deadline") %></dt>
                    <dd><%= job.getDeadline() %></dd>
                </div>
            </dl>
        </article>

        <article class="panel">
            <div class="panel-header">
                <h4><%= i18n.t("ta.job.detail.apply-heading") %></h4>
            </div>
            <div class="ai-score-shell" style="margin-bottom: 16px;">
                <span class="ai-score-label"><%= i18n.t("ta.job.detail.fit-score") %></span>
                <strong><%= fitScore %>%</strong>
            </div>
            <% if (!missingSkills.isEmpty()) { %>
            <div class="alert danger" style="margin-top: 0; margin-bottom: 16px;">
                <strong><%= i18n.t("ta.job.detail.missing-skills") %>:</strong> <%= String.join(", ", missingSkills) %><br>
                <%= i18n.t("ta.job.detail.missing-skills-hint") %>
            </div>
            <% } else { %>
            <div class="alert success" style="margin-top: 0; margin-bottom: 16px;">
                <%= i18n.t("ta.job.detail.full-match") %>
            </div>
            <% } %>
            <form method="post" action="<%= request.getContextPath() %>/ta/job" class="form-grid">
                <input type="hidden" name="jobId" value="<%= job.getId() %>">
                <label class="span-two">
                    <%= i18n.t("ta.job.detail.preference") %>
                    <select name="priority">
                        <option><%= i18n.t("ta.job.detail.priority-1") %></option>
                        <option><%= i18n.t("ta.job.detail.priority-2") %></option>
                        <option><%= i18n.t("ta.job.detail.priority-3") %></option>
                    </select>
                </label>
                <label class="span-two">
                    <%= i18n.t("ta.job.detail.your-skills") %>
                    <input type="text" name="applicantSkills" placeholder="<%= i18n.t("ta.job.detail.skills-placeholder") %>" required>
                </label>
                <label class="span-two">
                    <%= i18n.t("ta.job.detail.desc-label") %>
                    <textarea name="applicantDescription" rows="4" placeholder="<%= i18n.t("ta.job.detail.desc-placeholder") %>" required></textarea>
                </label>
                <label class="span-two">
                    <%= i18n.t("ta.job.detail.motivation-label") %>
                    <textarea name="notes" rows="3"><%= missingSkills.isEmpty() ? i18n.t("ta.job.detail.motivation-default-skills") : i18n.t("ta.job.detail.motivation-default-missing") + " " + String.join(", ", missingSkills) + "." %></textarea>
                </label>
                <div class="button-row span-two">
                    <button class="primary-button" type="submit"><%= i18n.t("ta.job.detail.submit-application") %></button>
                    <a class="ghost-button" href="<%= request.getContextPath() %>/ta/jobs"><%= i18n.t("ta.job.detail.back-to-list") %></a>
                </div>
            </form>
        </article>
    </div>

    <div class="grid one-col">
        <details class="accordion-card" open>
            <summary>Optional AI Fit Summary - AI Fit Advisor</summary>
            <div class="accordion-body">
                <article class="ai-card">
                    <div class="ai-meta-grid">
                        <div class="ai-score-shell">
                            <span class="ai-score-label">Fit score</span>
                            <strong><%= fitScore %>%</strong>
                        </div>
                        <div>
                            <span class="ai-source-chip ai-source-<%= aiSourceMode %>">
                                Generated by <%= aiSourceLabel %>
                            </span>
                        </div>
                    </div>
                    <div class="ai-insights">
                        <p><strong>Matched skills:</strong> <%= matchedSkills.isEmpty() ? "None detected" : String.join(", ", matchedSkills) %></p>
                        <% if (missingSkills.isEmpty()) { %>
                        <p><strong><%= i18n.t("ta.job.detail.missing-skills") %>:</strong> <%= i18n.t("ta.job.detail.missing-none") %></p>
                        <% } else { %>
                        <p><strong><%= i18n.t("ta.job.detail.missing-skills") %>:</strong> <%= String.join(", ", missingSkills) %>. <%= i18n.t("ta.job.detail.missing-has") %></p>
                        <% } %>
                        <div class="ai-evidence-box">
                            <strong>CV evidence</strong>
                            <p><%= aiFit == null ? "CV evidence is unavailable." : aiFit.getCvEvidence() %></p>
                        </div>
                        <p><strong>Advice:</strong> <%= aiFit == null ? "Review skills manually before applying." : aiFit.getAdvice() %></p>
                        <% if (aiFit != null && aiFit.getErrorMessage() != null) { %>
                        <p class="ai-source-error"><strong>Error:</strong> <%= aiFit.getErrorMessage() %></p>
                        <% } %>
                        <p class="ai-disclaimer">AI guidance is advisory only. The applicant and hiring staff remain responsible for final judgement.</p>
                    </div>
                </article>
            </div>
        </details>
    </div>
    <% } %>
</section>
<%@ include file="../fragments/pageEnd.jspf" %>
