package com.group91.tars.servlet;

import com.group91.tars.model.OperationResult;
import com.group91.tars.model.TAProfile;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;

@WebServlet("/ta/profile")
@MultipartConfig
public class TaProfileServlet extends BasePageServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        preparePage(request, "ta-profile", "TA Flow", "TA Profile And CV");
        request.setAttribute("profile", service.getCurrentTaProfile());
        forward(request, response, "/WEB-INF/jsp/ta/profile.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        String action = request.getParameter("action");
        OperationResult result;
        if ("uploadCv".equals(action)) {
            Part cvFile = request.getPart("cvFile");
            result = service.uploadCurrentTaCv(cvFile);
        } else {
            TAProfile existing = service.getCurrentTaProfile();
            TAProfile profile = new TAProfile();
            profile.setFullName(request.getParameter("fullName"));
            profile.setStudentNumber(request.getParameter("studentNumber"));
            profile.setEmail(request.getParameter("email"));
            profile.setPhone(request.getParameter("phone"));
            profile.setSkills(request.getParameter("skills"));
            profile.setAvailability(request.getParameter("availability"));
            profile.setCvPath(existing == null ? null : existing.getCvPath());
            result = service.saveCurrentTaProfile(profile);
        }
        flash(request, result.isSuccess() ? "success" : "error", result.getMessage());
        redirect(request, response, "/ta/profile");
    }
}
