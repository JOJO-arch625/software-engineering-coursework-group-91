<%@ page import="java.util.List,com.group91.tars.model.ApplicationRecord,com.group91.tars.model.JobPosting,com.group91.tars.model.TAProfile,com.group91.tars.service.TarsService" %>
<%
    JobPosting selectedJob = (JobPosting) request.getAttribute("selectedJob");
    List<JobPosting> moJobs = (List<JobPosting>) request.getAttribute("moJobs");
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
            <% if (moJobs != null && !moJobs.isEmpty()) { %>
            <div class="review-job-switcher" aria-label="MO job postings">
                <% for (JobPosting jobOption : moJobs) {
                    boolean isCurrentJob = selectedJob != null && jobOption.getId().equals(selectedJob.getId());
                    int applicantCount = pageService.countApplicantsForJob(jobOption.getId());
                %>
                <a class="review-job-chip <%= isCurrentJob ? "active" : "" %>"
                   href="<%= request.getContextPath() %>/mo/review?jobId=<%= jobOption.getId() %>">
                    <strong><%= jobOption.getModuleCode() %></strong>
                    <span><%= jobOption.getTitle() %></span>
                    <em><%= applicantCount %> applicants</em>
                </a>
                <% } %>
            </div>
            <% } %>
            <p class="muted review-list-caption">
                Showing <%= applications == null ? 0 : applications.size() %> applicants for
                <strong><%= selectedJob == null ? "no selected posting" : selectedJob.getModuleCode() + " " + selectedJob.getTitle() %></strong>.
            </p>
            <div class="table-shell">
                <table>
                    <thead>
                    <tr>
                        <th><%= i18n.t("mo.review.applicant") %></th>
                        <th><%= i18n.t("mo.review.priority") %></th>
                        <th>Skill Fit</th>
                        <th>CV</th>
                    </tr>
                    </thead>
                    <tbody>
                    <% for (ApplicationRecord record : applications) {
                        TAProfile applicant = pageService.getProfileById(record.getTaId());
                        int fitScore = selectedJob == null ? 0 : pageService.calculateFitScore(record.getTaId(), selectedJob.getId());
                        boolean isSelected = selectedApplication != null && record.getId().equals(selectedApplication.getId());
                        boolean hasApplicantCv = applicant != null && applicant.getCvPath() != null && !applicant.getCvPath().trim().isEmpty();
                    %>
                    <tr style="<%= isSelected ? "background: rgba(0,180,216,0.08);" : "" %>">
                        <td><a class="table-link" href="<%= request.getContextPath() %>/mo/review?jobId=<%= selectedJob == null ? "" : selectedJob.getId() %>&appId=<%= record.getId() %>"><%= applicant == null ? record.getTaId() : applicant.getFullName() %></a></td>
                        <td><%= record.getPriority() %></td>
                        <td><strong><%= fitScore %>%</strong></td>
                        <td>
                            <% if (hasApplicantCv) { %>
                            <a class="table-link" target="_blank" href="<%= request.getContextPath() %>/cv/view?taId=<%= record.getTaId() %>">View CV</a>
                            <% } else { %>
                            <span class="muted">No CV</span>
                            <% } %>
                        </td>
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
                int profileFitScore = selectedJob == null || selectedApplicant == null
                    ? 0
                    : pageService.calculateFitScore(selectedApplicant.getId(), selectedJob.getId());
                String[] jobSkillArr = selectedJob.getSkills() == null ? new String[0]
                    : selectedJob.getSkills().toLowerCase(java.util.Locale.ENGLISH).split("[,;\\s]+");
                String[] taSkillArr = selectedApplicant.getSkills() == null ? new String[0]
                    : selectedApplicant.getSkills().toLowerCase(java.util.Locale.ENGLISH).split("[,;\\s]+");
                java.util.List<String> profileMatched = new java.util.ArrayList<String>();
                java.util.List<String> profileMissing = new java.util.ArrayList<String>();
                for (String js : jobSkillArr) {
                    if (js.trim().isEmpty()) continue;
                    boolean found = false;
                    for (String ts : taSkillArr) {
                        if (js.equals(ts) || ts.contains(js) || js.contains(ts)) { found = true; break; }
                    }
                    if (found) profileMatched.add(js); else profileMissing.add(js);
                }
                String selectedCvPath = selectedApplicant.getCvPath();
                boolean selectedHasCv = selectedCvPath != null && !selectedCvPath.trim().isEmpty();
            %>
            <h4 style="margin-bottom: 10px;"><%= selectedApplicant.getFullName() %></h4>
            <div class="ai-evidence-box" style="margin-bottom: 12px;">
                <strong>Applicant CV attachment</strong>
                <% if (selectedHasCv) { %>
                <p class="file-name"><%= selectedCvPath %></p>
                <a class="secondary-button" target="_blank" href="<%= request.getContextPath() %>/cv/view?taId=<%= selectedApplicant.getId() %>">Open CV</a>
                <% } else { %>
                <p>No CV has been uploaded for this applicant.</p>
                <% } %>
            </div>
            <div class="ai-score-shell" style="margin-bottom: 6px;">
                <span class="ai-score-label">Skill Fit</span>
                <strong><%= profileFitScore %></strong>
            </div>
            <% if (!profileMissing.isEmpty()) { %>
            <div class="alert danger" style="margin-top: 0; margin-bottom: 12px;">
                <strong><%= i18n.t("mo.review.missing-skills") %>:</strong> <%= String.join(", ", profileMissing) %>
            </div>
            <% } else if (!profileMatched.isEmpty()) { %>
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
                <div class="span-two">
                    <dt><%= i18n.t("mo.review.reviewer-notes") %></dt>
                    <dd><%= selectedApplication.getReviewerNotes() == null || selectedApplication.getReviewerNotes().trim().isEmpty() ? i18n.t("ta.applications.no-reviewer-notes") : selectedApplication.getReviewerNotes() %></dd>
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
                    <textarea name="notes"><%= selectedApplication.getReviewerNotes() == null ? "" : selectedApplication.getReviewerNotes() %></textarea>
                </label>
                <div class="button-row span-two">
                    <button class="secondary-button" type="submit" name="status" value="Under Review"><%= i18n.t("mo.review.mark-under-review") %></button>
                    <button class="secondary-button" type="submit" name="status" value="Shortlisted"><%= i18n.t("mo.review.shortlist") %></button>
                    <button class="primary-button" type="submit" name="status" value="Accepted"><%= i18n.t("mo.review.accept") %></button>
                    <button class="ghost-button" type="submit" name="status" value="Rejected"><%= i18n.t("mo.review.reject") %></button>
                </div>
            </form>
            <form method="post" action="<%= request.getContextPath() %>/mo/review" style="margin-top: 12px;">
                <input type="hidden" name="action" value="bulkShortlist">
                <input type="hidden" name="jobId" value="<%= selectedJob == null ? "" : selectedJob.getId() %>">
                <input type="hidden" name="applicationId" value="<%= selectedApplication.getId() %>">
                <input type="hidden" name="notes" value="<%= i18n.t("mo.review.bulk-shortlisted-note") %>">
                <button class="secondary-button" type="submit"><%= i18n.t("mo.review.bulk-shortlist") %></button>
            </form>
            <% } %>
        </article>
    </div>
</section>
<%@ include file="../fragments/pageEnd.jspf" %>
