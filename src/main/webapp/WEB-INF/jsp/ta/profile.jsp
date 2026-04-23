<%@ page import="com.group91.tars.model.TAProfile" %>
<%
    TAProfile profile = (TAProfile) request.getAttribute("profile");
%>
<%@ include file="../fragments/pageStart.jspf" %>
<section class="view active">
    <div class="grid two-col">
        <article class="panel">
            <div class="panel-header">
                <h4>Applicant Profile</h4>
                <p>Maintain TA details, declared skills, and availability before applying.</p>
            </div>
            <form method="post" action="<%= request.getContextPath() %>/ta/profile" class="form-grid">
                <input type="hidden" name="action" value="saveProfile">
                <label>
                    Full name
                    <input type="text" name="fullName" value="<%= profile == null ? "" : profile.getFullName() %>" required>
                </label>
                <label>
                    Student number
                    <input type="text" name="studentNumber" value="<%= profile == null ? "" : profile.getStudentNumber() %>" required>
                </label>
                <label>
                    Email
                    <input type="email" name="email" value="<%= profile == null ? "" : profile.getEmail() %>" required>
                </label>
                <label>
                    Phone
                    <input type="text" name="phone" value="<%= profile == null ? "" : profile.getPhone() %>">
                </label>
                <label class="span-two">
                    Skills
                    <input type="text" name="skills" value="<%= profile == null ? "" : profile.getSkills() %>" required>
                </label>
                <label class="span-two">
                    Availability
                    <textarea name="availability" required><%= profile == null ? "" : profile.getAvailability() %></textarea>
                </label>
                <div class="button-row span-two">
                    <button class="primary-button" type="submit">Save profile</button>
                </div>
            </form>
        </article>

        <article class="panel upload-card">
            <div class="panel-header">
                <h4>CV Upload</h4>
                <p>Accepted types in final system: PDF, DOC, DOCX</p>
            </div>
            <p class="file-name"><%= profile != null && profile.getCvPath() != null ? profile.getCvPath() : "No CV uploaded yet" %></p>
            <form method="post" action="<%= request.getContextPath() %>/ta/profile" enctype="multipart/form-data">
                <input type="hidden" name="action" value="uploadCv">
                <label>
                    Select CV file
                    <input type="file" name="cvFile" accept=".pdf,.doc,.docx" required>
                </label>
                <div class="button-row">
                    <button class="secondary-button" type="submit">Upload CV</button>
                </div>
            </form>
            <div class="alert info" style="margin-top: 18px;">
                Data is stored locally in files for coursework compliance. No database is used in this build.
            </div>
        </article>
    </div>
</section>
<%@ include file="../fragments/pageEnd.jspf" %>
