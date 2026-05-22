<%@ page import="java.util.List,com.group91.tars.model.ApplicationRecord,com.group91.tars.model.JobPosting,com.group91.tars.model.TAProfile,com.group91.tars.service.TarsService" %>
<%
    List<JobPosting> moJobs = (List<JobPosting>) request.getAttribute("moJobs");
    List<ApplicationRecord> allApplications = (List<ApplicationRecord>) request.getAttribute("allApplications");
    ApplicationRecord selectedApplication = (ApplicationRecord) request.getAttribute("selectedApplication");
    TAProfile selectedApplicant = (TAProfile) request.getAttribute("selectedApplicant");
    TarsService pageService = TarsService.getInstance();

    java.util.Map<String, JobPosting> jobMap = new java.util.LinkedHashMap<String, JobPosting>();
    if (moJobs != null) {
        for (JobPosting j : moJobs) {
            jobMap.put(j.getId(), j);
        }
    }

    String selectedAppId = selectedApplication == null ? "" : selectedApplication.getId();
%>
<%@ include file="../fragments/pageStart.jspf" %>
<section class="view active">
    <div class="grid two-col">
        <article class="panel">
            <div class="panel-header">
                <h4><%= i18n.t("mo.review.heading") %></h4>
                <p class="muted" style="margin: 4px 0 0; font-size: 13px;"><%= TarsService.MO_COURSE_LABEL %></p>
                <p><%= i18n.t("mo.review.description") %></p>
            </div>
            <p class="muted review-list-caption">
                Showing <%= allApplications == null ? 0 : allApplications.size() %> applicants.
            </p>
            <div class="table-shell">
                <table>
                    <thead>
                    <tr>
                        <th><%= i18n.t("mo.review.applicant") %></th>
                        <th>Position</th>
                        <th><%= i18n.t("mo.review.priority") %></th>
                        <th>Skill Fit</th>
                        <th>CV</th>
                    </tr>
                    </thead>
                    <tbody>
                    <% if (allApplications != null) {
                        for (ApplicationRecord record : allApplications) {
                            TAProfile applicant = pageService.getProfileById(record.getTaId());
                            JobPosting job = jobMap.get(record.getJobId());
                            int fitScore = job == null ? 0 : pageService.calculateFitScore(record.getTaId(), job.getId());
                            boolean isSelected = selectedApplication != null && record.getId().equals(selectedApplication.getId());
                            boolean hasApplicantCv = applicant != null && applicant.getCvPath() != null && !applicant.getCvPath().trim().isEmpty();
                    %>
                    <tr style="<%= isSelected ? "background: rgba(0,180,216,0.08);" : "" %>">
                        <td><a class="table-link" href="<%= request.getContextPath() %>/mo/review?appId=<%= record.getId() %>"><%= applicant == null ? record.getTaId() : applicant.getFullName() %></a></td>
                        <td><%= TarsService.MO_COURSE_TITLE %> TA</td>
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
                    <% } } %>
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
                JobPosting selectedJob = jobMap.get(selectedApplication.getJobId());
                int profileFitScore = selectedJob == null || selectedApplicant == null
                    ? 0
                    : pageService.calculateFitScore(selectedApplicant.getId(), selectedJob.getId());
                String[] jobSkillArr = selectedJob == null || selectedJob.getSkills() == null ? new String[0]
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
            <p class="muted" style="margin: 0 0 12px; font-size: 13px;"><%= selectedJob == null ? "" : selectedJob.getTitle() %></p>
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
                    <dd><%= selectedApplication.getNotes() == null ? "" : selectedApplication.getNotes() %></dd>
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
            <% } %>
        </article>
    </div>
</section>
<%@ include file="../fragments/pageEnd.jspf" %>
