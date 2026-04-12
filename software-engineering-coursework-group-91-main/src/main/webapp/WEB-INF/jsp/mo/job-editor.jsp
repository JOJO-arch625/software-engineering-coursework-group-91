<%@ page import="com.group91.tars.model.JobPosting" %>
<%
    JobPosting job = (JobPosting) request.getAttribute("job");
%>
<%@ include file="../fragments/pageStart.jspf" %>
<section class="view active">
    <div class="grid two-col">
        <article class="panel">
            <div class="panel-header">
                <h4>Create Or Edit Job Posting</h4>
                <p>MO can save, update, or close a posting using local file-backed data.</p>
            </div>
            <form method="post" action="<%= request.getContextPath() %>/mo/jobs/edit" class="form-grid">
                <input type="hidden" name="action" value="save">
                <input type="hidden" name="id" value="<%= job.getId() == null ? "" : job.getId() %>">
                <input type="hidden" name="status" value="<%= job.getStatus() == null ? "Open" : job.getStatus() %>">
                <label>
                    Module code
                    <input type="text" name="moduleCode" value="<%= job.getModuleCode() == null ? "" : job.getModuleCode() %>" required>
                </label>
                <label>
                    Job title
                    <input type="text" name="title" value="<%= job.getTitle() == null ? "" : job.getTitle() %>" required>
                </label>
                <label class="span-two">
                    Required skills
                    <input type="text" name="skills" value="<%= job.getSkills() == null ? "" : job.getSkills() %>" required>
                </label>
                <label class="span-two">
                    Description
                    <textarea name="description"><%= job.getDescription() == null ? "" : job.getDescription() %></textarea>
                </label>
                <label class="span-two">
                    Knowledge requirements
                    <textarea name="requirements" required><%= job.getRequirements() == null ? "" : job.getRequirements() %></textarea>
                </label>
                <label>
                    Weekly workload
                    <input type="text" name="workload" value="<%= job.getWorkload() == null ? "" : job.getWorkload() %>" required>
                </label>
                <label>
                    Application deadline
                    <input type="date" name="deadline" value="<%= job.getDeadline() == null ? "" : job.getDeadline() %>" required>
                </label>
                <div class="button-row span-two">
                    <button class="primary-button" type="submit">Save posting</button>
                </div>
            </form>

            <% if (job.getId() != null) { %>
            <form method="post" action="<%= request.getContextPath() %>/mo/jobs/edit" style="margin-top: 12px;">
                <input type="hidden" name="action" value="close">
                <input type="hidden" name="id" value="<%= job.getId() %>">
                <button class="ghost-button" type="submit">Close posting</button>
            </form>
            <% } %>
        </article>

        <article class="panel">
            <div class="panel-header">
                <h4>Publishing Checklist</h4>
            </div>
            <ul class="feature-list">
                <li>Skill requirements are visible to TAs before they apply</li>
                <li>Workload and deadlines are clearly shown</li>
                <li>Closing a job blocks future TA submissions</li>
                <li>Posting data is saved to JSON for demo consistency</li>
            </ul>
        </article>
    </div>
</section>
<%@ include file="../fragments/pageEnd.jspf" %>
