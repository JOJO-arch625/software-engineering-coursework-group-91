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
                <h4>Applicant Review<%= selectedJob == null ? "" : " - " + selectedJob.getTitle() %></h4>
                <p>Select an applicant to view details and make a decision.</p>
            </div>
            <div class="table-shell">
                <table>
                    <thead>
                    <tr>
                        <th>Applicant</th>
                        <th>Priority</th>
                        <th>AI Fit</th>
                        <th>Accepted</th>
                        <th>Status</th>
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
                        <td><span class="status-chip <%= statusClass %>"><%= record.getStatus() %></span></td>
                    </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </article>

        <article class="panel">
            <div class="panel-header">
                <h4>Applicant Detail</h4>
            </div>
            <% if (selectedApplication == null || selectedApplicant == null) { %>
            <div class="alert info">Select an applicant from the list to view their details.</div>
            <% } else {
                int selectedFitScore = selectedJob == null ? 0 : pageService.calculateFitScore(selectedApplicant.getId(), selectedJob.getId());
                List<String> selectedMissingSkills = selectedJob == null ? new java.util.ArrayList<String>() : pageService.getMissingSkills(selectedApplicant.getId(), selectedJob.getId());
            %>
            <h4 style="margin-bottom: 10px;"><%= selectedApplicant.getFullName() %></h4>
            <div class="ai-score-shell" style="margin-bottom: 12px;">
                <span class="ai-score-label">Skill fit score</span>
                <strong><%= selectedFitScore %>%</strong>
            </div>
            <% if (!selectedMissingSkills.isEmpty()) { %>
            <div class="alert danger" style="margin-top: 0; margin-bottom: 12px;">
                <strong>Missing skills:</strong> <%= String.join(", ", selectedMissingSkills) %>
            </div>
            <% } else { %>
            <div class="alert success" style="margin-top: 0; margin-bottom: 12px;">
                Full skill match for this position.
            </div>
            <% } %>
            <dl class="detail-grid">
                <div>
                    <dt>Applicant Skills</dt>
                    <dd><%= selectedApplication.getApplicantSkills() == null || selectedApplication.getApplicantSkills().isEmpty() ? selectedApplicant.getSkills() : selectedApplication.getApplicantSkills() %></dd>
                </div>
                <div>
                    <dt>Profile Skills</dt>
                    <dd><%= selectedApplicant.getSkills() %></dd>
                </div>
                <div class="span-two">
                    <dt>Description</dt>
                    <dd><%= selectedApplication.getApplicantDescription() == null || selectedApplication.getApplicantDescription().isEmpty() ? "No description provided." : selectedApplication.getApplicantDescription() %></dd>
                </div>
                <div class="span-two">
                    <dt>Motivation Note</dt>
                    <dd><%= selectedApplication.getNotes() == null ? "0" : selectedApplication.getNotes() %></dd>
                </div>
                <div>
                    <dt>Student Number</dt>
                    <dd><%= selectedApplicant.getStudentNumber() %></dd>
                </div>
                <div>
                    <dt>Email</dt>
                    <dd><%= selectedApplicant.getEmail() %></dd>
                </div>
                <div>
                    <dt>Current Accepted Jobs</dt>
                    <dd><%= pageService.countAcceptedJobsForTaPublic(selectedApplicant.getId()) %> / 3</dd>
                </div>
                <div>
                    <dt>Current Status</dt>
                    <dd><%= selectedApplication.getStatus() %></dd>
                </div>
            </dl>
            <form method="post" action="<%= request.getContextPath() %>/mo/review" class="form-grid" style="margin-top: 18px;">
                <input type="hidden" name="jobId" value="<%= selectedJob == null ? "" : selectedJob.getId() %>">
                <input type="hidden" name="applicationId" value="<%= selectedApplication.getId() %>">
                <label class="span-two">
                    Review note
                    <textarea name="notes"><%= selectedApplication.getNotes() == null ? "" : selectedApplication.getNotes() %></textarea>
                </label>
                <div class="button-row span-two">
                    <button class="secondary-button" type="submit" name="status" value="Under Review">Mark under review</button>
                    <button class="primary-button" type="submit" name="status" value="Accepted">Accept</button>
                    <button class="ghost-button" type="submit" name="status" value="Rejected">Reject</button>
                </div>
            </form>
            <% } %>
        </article>
    </div>
</section>
<%@ include file="../fragments/pageEnd.jspf" %>
