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
    <title>Login | TA Recruitment System</title>
    <link rel="stylesheet" href="<%= contextPath %>/assets/styles/app.css?v=20260409c">
</head>
<body class="login-page">
<header class="login-topbar">
    <div class="topbar-left">
        <span class="wordmark">TA Recruit</span>
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
            <p class="eyebrow">Teaching Assistant Recruitment System</p>
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
                    <label class="login-field">
                        <span>Username</span>
                        <input type="text" name="username" id="username" placeholder="Enter demo username" required>
                    </label>
                    <label class="login-field">
                        <span>Password</span>
                        <input type="password" name="password" id="password" placeholder="Enter demo password" required>
                    </label>
                    <button class="primary-button login-submit" type="submit">Log in</button>
                </form>
            </article>

            <article class="demo-account-card">
                <h3>Demo accounts</h3>
                <p class="muted" style="margin-bottom: 12px; font-size: 13px;">Click an account to auto-fill</p>
                <div class="account-grid">
                    <div class="account-chip clickable-account" data-user="ta.demo" data-pass="TaDemo123">
                        <strong>TA</strong>
                        <span>ta.demo / TaDemo123</span>
                    </div>
                    <div class="account-chip clickable-account" data-user="mo.demo" data-pass="MoDemo123">
                        <strong>MO</strong>
                        <span>mo.demo / MoDemo123</span>
                    </div>
                    <div class="account-chip clickable-account" data-user="admin.demo" data-pass="AdminDemo123">
                        <strong>Admin</strong>
                        <span>admin.demo / AdminDemo123</span>
                    </div>
                </div>
            </article>
        </aside>
    </section>
</main>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        // Handle demo account clicks
        const chips = document.querySelectorAll('.clickable-account');
        const userInp = document.getElementById('username');
        const passInp = document.getElementById('password');

        chips.forEach(chip => {
            chip.addEventListener('click', function() {
                userInp.value = this.getAttribute('data-user');
                passInp.value = this.getAttribute('data-pass');
                
                // Optional: visual feedback
                chip.style.transform = 'scale(0.98)';
                setTimeout(() => chip.style.transform = '', 100);
            });
        });

        // Form submission feedback
        const form = document.querySelector('.login-form');
        const submitBtn = document.querySelector('.login-submit');
        
        form.addEventListener('submit', function() {
            submitBtn.textContent = 'Logging in...';
            submitBtn.style.opacity = '0.8';
            submitBtn.style.pointerEvents = 'none';
        });
    });
</script>
</body>
</html>
