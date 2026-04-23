<%@ page contentType="text/html;charset=UTF-8" language="java"
    import="com.group91.tars.model.FlashMessage" %>
<%
    FlashMessage flash = (FlashMessage) request.getAttribute("flash");
    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login | INTERNATIONAL SCHOOL TA RECRUITMENT SYSTEM</title>
    <link rel="stylesheet" href="<%= contextPath %>/assets/styles/app.css?v=20260409c">
</head>
<body class="login-page">
<header class="login-topbar">
    <div class="topbar-left">
        <span class="wordmark">INTERNATIONAL SCHOOL TA RECRUITMENT SYSTEM</span>
        <div class="topbar-links">
            <span>Coursework portal</span>
            <span>Role-based access</span>
        </div>
    </div>
    <div class="topbar-right">
        <span class="icon-badge">Local demo</span>
    </div>
</header>

<main class="login-shell">
    <section class="login-hero">
        <section class="login-copy-panel">
            <p class="eyebrow">INTERNATIONAL SCHOOL TA RECRUITMENT SYSTEM</p>
            <h1>Track recruitment clearly, not through scattered spreadsheets.</h1>
            <p class="login-lead">
                This portal brings TA applications, MO review, and Admin workload monitoring into one
                cleaner workflow with role-based access and local file storage.
            </p>

            <div class="login-pill-row">
                <span class="pill pill-warning">No database</span>
                <span class="pill pill-neutral">JSON + local files</span>
                <span class="pill pill-neutral">Core workflow first</span>
            </div>

            <div class="login-feature-grid">
                <article class="login-feature-card">
                    <h3>For TAs</h3>
                    <p>Maintain your profile, upload a CV, browse open roles, and track application status.</p>
                </article>
                <article class="login-feature-card">
                    <h3>For MOs</h3>
                    <p>Publish postings, review applicants, and update decisions from one workspace.</p>
                </article>
                <article class="login-feature-card">
                    <h3>For Admin</h3>
                    <p>Monitor accepted allocations and spot workload pressure before it becomes a problem.</p>
                </article>
            </div>
        </section>

        <aside class="login-card-shell">
            <article class="login-card">
                <div class="login-card-header">
                    <p class="eyebrow">Sign in</p>
                    <h2>Open your role workspace</h2>
                    <p class="muted">Use one of the local demo accounts below to access the TA, MO, or Admin flow.</p>
                </div>

                <% if (flash != null) { %>
                <div class="alert <%= "success".equals(flash.getLevel()) ? "success" : ("info".equals(flash.getLevel()) ? "info" : "danger") %>">
                    <%= flash.getText() %>
                </div>
                <% } %>

                <form class="login-form" method="post" action="<%= contextPath %>/login">
                    <label>
                        Username
                        <input type="text" name="username" placeholder="Enter demo username" required>
                    </label>
                    <label>
                        Password
                        <input type="password" name="password" placeholder="Enter demo password" required>
                    </label>
                    <button class="primary-button login-submit" type="submit">Log in</button>
                </form>
            </article>

            <article class="demo-account-card">
                <h3>Demo accounts</h3>
                <div class="account-grid">
                    <div class="account-chip">
                        <strong>TA</strong>
                        <span>ta.demo / TaDemo123</span>
                    </div>
                    <div class="account-chip">
                        <strong>MO</strong>
                        <span>mo.demo / MoDemo123</span>
                    </div>
                    <div class="account-chip">
                        <strong>Admin</strong>
                        <span>admin.demo / AdminDemo123</span>
                    </div>
                </div>
            </article>
        </aside>
    </section>
</main>
</body>
</html>
