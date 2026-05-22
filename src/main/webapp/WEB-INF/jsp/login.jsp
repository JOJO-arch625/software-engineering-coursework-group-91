<%@ page contentType="text/html;charset=UTF-8" language="java"
    import="com.group91.tars.model.FlashMessage,com.group91.tars.i18n.I18n,java.util.Locale" %>
<%
    FlashMessage flash = (FlashMessage) request.getAttribute("flash");
    String contextPath = request.getContextPath();
    java.util.Locale loginLocale = (java.util.Locale) request.getSession(true).getAttribute("locale");
    if (loginLocale == null) { loginLocale = java.util.Locale.ENGLISH; }
    I18n i18n = new I18n(loginLocale);
    String lang = loginLocale.getLanguage();
    String newLang = "zh".equals(lang) ? "en" : "zh";
    String qs = request.getQueryString();
    String langToggleUrl;
    if (qs == null) {
        langToggleUrl = "?lang=" + newLang;
    } else if (qs.contains("lang=")) {
        langToggleUrl = "?" + qs.replaceAll("lang=(en|zh)", "lang=" + newLang);
    } else {
        langToggleUrl = "?" + qs + "&lang=" + newLang;
    }
%>
<!DOCTYPE html>
<html lang="<%= lang %>">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login | <%= i18n.t("page.title.suffix") %></title>
    <link rel="stylesheet" href="<%= contextPath %>/assets/styles/app.css?v=20260522b">
</head>
<body class="login-page">
<header class="login-topbar">
    <div class="topbar-left">
        <span class="wordmark"><%= i18n.t("brand.title") %></span>
    </div>
    <div class="topbar-right">
        <button class="help-toggle" type="button" data-help-open title="<%= i18n.t("topbar.help") %>"><%= i18n.t("topbar.help") %></button>
        <a class="lang-toggle" href="<%= langToggleUrl %>" title="<%= "zh".equals(lang) ? "Switch to English" : "切换到中文" %>">
            <%= "zh".equals(lang) ? "EN" : "中文" %>
        </a>
    </div>
</header>

<main class="login-shell">
    <section class="login-hero">
        <section class="login-copy-panel">
            <h1><%= i18n.t("login.hero-heading") %></h1>
            <p class="login-lead"><%= i18n.t("login.hero-lead") %></p>

            <div class="login-feature-grid">
                <article class="login-feature-card">
                    <h3><%= i18n.t("login.card-ta-heading") %></h3>
                    <p><%= i18n.t("login.card-ta-body") %></p>
                </article>
                <article class="login-feature-card">
                    <h3><%= i18n.t("login.card-mo-heading") %></h3>
                    <p><%= i18n.t("login.card-mo-body") %></p>
                </article>
                <article class="login-feature-card">
                    <h3><%= i18n.t("login.card-admin-heading") %></h3>
                    <p><%= i18n.t("login.card-admin-body") %></p>
                </article>
            </div>
        </section>

        <aside class="login-card-shell">
            <article class="login-card">
                <div class="login-card-header">
                    <p class="eyebrow"><%= i18n.t("login.signin-eyebrow") %></p>
                    <h2><%= i18n.t("login.signin-heading") %></h2>
                    <p class="muted"><%= i18n.t("login.signin-description") %></p>
                </div>

                <% if (flash != null) { %>
                <div class="alert <%= "success".equals(flash.getLevel()) ? "success" : ("info".equals(flash.getLevel()) ? "info" : "danger") %>">
                    <%= flash.getText() %>
                </div>
                <% } %>

                <form class="login-form" method="post" action="<%= contextPath %>/login">
                    <label>
                        <%= i18n.t("login.label-username") %>
                        <input type="text" name="username" placeholder="<%= i18n.t("login.placeholder-username") %>" required>
                    </label>
                    <label>
                        <%= i18n.t("login.label-password") %>
                        <input type="password" name="password" placeholder="<%= i18n.t("login.placeholder-password") %>" required>
                    </label>
                    <button class="primary-button login-submit" type="submit"><%= i18n.t("login.button-login") %></button>
                </form>
            </article>

            <article class="demo-account-card">
                <h3><%= i18n.t("login.demo-heading") %></h3>
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
<%@ include file="fragments/helpModal.jspf" %>
</body>
</html>
