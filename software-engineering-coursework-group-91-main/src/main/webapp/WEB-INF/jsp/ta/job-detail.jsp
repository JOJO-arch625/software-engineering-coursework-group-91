<%@ page import="java.util.List,com.group91.tars.model.JobPosting,com.group91.tars.service.TarsService" %>
<%
    JobPosting job = (JobPosting) request.getAttribute("job");
    List<String> aiTodos = (List<String>) request.getAttribute("aiTodos");
    TarsService pageService = TarsService.getInstance();
%>
<%@ include file="../fragments/pageStart.jspf" %>
<%
    int fitScore = 0;
    List<String> missingSkills = new java.util.ArrayList<String>();
    if (job != null && currentUser != null) {
        fitScore = pageService.calculateFitScore(currentUser.getLinkedId(), job.getId());
        missingSkills = pageService.getMissingSkills(currentUser.getLinkedId(), job.getId());
    }
%>
<section class="view active">
    <% if (job == null) { %>
    <div class="alert danger">No job posting is currently available.</div>
    <% } else { %>
    <div class="grid two-col">
        <article class="panel">
            <div class="panel-header">
                <p class="eyebrow"><%= job.getModuleCode() %></p>
                <h4><%= job.getTitle() %></h4>
                <span class="pill <%= "Open".equals(job.getStatus()) ? "pill-success" : "pill-warning" %>"><%= job.getStatus() %></span>
            </div>
            <dl class="detail-grid">
                <div>
                    <dt>Required skills</dt>
                    <dd><%= job.getSkills() %></dd>
                </div>
                <div>
                    <dt>Expected workload</dt>
                    <dd><%= job.getWorkload() %></dd>
                </div>
                <div class="span-two">
                    <dt>Description</dt>
                    <dd><%= job.getDescription() %></dd>
                </div>
                <div class="span-two">
                    <dt>Knowledge requirements</dt>
                    <dd><%= job.getRequirements() %></dd>
                </div>
                <div>
                    <dt>Application deadline</dt>
                    <dd><%= job.getDeadline() %></dd>
                </div>
            </dl>
        </article>

        <article class="panel">
            <div class="panel-header">
                <h4>Apply To This Job</h4>
            </div>
            <div class="ai-score-shell" style="margin-bottom: 16px;">
                <span class="ai-score-label">Your skill fit score</span>
                <strong><%= fitScore %>%</strong>
            </div>
            <% if (!missingSkills.isEmpty()) { %>
            <div class="alert danger" style="margin-top: 0; margin-bottom: 16px;">
                <strong>Missing skills:</strong> <%= String.join(", ", missingSkills) %><br>
                Consider highlighting related experience in your motivation note.
            </div>
            <% } else { %>
            <div class="alert success" style="margin-top: 0; margin-bottom: 16px;">
                Your skills fully match the requirements for this position.
            </div>
            <% } %>
            <form method="post" action="<%= request.getContextPath() %>/ta/job" class="form-grid">
                <input type="hidden" name="jobId" value="<%= job.getId() %>">
                <label class="span-two">
                    Preference ranking
                    <select name="priority">
                        <option>Priority 1</option>
                        <option>Priority 2</option>
                        <option>Priority 3</option>
                    </select>
                </label>
                <label class="span-two">
                    Motivation note
                    <textarea name="notes"><%= missingSkills.isEmpty() ? "I have relevant skills and can support this module consistently." : "I have relevant skills and am eager to develop further in: " + String.join(", ", missingSkills) + "." %></textarea>
                </label>
                <div class="button-row span-two">
                    <button class="primary-button" type="submit">Submit application</button>
                    <a class="ghost-button" href="<%= request.getContextPath() %>/ta/jobs">Back to job list</a>
                </div>
            </form>
        </article>
    </div>

    <div class="grid one-col">
        <details class="accordion-card">
            <summary>AI Fit Summary Details</summary>
            <div class="accordion-body">
                <article class="ai-card">
                    <div class="ai-insights">
                        <p><strong>Fit score:</strong> <%= fitScore %>% match based on your declared skills vs. job requirements.</p>
                        <% if (missingSkills.isEmpty()) { %>
                        <p><strong>Missing skills:</strong> None. Your profile covers all required skills for this position.</p>
                        <% } else { %>
                        <p><strong>Missing skills:</strong> <%= String.join(", ", missingSkills) %>. You may still apply, but consider addressing these in your motivation note.</p>
                        <% } %>
                        <p><strong>Note:</strong> AI scoring is based on keyword matching and serves as guidance only. Final decisions are made by the MO.</p>
                    </div>
                </article>
            </div>
        </details>
    </div>
    <% } %>
</section>
<%@ include file="../fragments/pageEnd.jspf" %>
