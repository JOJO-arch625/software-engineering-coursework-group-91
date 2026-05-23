<%@ page import="com.group91.tars.model.TAProfile,com.group91.tars.model.JobRecommendation,java.util.List" %>
<%
    TAProfile profile = (TAProfile) request.getAttribute("profile");
    List<String> notifications = (List<String>) request.getAttribute("notifications");
    List<JobRecommendation> recommendations = (List<JobRecommendation>) request.getAttribute("recommendations");
    Integer applicationTotal = (Integer) request.getAttribute("topMetricTwoValue");
    Integer acceptedTotal = (Integer) request.getAttribute("topMetricThreeValue");
%>
<%@ include file="../fragments/pageStart.jspf" %>
<section class="view active">
    <div class="grid two-col">
        <article class="panel">
            <div class="panel-header">
                <h4><%= i18n.t("ta.dashboard.profile-readiness") %></h4>
            </div>
            <p class="dashboard-lead">
                <strong><%= profile != null && profile.getCvPath() != null ? i18n.t("ta.dashboard.complete-profile") : i18n.t("ta.dashboard.profile-needs-cv") %></strong>
            </p>
            <p class="muted"><%= i18n.t("ta.dashboard.profile-description") %></p>
            <div class="legend">
                <span class="pill pill-neutral"><%= i18n.t("metric.applications") %>: <%= applicationTotal == null ? 0 : applicationTotal %> / 3</span>
                <span class="pill pill-neutral"><%= i18n.t("metric.accepted") %>: <%= acceptedTotal == null ? 0 : acceptedTotal %> / 3</span>
                <span class="pill <%= profile != null && profile.getCvPath() != null ? "pill-success" : "pill-warning" %>">
                    <%= profile != null && profile.getCvPath() != null ? i18n.t("ta.dashboard.cv-linked") : i18n.t("ta.dashboard.cv-required") %>
                </span>
            </div>
        </article>
        <article class="panel">
            <div class="panel-header">
                <h4><%= i18n.t("ta.dashboard.latest-notifications") %></h4>
            </div>
            <ul class="feature-list">
                <% for (String item : notifications) { %>
                <li><%= i18n.td(item) %></li>
                <% } %>
            </ul>
        </article>
    </div>

    <div class="grid one-col">
        <article class="panel">
            <div class="panel-header">
                <div>
                    <h4><%= i18n.t("ta.dashboard.recommendations") %></h4>
                    <p><%= i18n.t("ta.dashboard.recommendations-desc") %></p>
                </div>
                <span class="pill pill-warning"><%= i18n.t("ta.dashboard.ai-reference") %></span>
            </div>
            <% if (recommendations == null || recommendations.isEmpty()) { %>
            <p class="muted"><%= i18n.t("ta.dashboard.no-recommendations") %></p>
            <% } else { %>
            <div class="recommendation-list">
                <% for (JobRecommendation recommendation : recommendations) { %>
                <article class="recommendation-item">
                    <div>
                        <p class="eyebrow"><%= recommendation.getJob().getModuleCode() %></p>
                        <h5><%= recommendation.getJob().getTitle() %></h5>
                        <p class="muted"><%= recommendation.getJob().getDeadline() %> - <%= recommendation.getJob().getWorkload() %></p>
                    </div>
                    <strong><%= recommendation.getMatchRate() %>%</strong>
                    <p><%= i18n.t("ta.dashboard.match-detail", recommendation.getMatchedCount(), recommendation.getTotalRequiredCount(), recommendation.getMatchRate()) %></p>
                    <p class="muted"><%= recommendation.getMatchedSkills().isEmpty() ? "-" : String.join(", ", recommendation.getMatchedSkills()) %></p>
                    <a class="secondary-button" href="<%= request.getContextPath() %>/ta/job?id=<%= recommendation.getJob().getId() %>"><%= i18n.t("common.view-detail") %></a>
                </article>
                <% } %>
            </div>
            <% } %>
        </article>

        <article class="panel">
            <div class="panel-header">
                <h4><%= i18n.t("ta.dashboard.quick-actions") %></h4>
            </div>
            <div class="button-row">
                <a class="secondary-button" href="<%= request.getContextPath() %>/ta/profile"><%= i18n.t("ta.dashboard.update-profile") %></a>
                <a class="secondary-button" href="<%= request.getContextPath() %>/ta/jobs"><%= i18n.t("ta.dashboard.browse-jobs") %></a>
                <a class="secondary-button" href="<%= request.getContextPath() %>/ta/applications"><%= i18n.t("ta.dashboard.view-applications") %></a>
            </div>
            <p class="muted"><%= i18n.t("ta.dashboard.footnote") %></p>
        </article>
    </div>
</section>
<%@ include file="../fragments/pageEnd.jspf" %>
