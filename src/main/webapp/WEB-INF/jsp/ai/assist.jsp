<%@ page import="java.util.List" %>
<%
    List<String> aiTodos = (List<String>) request.getAttribute("aiTodos");
%>
<%@ include file="../fragments/pageStart.jspf" %>
<section class="view active">
    <article class="hero-card">
        <div>
            <p class="eyebrow">Optional Enhancement</p>
            <h3>AI features are shown as future decision support, not core workflow</h3>
            <p class="muted">
                Coursework guidance does not require AI for full marks. This page keeps clear TODO space for teammates without blocking the core system demo.
            </p>
        </div>
        <div class="hero-meta">
            <span class="pill pill-warning">Future enhancement</span>
            <span class="pill pill-neutral">No dependency for MVP</span>
        </div>
    </article>

    <div class="role-grid">
        <article class="role-card accent-blue">
            <p class="eyebrow">TA-side feature</p>
            <h4>Fit score</h4>
            <p>Show likely job fit before the TA submits an application.</p>
        </article>
        <article class="role-card accent-amber">
            <p class="eyebrow">MO-side feature</p>
            <h4>Shortlist support</h4>
            <p>Rank applicants by skill fit and workload awareness.</p>
        </article>
        <article class="role-card accent-green">
            <p class="eyebrow">Admin-side feature</p>
            <h4>Balancing advice</h4>
            <p>Flag overload and suggest redistribution ideas.</p>
        </article>
    </div>

    <div class="grid two-col">
        <article class="panel">
            <div class="panel-header">
                <h4>Reserved TODO Items</h4>
            </div>
            <ul class="feature-list">
                <% for (String item : aiTodos) { %>
                <li><%= item %></li>
                <% } %>
            </ul>
        </article>
        <article class="panel">
            <div class="panel-header">
                <h4>Safe Coursework Positioning</h4>
            </div>
            <ul class="feature-list">
                <li>Keep AI outside MVP acceptance criteria</li>
                <li>State that AI output is assistive, not authoritative</li>
                <li>Do not require internet access for the base workflow</li>
                <li>Prioritise the core recruitment flow first, AI second</li>
            </ul>
        </article>
    </div>
</section>
<%@ include file="../fragments/pageEnd.jspf" %>
