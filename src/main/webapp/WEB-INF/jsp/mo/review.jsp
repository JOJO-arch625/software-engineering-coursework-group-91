<%@ page import="java.util.List,com.group91.tars.model.ApplicationRecord,com.group91.tars.model.JobPosting,com.group91.tars.model.TAProfile,com.group91.tars.service.TarsService" %>
<%
    JobPosting selectedJob = (JobPosting) request.getAttribute("selectedJob");
    List<ApplicationRecord> applications = (List<ApplicationRecord>) request.getAttribute("applications");
    ApplicationRecord selectedApplication = (ApplicationRecord) request.getAttribute("selectedApplication");
    TAProfile selectedApplicant = (TAProfile) request.getAttribute("selectedApplicant");
    TarsService pageService = TarsService.getInstance();
%>
<%@ include file="../fragments/pageStart.jspf" %>
<section class="view active">
    <div class="grid two-col">
        <article class="panel">
            <div class="panel-header">
                <h4><%= i18n.t("mo.review.heading") %><%= selectedJob == null ? "" : " - " + selectedJob.getTitle() %></h4>
                <p><%= i18n.t("mo.review.description") %></p>
            </div>
            <div class="table-shell">
                <table>
                    <thead>
                    <tr>
                        <th><%= i18n.t("mo.review.applicant") %></th>
                        <th><%= i18n.t("mo.review.priority") %></th>
                        <th><%= i18n.t("mo.review.ai-fit") %></th>
                        <th><%= i18n.t("mo.review.accepted") %></th>
                        <th><%= i18n.t("mo.review.status") %></th>
                    </tr>
                    </thead>
                    <tbody>
                    <% for (ApplicationRecord record : applications) {
                        TAProfile applicant = pageService.getProfileById(record.getTaId());
                        String statusClass = "Submitted".equals(record.getStatus()) ? "status-open"
                            : ("Under Review".equals(record.getStatus()) ? "status-review"
                            : ("Accepted".equals(record.getStatus()) ? "status-accepted" : "status-rejected"));
                        int fitScore = selectedJob == null ? 0 : pageService.calculateFitScore(record.getTaId(), selectedJob.getId());
                        boolean isSelected = selectedApplication != null && record.getId().equals(selectedApplication.getId());
                    %>
                    <tr style="<%= isSelected ? "background: rgba(0,180,216,0.08);" : "" %>">
                        <td><a class="table-link" href="<%= request.getContextPath() %>/mo/review?jobId=<%= selectedJob == null ? "" : selectedJob.getId() %>&appId=<%= record.getId() %>"><%= applicant == null ? record.getTaId() : applicant.getFullName() %></a></td>
                        <td><%= record.getPriority() %></td>
                        <td><strong><%= fitScore %>%</strong></td>
                        <td><%= pageService.countAcceptedJobsForTaPublic(record.getTaId()) %> / 3</td>
                        <td><span class="status-chip <%= statusClass %>"><%= i18n.t("status." + record.getStatus().toLowerCase().replace(" ", "-")) %></span></td>
                    </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </article>

        <article class="panel">
            <div class="panel-header">
                <h4><%= i18n.t("mo.review.detail-heading") %></h4>
            </div>
            <% if (selectedApplication == null || selectedApplicant == null) { %>
            <div class="alert info"><%= i18n.t("mo.review.select-applicant") %></div>
            <% } else {
                int selectedFitScore = selectedJob == null ? 0 : pageService.calculateFitScore(selectedApplicant.getId(), selectedJob.getId());
                List<String> selectedMissingSkills = selectedJob == null ? new java.util.ArrayList<String>() : pageService.getMissingSkills(selectedApplicant.getId(), selectedJob.getId());
            %>
            <h4 style="margin-bottom: 10px;"><%= selectedApplicant.getFullName() %></h4>
            <div class="ai-score-shell" style="margin-bottom: 12px;">
                <span class="ai-score-label"><%= i18n.t("mo.review.fit-score") %></span>
                <strong><%= selectedFitScore %>%</strong>
            </div>
            <% if (!selectedMissingSkills.isEmpty()) { %>
            <div class="alert danger" style="margin-top: 0; margin-bottom: 12px;">
                <strong><%= i18n.t("mo.review.missing-skills") %>:</strong> <%= String.join(", ", selectedMissingSkills) %>
            </div>
            <% } else { %>
            <div class="alert success" style="margin-top: 0; margin-bottom: 12px;">
                <%= i18n.t("mo.review.full-match") %>
            </div>
            <% } %>
            <dl class="detail-grid">
                <div>
                    <dt><%= i18n.t("mo.review.applicant-skills") %></dt>
                    <dd><%= selectedApplication.getApplicantSkills() == null || selectedApplication.getApplicantSkills().isEmpty() ? selectedApplicant.getSkills() : selectedApplication.getApplicantSkills() %></dd>
                </div>
                <div>
                    <dt><%= i18n.t("mo.review.profile-skills") %></dt>
                    <dd><%= selectedApplicant.getSkills() %></dd>
                </div>
                <div class="span-two">
                    <dt><%= i18n.t("mo.review.description") %></dt>
                    <dd><%= selectedApplication.getApplicantDescription() == null || selectedApplication.getApplicantDescription().isEmpty() ? i18n.t("mo.review.no-description") : selectedApplication.getApplicantDescription() %></dd>
                </div>
                <div class="span-two">
                    <dt><%= i18n.t("mo.review.motivation-note") %></dt>
                    <dd><%= selectedApplication.getNotes() == null ? "0" : selectedApplication.getNotes() %></dd>
                </div>
                <div>
                    <dt><%= i18n.t("mo.review.student-number") %></dt>
                    <dd><%= selectedApplicant.getStudentNumber() %></dd>
                </div>
                <div>
                    <dt><%= i18n.t("mo.review.email") %></dt>
                    <dd><%= selectedApplicant.getEmail() %></dd>
                </div>
                <div>
                    <dt><%= i18n.t("mo.review.current-accepted") %></dt>
                    <dd><%= pageService.countAcceptedJobsForTaPublic(selectedApplicant.getId()) %> / 3</dd>
                </div>
                <div>
                    <dt><%= i18n.t("mo.review.current-status") %></dt>
                    <dd><%= i18n.t("status." + selectedApplication.getStatus().toLowerCase().replace(" ", "-")) %></dd>
                </div>
            </dl>
            <form method="post" action="<%= request.getContextPath() %>/mo/review" class="form-grid" style="margin-top: 18px;">
                <input type="hidden" name="jobId" value="<%= selectedJob == null ? "" : selectedJob.getId() %>">
                <input type="hidden" name="applicationId" value="<%= selectedApplication.getId() %>">
                <label class="span-two">
                    <%= i18n.t("mo.review.review-note") %>
                    <textarea name="notes"><%= selectedApplication.getNotes() == null ? "" : selectedApplication.getNotes() %></textarea>
                </label>
                <div class="button-row span-two">
                    <button class="secondary-button" type="submit" name="status" value="Under Review"><%= i18n.t("mo.review.mark-under-review") %></button>
                    <button class="primary-button" type="submit" name="status" value="Accepted"><%= i18n.t("mo.review.accept") %></button>
                    <button class="ghost-button" type="submit" name="status" value="Rejected"><%= i18n.t("mo.review.reject") %></button>
                </div>
            </form>
            <% } %>
        </article>
    </div>
</section>
<%@ include file="../fragments/pageEnd.jspf" %>
