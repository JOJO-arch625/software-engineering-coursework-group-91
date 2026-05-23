<%@ page import="java.util.List" %>
<%
    List<String> notifications = (List<String>) request.getAttribute("notifications");
    List<String> aiTodos = (List<String>) request.getAttribute("aiTodos");
%>
<%@ include file="fragments/pageStart.jspf" %>
<section class="view active" id="gateway">
    <article class="hero-card">
        <div>
            <p class="eyebrow"><%= i18n.t("gateway.eyebrow") %></p>
            <h3><%= i18n.t("gateway.heading") %></h3>
            <p class="muted"><%= i18n.t("gateway.description") %></p>
        </div>
        <div class="hero-meta">
            <span class="pill pill-neutral"><%= i18n.t("gateway.pill-servlet") %></span>
            <span class="pill pill-neutral"><%= i18n.t("gateway.pill-json") %></span>
            <span class="pill pill-warning"><%= i18n.t("gateway.pill-ai") %></span>
        </div>
    </article>

    <div class="role-grid">
        <article class="role-card accent-blue">
            <p class="eyebrow"><%= i18n.t("gateway.ta-eyebrow") %></p>
            <h4><%= i18n.t("gateway.ta-heading") %></h4>
            <p><%= i18n.t("gateway.ta-body") %></p>
            <div class="button-row">
                <a class="primary-button" href="<%= request.getContextPath() %>/ta/dashboard"><%= i18n.t("gateway.ta-button") %></a>
            </div>
        </article>
        <article class="role-card accent-amber">
            <p class="eyebrow"><%= i18n.t("gateway.mo-eyebrow") %></p>
            <h4><%= i18n.t("gateway.mo-heading") %></h4>
            <p><%= i18n.t("gateway.mo-body") %></p>
            <div class="button-row">
                <a class="primary-button" href="<%= request.getContextPath() %>/mo/dashboard"><%= i18n.t("gateway.mo-button") %></a>
            </div>
        </article>
        <article class="role-card accent-green">
            <p class="eyebrow"><%= i18n.t("gateway.admin-eyebrow") %></p>
            <h4><%= i18n.t("gateway.admin-heading") %></h4>
            <p><%= i18n.t("gateway.admin-body") %></p>
            <div class="button-row">
                <a class="primary-button" href="<%= request.getContextPath() %>/admin/workload"><%= i18n.t("gateway.admin-button") %></a>
            </div>
        </article>
    </div>

    <div class="grid two-col">
        <article class="panel">
            <div class="panel-header">
                <h4><%= i18n.t("gateway.coverage-heading") %></h4>
            </div>
            <ul class="feature-list">
                <li><%= i18n.t("gateway.coverage-1") %></li>
                <li><%= i18n.t("gateway.coverage-2") %></li>
                <li><%= i18n.t("gateway.coverage-3") %></li>
                <li><%= i18n.t("gateway.coverage-4") %></li>
                <li><%= i18n.t("gateway.coverage-5") %></li>
            </ul>
        </article>
        <article class="panel">
            <div class="panel-header">
                <h4><%= i18n.t("gateway.highlights-heading") %></h4>
            </div>
            <ul class="feature-list">
                <% for (String item : notifications) { %>
                <li><%= i18n.td(item) %></li>
                <% } %>
            </ul>
        </article>
    </div>

    <div class="grid two-col">
        <article class="panel">
            <div class="panel-header">
                <h4><%= i18n.t("gateway.rules-heading") %></h4>
            </div>
            <ul class="feature-list">
                <li><%= i18n.t("gateway.rules-1") %></li>
                <li><%= i18n.t("gateway.rules-2") %></li>
                <li><%= i18n.t("gateway.rules-3") %></li>
                <li><%= i18n.t("gateway.rules-4") %></li>
            </ul>
        </article>
        <article class="panel">
            <div class="panel-header">
                <h4><%= i18n.t("gateway.todo-heading") %></h4>
            </div>
            <ul class="feature-list">
                <% for (String item : aiTodos) { %>
                <li><%= i18n.td(item) %></li>
                <% } %>
            </ul>
        </article>
    </div>
</section>
<%@ include file="fragments/pageEnd.jspf" %>
