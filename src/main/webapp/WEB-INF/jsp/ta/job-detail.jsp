<%@ page import="java.util.List,com.group91.tars.model.JobPosting" %>
<%
    JobPosting job = (JobPosting) request.getAttribute("job");
    List<String> aiTodos = (List<String>) request.getAttribute("aiTodos");
%>
<%@ include file="../fragments/pageStart.jspf" %>
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
                    <textarea name="notes">I have relevant skills and can support this module consistently.</textarea>
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
            <summary>Optional AI Fit Summary</summary>
            <div class="accordion-body">
                <article class="ai-card">
                    <p class="muted">Future enhancement only. Core recruitment flow does not depend on AI.</p>
                    <ul class="feature-list">
                        <% for (String item : aiTodos) { %>
                        <li><%= item %></li>
                        <% } %>
                    </ul>
                </article>
            </div>
        </details>
    </div>
    <% } %>
</section>
<%@ include file="../fragments/pageEnd.jspf" %>
