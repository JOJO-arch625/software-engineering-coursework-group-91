package com.group91.tars.servlet;

import com.group91.tars.model.ApplicationRecord;
import com.group91.tars.model.JobPosting;
import com.group91.tars.model.TAProfile;
import com.group91.tars.model.UserAccount;
import com.group91.tars.service.TarsService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@WebServlet("/cv/view")
public class CvFileServlet extends BasePageServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        if (!requireAuthenticated(request, response)) {
            return;
        }

        String taId = request.getParameter("taId");
        UserAccount currentUser = getCurrentUser(request);
        TAProfile profile = service.getProfileById(taId);
        if (profile == null || !canViewCv(currentUser, taId) || profile.getCvPath() == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        File cvFile = resolveCvFile(profile.getCvPath());
        if (cvFile == null || !cvFile.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String mimeType = getServletContext().getMimeType(cvFile.getName());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        response.setContentType(mimeType);
        response.setHeader("Content-Disposition", "inline; filename=\"" + cvFile.getName().replace("\"", "") + "\"");
        response.setContentLength((int) cvFile.length());
        Files.copy(cvFile.toPath(), response.getOutputStream());
    }

    private boolean canViewCv(UserAccount currentUser, String taId) {
        if (currentUser == null || taId == null) {
            return false;
        }
        if (TarsService.ROLE_ADMIN.equals(currentUser.getRole())) {
            return true;
        }
        if (TarsService.ROLE_TA.equals(currentUser.getRole())) {
            return taId.equals(currentUser.getLinkedId());
        }
        if (!TarsService.ROLE_MO.equals(currentUser.getRole())) {
            return false;
        }
        for (JobPosting job : service.getJobsForMo(currentUser.getLinkedId())) {
            for (ApplicationRecord application : service.getApplicationsForJob(job.getId())) {
                if (taId.equals(application.getTaId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private File resolveCvFile(String cvPath) throws IOException {
        Path root = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path uploadRoot = root.resolve(Paths.get("uploads", "cv")).normalize();
        Path candidate = Paths.get(cvPath);
        Path resolved = candidate.isAbsolute() ? candidate.normalize() : root.resolve(candidate).normalize();
        if (!resolved.startsWith(uploadRoot)) {
            return null;
        }
        return resolved.toFile();
    }
}
