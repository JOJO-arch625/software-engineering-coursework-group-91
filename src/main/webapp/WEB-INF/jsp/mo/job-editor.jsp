<%@ page import="com.group91.tars.model.JobPosting" %>
<%
    JobPosting job = (JobPosting) request.getAttribute("job");
%>
<%@ include file="../fragments/pageStart.jspf" %>
<section class="view active">
    <div class="grid two-col">
        <article class="panel">
            <div class="panel-header">
                <h4><%= i18n.t("mo.job.editor.heading") %></h4>
                <p><%= i18n.t("mo.job.editor.description") %></p>
            </div>
            <form method="post" action="<%= request.getContextPath() %>/mo/jobs/edit" class="form-grid">
                <input type="hidden" name="action" value="save">
                <input type="hidden" name="id" value="<%= job.getId() == null ? "" : job.getId() %>">
                <input type="hidden" name="status" value="<%= job.getStatus() == null ? "Open" : job.getStatus() %>">
                <label>
                    <%= i18n.t("mo.job.editor.module-code") %>
                    <input type="text" name="moduleCode" value="<%= job.getModuleCode() == null ? "" : job.getModuleCode() %>" required>
                </label>
                <label>
                    <%= i18n.t("mo.job.editor.job-title") %>
                    <input type="text" name="title" value="<%= job.getTitle() == null ? "" : job.getTitle() %>" required>
                </label>
                <label class="span-two">
                    <%= i18n.t("mo.job.editor.required-skills") %>
                    <input type="text" name="skills" value="<%= job.getSkills() == null ? "" : job.getSkills() %>" required>
                </label>
                <label class="span-two">
                    <%= i18n.t("mo.job.editor.description") %>
                    <textarea name="description"><%= job.getDescription() == null ? "" : job.getDescription() %></textarea>
                </label>
                <label class="span-two">
                    <%= i18n.t("mo.job.editor.knowledge-requirements") %>
                    <textarea name="requirements" required><%= job.getRequirements() == null ? "" : job.getRequirements() %></textarea>
                </label>
                <label>
                    <%= i18n.t("mo.job.editor.weekly-workload") %>
                    <input type="text" name="workload" value="<%= job.getWorkload() == null ? "" : job.getWorkload() %>" required>
                </label>
                <label>
                    <%= i18n.t("mo.job.editor.deadline") %>
                    <input type="date" name="deadline" value="<%= job.getDeadline() == null ? "" : job.getDeadline() %>" required>
                </label>
                <div class="button-row span-two">
                    <button class="primary-button" type="submit"><%= i18n.t("mo.job.editor.save") %></button>
                </div>
            </form>

            <% if (job.getId() != null) { %>
            <form method="post" action="<%= request.getContextPath() %>/mo/jobs/edit" style="margin-top: 12px;">
                <input type="hidden" name="action" value="close">
                <input type="hidden" name="id" value="<%= job.getId() %>">
                <button class="ghost-button" type="submit"><%= i18n.t("mo.job.editor.close") %></button>
            </form>
            <% } %>
        </article>

        <article class="panel">
            <div class="panel-header">
                <h4><%= i18n.t("mo.job.editor.checklist-heading") %></h4>
            </div>
            <ul class="feature-list">
                <li><%= i18n.t("mo.job.editor.checklist-1") %></li>
                <li><%= i18n.t("mo.job.editor.checklist-2") %></li>
                <li><%= i18n.t("mo.job.editor.checklist-3") %></li>
                <li><%= i18n.t("mo.job.editor.checklist-4") %></li>
            </ul>
        </article>
    </div>
</section>
<%@ include file="../fragments/pageEnd.jspf" %>
