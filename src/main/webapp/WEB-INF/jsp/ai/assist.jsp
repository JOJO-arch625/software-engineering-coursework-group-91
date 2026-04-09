<%@ page import="java.util.List" %>
<%
    List<String> aiTodos = (List<String>) request.getAttribute("aiTodos");
%>
<%@ include file="../fragments/pageStart.jspf" %>
<section class="view active">
    <article class="hero-card">
        <div>
            <p class="eyebrow">Optional Enhancement</p>
            <h3>AI stays hidden until you choose to explore it</h3>
            <p class="muted">
                This page keeps AI outside the MVP while still showing a clear path for follow-up teammate work.
                Each feature area is collapsed by default so the interface stays clean during demos.
            </p>
        </div>
        <div class="hero-meta">
            <span class="pill pill-warning">Future enhancement</span>
            <span class="pill pill-neutral">Optional panels</span>
        </div>
    </article>

    <div class="ai-layout">
        <div class="ai-main">
            <div class="accordion-stack">
                <details class="accordion-card">
                    <summary>TA-side fit scoring</summary>
                    <div class="accordion-body">
                        <article class="ai-card">
                            <div class="ai-score-shell">
                                <span class="ai-score-label">Illustrative match score</span>
                                <strong>88%</strong>
                            </div>
                            <div class="ai-insights">
                                <p><strong>Purpose:</strong> Help the TA understand likely skill fit before submitting an application.</p>
                                <p><strong>Design choice:</strong> Show a simple score and short rationale, not an automatic decision.</p>
                            </div>
                        </article>
                    </div>
                </details>

                <details class="accordion-card">
                    <summary>MO-side shortlist support</summary>
                    <div class="accordion-body">
                        <article class="ai-card">
                            <div class="ai-score-shell">
                                <span class="ai-score-label">Illustrative shortlist score</span>
                                <strong>82%</strong>
                            </div>
                            <div class="ai-insights">
                                <p><strong>Purpose:</strong> Rank applicants by skill alignment and current workload before the MO reviews manually.</p>
                                <p><strong>Safety:</strong> Final applicant decisions still belong to the MO.</p>
                            </div>
                        </article>
                    </div>
                </details>

                <details class="accordion-card">
                    <summary>Admin-side balancing advice</summary>
                    <div class="accordion-body">
                        <article class="ai-card">
                            <div class="ai-insights">
                                <p><strong>Purpose:</strong> Flag TAs near overload and suggest where redistribution may be needed.</p>
                                <p><strong>Use case:</strong> Support the existing workload dashboard instead of replacing it.</p>
                            </div>
                        </article>
                    </div>
                </details>
            </div>

            <div class="grid two-col ai-support-grid">
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
                        <li>Hide optional panels until the user opens them</li>
                        <li>Present AI output as assistance, not authority</li>
                        <li>Allow teammates to extend AI without blocking the main demo</li>
                    </ul>
                </article>
            </div>
        </div>

        <aside class="ai-rail">
            <article class="panel rail-panel" id="ui-todo">
                <div class="panel-header">
                    <h4>Workspace TODO</h4>
                </div>
                <ul class="feature-list">
                    <li>Search module: add cross-page search for jobs, applicants, and records.</li>
                    <li>Inbox module: add status updates, review reminders, and system alerts.</li>
                    <li>Keep both modules hidden from MVP acceptance until their data flows are implemented.</li>
                </ul>
            </article>

            <article class="panel rail-panel">
                <div class="panel-header">
                    <h4>Admin use of AI later</h4>
                </div>
                <ul class="feature-list">
                    <li>Admin does not recruit directly; Admin monitors allocation across all accepted jobs.</li>
                    <li>AI can later suggest overload balancing, but only after the current workload dashboard is stable.</li>
                    <li>This keeps the current Admin page practical while still leaving clear extension space for teammates.</li>
                </ul>
            </article>
        </aside>
    </div>
</section>
<%@ include file="../fragments/pageEnd.jspf" %>
