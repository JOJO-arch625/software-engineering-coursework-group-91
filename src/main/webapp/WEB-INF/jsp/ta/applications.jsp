<%@ page import="java.util.List,com.group91.tars.model.ApplicationRecord,com.group91.tars.service.TarsService" %>
<%
    List<ApplicationRecord> applications = (List<ApplicationRecord>) request.getAttribute("applications");
    TarsService pageService = TarsService.getInstance();
%>
<%@ include file="../fragments/pageStart.jspf" %>
<section class="view active">
    <article class="panel">
        <div class="panel-header">
            <h4><%= i18n.t("ta.applications.heading") %></h4>
            <p><%= i18n.t("ta.applications.description") %></p>
        </div>
        <div class="alert info">
            <%= i18n.t("ta.applications.alert") %>
        </div>
        <div class="table-shell" style="margin-top: 18px;">
            <table>
                <thead>
                <tr>
                    <th><%= i18n.t("ta.applications.module") %></th>
                    <th><%= i18n.t("ta.applications.priority") %></th>
                    <th><%= i18n.t("ta.applications.status") %></th>
                    <th><%= i18n.t("ta.applications.notes") %></th>
                </tr>
                </thead>
                <tbody>
                <% for (ApplicationRecord record : applications) {
                    String statusClass = "Submitted".equals(record.getStatus()) ? "status-open"
                        : ("Under Review".equals(record.getStatus()) ? "status-review"
                        : ("Shortlisted".equals(record.getStatus()) ? "status-shortlisted"
                        : ("Accepted".equals(record.getStatus()) ? "status-accepted" : "status-rejected")));
                %>
                <tr>
                    <td><strong><%= pageService.getJobModuleCode(record.getJobId()) %></strong><br><span class="muted"><%= pageService.getJobTitle(record.getJobId()) %></span></td>
                    <td><%= i18n.td(record.getPriority()) %></td>
                    <td><span class="status-chip <%= statusClass %>"><%= i18n.t("status." + record.getStatus().toLowerCase().replace(" ", "-")) %></span></td>
                    <td>
                        <strong><%= i18n.t("ta.applications.motivation") %>:</strong> <%= record.getNotes() == null ? "" : record.getNotes() %><br>
                        <span class="muted"><strong><%= i18n.t("ta.applications.reviewer-notes") %>:</strong> <%= record.getReviewerNotes() == null || record.getReviewerNotes().trim().isEmpty() ? i18n.t("ta.applications.no-reviewer-notes") : record.getReviewerNotes() %></span>
                    </td>
                </tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </article>
</section>
<%@ include file="../fragments/pageEnd.jspf" %>
