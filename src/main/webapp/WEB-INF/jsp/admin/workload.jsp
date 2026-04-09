<%@ page import="java.util.List,com.group91.tars.model.WorkloadSummary" %>
<%
    List<WorkloadSummary> summaries = (List<WorkloadSummary>) request.getAttribute("summaries");
    Integer overloadCount = (Integer) request.getAttribute("overloadCount");
%>
<%@ include file="../fragments/pageStart.jspf" %>
<section class="view active">
    <div class="grid three-col">
        <article class="metric-card">
            <span>Total TAs tracked</span>
            <strong><%= summaries == null ? 0 : summaries.size() %></strong>
            <small>All visible recruitment records</small>
        </article>
        <article class="metric-card">
            <span>Accepted assignments</span>
            <strong><%
                int totalAccepted = 0;
                if (summaries != null) {
                    for (WorkloadSummary summary : summaries) {
                        totalAccepted += summary.getAcceptedCount();
                    }
                }
                out.print(totalAccepted);
            %></strong>
            <small>Across all modules</small>
        </article>
        <article class="metric-card">
            <span>Overload alerts</span>
            <strong><%= overloadCount == null ? 0 : overloadCount %></strong>
            <small>Flagged at workload threshold</small>
        </article>
    </div>

    <div class="grid two-col">
        <article class="panel">
            <div class="panel-header">
                <h4>Workload Overview</h4>
                <p>Threshold: 3 accepted jobs</p>
            </div>
            <div class="alert <%= overloadCount != null && overloadCount > 0 ? "danger" : "info" %>">
                <%= overloadCount != null && overloadCount > 0
                    ? overloadCount + " TA(s) have reached the workload threshold. Review allocation before confirming more offers."
                    : "No TA is currently at the overload threshold." %>
            </div>
            <div class="table-shell" style="margin-top: 18px;">
                <table>
                    <thead>
                    <tr>
                        <th>TA</th>
                        <th>Accepted Modules</th>
                        <th>Count</th>
                        <th>Workload Status</th>
                    </tr>
                    </thead>
                    <tbody>
                    <% for (WorkloadSummary summary : summaries) { %>
                    <tr>
                        <td><%= summary.getTaName() %></td>
                        <td><%= summary.getAcceptedModules().isEmpty() ? "-" : String.join(", ", summary.getAcceptedModules()) %></td>
                        <td><%= summary.getAcceptedCount() %></td>
                        <td>
                            <span class="status-chip <%= summary.isOverloadFlag() ? "status-overload" : "status-open" %>">
                                <%= summary.isOverloadFlag() ? "Overload risk" : "Balanced" %>
                            </span>
                        </td>
                    </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </article>
        <article class="panel">
            <div class="panel-header">
                <h4>Admin Actions</h4>
            </div>
            <ul class="feature-list">
                <li>View all accepted job records</li>
                <li>See workload count and course names per TA</li>
                <li>Identify overload risk before final allocation</li>
                <li>Use data transparency to contact MO or TA if needed</li>
            </ul>
        </article>
    </div>
</section>
<%@ include file="../fragments/pageEnd.jspf" %>
