package com.group91.tars.servlet;

import com.group91.tars.model.Notification;
import com.group91.tars.model.UserAccount;
import com.group91.tars.service.TarsService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/gateway")
public class HomeServlet extends BasePageServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        if (!requireAuthenticated(request, response)) {
            return;
        }
        UserAccount user = getCurrentUser(request);
        preparePage(request, "gateway", "Gateway", "Prototype Overview");
        request.setAttribute("notifications", buildOverviewNotes(user));
        request.setAttribute("recentNotifications", service.getNotificationsForUser(user.getLinkedId(), user.getRole()));
        request.setAttribute("aiTodos", service.getAiTodoNotes());
        forward(request, response, "/WEB-INF/jsp/gateway.jsp");
    }

    private List<String> buildOverviewNotes(UserAccount currentUser) {
        List<String> notes = new ArrayList<String>();
        if (TarsService.ROLE_TA.equals(currentUser.getRole())) {
            return service.getNotificationsForTa(currentUser.getLinkedId());
        }
        if (TarsService.ROLE_MO.equals(currentUser.getRole())) {
            notes.add("Your MO workspace keeps posting management and applicant review in one place.");
            notes.add("Pending applications are visible before you make accept or reject decisions.");
            notes.add("Optional AI panels remain collapsed until you choose to open them.");
            return notes;
        }
        notes.add("The admin dashboard summarises accepted allocations across all tracked TAs.");
        notes.add("Workload alerts highlight anyone already at the threshold before more offers are confirmed.");
        notes.add("Optional AI panels remain collapsed until you choose to open them.");
        return notes;
    }
}
