<%@ page import="com.group91.tars.model.TAProfile" %>
<%
    TAProfile profile = (TAProfile) request.getAttribute("profile");
    String cvPath = profile == null ? null : profile.getCvPath();
    boolean hasPdfCv = cvPath != null && cvPath.toLowerCase().endsWith(".pdf");
%>
<%@ include file="../fragments/pageStart.jspf" %>
<section class="view active">
    <div class="grid two-col">
        <article class="panel">
            <div class="panel-header">
                <h4><%= i18n.t("ta.profile.heading") %></h4>
                <p><%= i18n.t("ta.profile.description") %></p>
            </div>
            <form method="post" action="<%= request.getContextPath() %>/ta/profile" class="form-grid">
                <input type="hidden" name="action" value="saveProfile">
                <label>
                    <%= i18n.t("ta.profile.full-name") %>
                    <input type="text" name="fullName" value="<%= profile == null ? "" : profile.getFullName() %>" required>
                </label>
                <label>
                    <%= i18n.t("ta.profile.student-number") %>
                    <input type="text" name="studentNumber" value="<%= profile == null ? "" : profile.getStudentNumber() %>" required>
                </label>
                <label>
                    <%= i18n.t("ta.profile.email") %>
                    <input type="email" name="email" value="<%= profile == null ? "" : profile.getEmail() %>" required>
                </label>
                <label>
                    <%= i18n.t("ta.profile.phone") %>
                    <input type="text" name="phone" value="<%= profile == null ? "" : profile.getPhone() %>">
                </label>
                <label class="span-two">
                    <%= i18n.t("ta.profile.skills") %>
                    <input type="text" name="skills" value="<%= profile == null ? "" : profile.getSkills() %>" required>
                </label>
                <label class="span-two">
                    <%= i18n.t("ta.profile.availability") %>
                    <textarea name="availability" required><%= profile == null ? "" : profile.getAvailability() %></textarea>
                </label>
                <div class="button-row span-two">
                    <button class="primary-button" type="submit"><%= i18n.t("ta.profile.save") %></button>
                </div>
            </form>
        </article>

        <article class="panel upload-card">
            <div class="panel-header">
                <h4><%= i18n.t("ta.profile.cv-heading") %></h4>
                <p><%= i18n.t("ta.profile.cv-description") %></p>
            </div>
            <p class="file-name"><%= cvPath != null ? cvPath : i18n.t("ta.profile.cv-no-file") %></p>
            <div class="alert <%= hasPdfCv ? "success" : "info" %>" style="margin-top: 0; margin-bottom: 16px;">
                <strong><%= hasPdfCv ? i18n.t("ta.profile.cv-ai-ready") : i18n.t("ta.profile.cv-ai-pdf-only") %></strong><br>
                <%= hasPdfCv ? i18n.t("ta.profile.cv-ai-ready-detail") : i18n.t("ta.profile.cv-ai-pdf-only-detail") %>
            </div>
            <form method="post" action="<%= request.getContextPath() %>/ta/profile" enctype="multipart/form-data">
                <input type="hidden" name="action" value="uploadCv">
                <label>
                    <%= i18n.t("ta.profile.cv-select") %>
                    <input type="file" name="cvFile" accept=".pdf,.doc,.docx" required>
                </label>
                <div class="button-row">
                    <button class="secondary-button" type="submit"><%= i18n.t("ta.profile.cv-upload") %></button>
                </div>
            </form>
            <!-- <div class="alert info" style="margin-top: 18px;">
                <%= i18n.t("ta.profile.alert") %>
            </div> -->
        </article>
    </div>
</section>
<%@ include file="../fragments/pageEnd.jspf" %>
