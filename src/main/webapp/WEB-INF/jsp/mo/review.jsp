<%@ page import="java.util.List,com.group91.tars.model.ApplicationRecord,com.group91.tars.model.JobPosting,com.group91.tars.model.TAProfile,com.group91.tars.service.TarsService" %>
<%
    JobPosting selectedJob = (JobPosting) request.getAttribute("selectedJob");
    List<ApplicationRecord> applications = (List<ApplicationRecord>) request.getAttribute("applications");
    ApplicationRecord selectedApplication = (ApplicationRecord) request.getAttribute("selectedApplication");
    TAProfile selectedApplicant = (TAProfile) request.getAttribute("selectedApplicant");
    TarsService pageService = TarsService.getInstance();
    List<String> aiTodos = (List<String>) request.getAttribute("aiTodos");
%>
<%@ include file="../fragments/pageStart.jspf" %>
<section class="view active">
    <div class="grid two-col">
        <article class="panel">
            <div class="panel-header">
                <h4>Applicant Review<%= selectedJob == null ? "" : " - " + selectedJob.getTitle() %></h4>
                <p>MO reviews profile, CV reference, and workload before a decision.</p>
            </div>
            <div class="table-shell">
                <table>
                    <thead>
                    <tr>
                        <th>Applicant</th>
                        <th>Priority</th>
                        <th>Accepted Jobs</th>
                        <th>Status</th>
                    </tr>
                    </thead>
                    <tbody>
                    <% for (ApplicationRecord record : applications) {
                        TAProfile applicant = pageService.getProfileById(record.getTaId());
                        String statusClass = "Submitted".equals(record.getStatus()) ? "status-open"
                            : ("Under Review".equals(record.getStatus()) ? "status-review"
                            : ("Accepted".equals(record.getStatus()) ? "status-accepted" : "status-rejected"));
                    %>
                    <tr>
                        <td><a class="table-link" href="<%= request.getContextPath() %>/mo/review?jobId=<%= selectedJob == null ? "" : selectedJob.getId() %>&appId=<%= record.getId() %>"><%= applicant == null ? record.getTaId() : applicant.getFullName() %></a></td>
                        <td><%= record.getPriority() %></td>
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
                <h4>Selected Applicant Detail</h4>
            </div>
            <% if (selectedApplication == null || selectedApplicant == null) { %>
            <div class="alert info">No application is available for review.</div>
            <% } else { %>
            <h4 style="margin-bottom: 10px;"><%= selectedApplicant.getFullName() %></h4>
            <p><strong>Skills:</strong> <%= selectedApplicant.getSkills() %></p>
            <p><strong>CV path:</strong> <%= selectedApplicant.getCvPath() %></p>
            <p><strong>Current accepted jobs:</strong> <%= pageService.countAcceptedJobsForTaPublic(selectedApplicant.getId()) %> / 3</p>
            <p><strong>Current status:</strong> <%= selectedApplication.getStatus() %></p>
            <form method="post" action="<%= request.getContextPath() %>/mo/review" class="form-grid">
                <input type="hidden" name="jobId" value="<%= selectedJob == null ? "" : selectedJob.getId() %>">
                <input type="hidden" name="applicationId" value="<%= selectedApplication.getId() %>">
                <label class="span-two">
                    Review note
                    <textarea name="notes"><%= selectedApplication.getNotes() == null ? "" : selectedApplication.getNotes() %></textarea>
                </label>
                <div class="button-row span-two">
                    <button class="secondary-button" type="submit" name="status" value="Under Review">Mark under review</button>
                    <button class="primary-button" type="submit" name="status" value="Accepted">Accept applicant</button>
                    <button class="ghost-button" type="submit" name="status" value="Rejected">Reject applicant</button>
                </div>
            </form>
            <details class="accordion-card" style="margin-top: 18px;">
                <summary>Optional AI Review Support</summary>
                <div class="accordion-body">
                    <article class="ai-card">
                        <p class="muted">Reserved for teammate enhancement work. Final decision remains with the MO.</p>
                        <ul class="feature-list">
                            <% for (String item : aiTodos) { %>
                            <li><%= item %></li>
                            <% } %>
                        </ul>
                    </article>
                </div>
            </details>
            <% } %>
        </article>
    </div>
</section>
<%@ include file="../fragments/pageEnd.jspf" %>
