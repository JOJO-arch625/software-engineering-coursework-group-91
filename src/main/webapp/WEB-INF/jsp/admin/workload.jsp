<%@ page import="java.util.List,com.group91.tars.model.WorkloadSummary,com.group91.tars.service.TarsService" %>
<%
    List<WorkloadSummary> summaries = (List<WorkloadSummary>) request.getAttribute("summaries");
    Integer overloadCount = (Integer) request.getAttribute("overloadCount");
    TarsService pageService = TarsService.getInstance();
    List<String> balancingAdvice = pageService.getWorkloadBalancingAdvice();
%>
<%@ include file="../fragments/pageStart.jspf" %>
<section class="view active">
    <div class="grid three-col">
        <article class="metric-card">
            <span><%= i18n.t("admin.workload.total-tas") %></span>
            <strong><%= summaries == null ? 0 : summaries.size() %></strong>
            <small><%= i18n.t("admin.workload.total-tas-subtitle") %></small>
        </article>
        <article class="metric-card">
            <span><%= i18n.t("admin.workload.accepted-assignments") %></span>
            <strong><%
                int totalAccepted = 0;
                if (summaries != null) {
                    for (WorkloadSummary summary : summaries) {
                        totalAccepted += summary.getAcceptedCount();
                    }
                }
                out.print(totalAccepted);
            %></strong>
            <small><%= i18n.t("admin.workload.accepted-assignments-subtitle") %></small>
        </article>
        <article class="metric-card">
            <span><%= i18n.t("admin.workload.overload-alerts") %></span>
            <strong><%= overloadCount == null ? 0 : overloadCount %></strong>
            <small><%= i18n.t("admin.workload.overload-alerts-subtitle") %></small>
        </article>
    </div>

    <div class="grid two-col">
        <article class="panel">
            <div class="panel-header">
                <h4><%= i18n.t("admin.workload.overview-heading") %></h4>
                <p><%= i18n.t("admin.workload.threshold") %></p>
            </div>
            <div class="alert <%= overloadCount != null && overloadCount > 0 ? "danger" : "info" %>">
                <%= overloadCount != null && overloadCount > 0
                    ? i18n.t("admin.workload.overload-warning", overloadCount)
                    : i18n.t("admin.workload.no-overload") %>
            </div>
            <div class="table-shell" style="margin-top: 18px;">
                <table>
                    <thead>
                    <tr>
                        <th><%= i18n.t("admin.workload.ta") %></th>
                        <th><%= i18n.t("admin.workload.accepted-modules") %></th>
                        <th><%= i18n.t("admin.workload.count") %></th>
                        <th><%= i18n.t("admin.workload.workload-status") %></th>
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
                                <%= summary.isOverloadFlag() ? i18n.t("admin.workload.overload-risk") : i18n.t("admin.workload.balanced") %>
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
                <h4><%= i18n.t("admin.workload.advice-heading") %></h4>
            </div>
            <div class="ai-card">
                <div class="ai-insights">
                    <% for (String line : balancingAdvice) { %>
                    <p><%= line %></p>
                    <% } %>
                </div>
            </div>
            <div class="alert info" style="margin-top: 18px;">
                <%= i18n.t("admin.workload.advice-alert") %>
            </div>
        </article>
    </div>
</section>
<%@ include file="../fragments/pageEnd.jspf" %>
