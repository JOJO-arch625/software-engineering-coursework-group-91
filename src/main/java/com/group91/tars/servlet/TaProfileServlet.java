package com.group91.tars.servlet;

import com.group91.tars.model.OperationResult;
import com.group91.tars.model.TAProfile;
import com.group91.tars.service.TarsService;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;

/**
 * Handles TA profile viewing and editing at {@code /ta/profile}.
 * GET renders the profile form; POST processes profile saves and CV uploads.
 */
@WebServlet("/ta/profile")
@MultipartConfig
public class TaProfileServlet extends BasePageServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        if (!requireRole(request, response, TarsService.ROLE_TA)) {
            return;
        }
        preparePage(request, "ta-profile", "TA Flow", "TA Profile And CV");
        request.setAttribute("profile", service.getTaProfile(getCurrentUser(request).getLinkedId()));
        forward(request, response, "/WEB-INF/jsp/ta/profile.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        if (!requireRole(request, response, TarsService.ROLE_TA)) {
            return;
        }
        if (isForeignTaId(request)) {
            flashI18n(request, "error", "flash.auth.no-permission");
            redirect(request, response, "/ta/profile");
            return;
        }
        String action = request.getParameter("action");
        OperationResult result;
        if ("uploadCv".equals(action)) {
            Part cvFile = request.getPart("cvFile");
            result = service.uploadTaCv(getCurrentUser(request).getLinkedId(), cvFile);
        } else {
            TAProfile existing = service.getTaProfile(getCurrentUser(request).getLinkedId());
            TAProfile profile = new TAProfile();
            profile.setFullName(request.getParameter("fullName"));
            profile.setStudentNumber(request.getParameter("studentNumber"));
            profile.setEmail(request.getParameter("email"));
            profile.setPhone(request.getParameter("phone"));
            profile.setSkills(request.getParameter("skills"));
            profile.setAvailability(request.getParameter("availability"));
            profile.setCvPath(existing == null ? null : existing.getCvPath());
            result = service.saveTaProfile(getCurrentUser(request).getLinkedId(), profile);
        }
        if (result.isSuccess()) {
            flashI18n(request, "success", result.getMessageKey() != null ? result.getMessageKey() : "flash.profile.saved");
        } else {
            flashI18n(request, "error", result.getMessageKey() != null ? result.getMessageKey() : "flash.profile.validation");
        }
        String returnTo = request.getParameter("returnTo");
        redirect(request, response, isSafeReturnPath(returnTo) ? returnTo : "/ta/profile");
    }

    private boolean isSafeReturnPath(String value) {
        return value != null && value.startsWith("/") && !value.startsWith("//") && !value.contains("://");
    }

    private boolean isForeignTaId(HttpServletRequest request) {
        String postedTaId = request.getParameter("taId");
        return postedTaId != null
            && !postedTaId.trim().isEmpty()
            && !postedTaId.equals(getCurrentUser(request).getLinkedId());
    }
}
