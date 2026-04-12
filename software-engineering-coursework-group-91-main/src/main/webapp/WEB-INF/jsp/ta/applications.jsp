<%@ page import="java.util.List,com.group91.tars.model.ApplicationRecord,com.group91.tars.service.TarsService" %>
<%
    List<ApplicationRecord> applications = (List<ApplicationRecord>) request.getAttribute("applications");
    TarsService pageService = TarsService.getInstance();
%>
<%@ include file="../fragments/pageStart.jspf" %>
<section class="view active">
    <article class="panel">
        <div class="panel-header">
            <h4>My Applications</h4>
            <p>Transparent status tracking replaces the earlier hidden spreadsheet workflow.</p>
        </div>
        <div class="alert info">
            Visible rejection rule: rejected applications remain visible instead of disappearing silently.
        </div>
        <div class="table-shell" style="margin-top: 18px;">
            <table>
                <thead>
                <tr>
                    <th>Module</th>
                    <th>Priority</th>
                    <th>Status</th>
                    <th>Notes</th>
                </tr>
                </thead>
                <tbody>
                <% for (ApplicationRecord record : applications) {
                    String statusClass = "Submitted".equals(record.getStatus()) ? "status-open"
                        : ("Under Review".equals(record.getStatus()) ? "status-review"
                        : ("Accepted".equals(record.getStatus()) ? "status-accepted" : "status-rejected"));
                %>
                <tr>
                    <td><strong><%= pageService.getJobModuleCode(record.getJobId()) %></strong><br><span class="muted"><%= pageService.getJobTitle(record.getJobId()) %></span></td>
                    <td><%= record.getPriority() %></td>
                    <td><span class="status-chip <%= statusClass %>"><%= record.getStatus() %></span></td>
                    <td><%= record.getNotes() %></td>
                </tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </article>
</section>
<%@ include file="../fragments/pageEnd.jspf" %>
