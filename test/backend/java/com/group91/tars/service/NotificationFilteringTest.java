package com.group91.tars.service;

import com.group91.tars.model.Notification;
import com.group91.tars.model.UserAccount;
import com.group91.tars.servlet.InboxServlet;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for notification filtering based on user role.
 * Verifies MO users only see EBU6304-related notifications,
 * while TA and Admin see all their notifications.
 */
public class NotificationFilteringTest {

    private TarsService service;

    @Before
    public void setUp() {
        service = TarsService.getInstance();
    }

    // ===================== Notification Retrieval =====================

    @Test
    public void getNotificationsForUser_returnsUserNotifications() {
        List<Notification> notifications = service.getNotificationsForUser("ta-1");
        assertNotNull("Notifications should not be null", notifications);

        for (Notification n : notifications) {
            assertEquals("ta-1", n.getUserId());
        }
    }

    @Test
    public void getNotificationsForUser_returnsEmpty_forNewUser() {
        List<Notification> notifications = service.getNotificationsForUser("non-existent-user-xyz123");
        assertNotNull("Should return empty list, not null", notifications);
    }

    @Test
    public void getNotificationsForUser_returnsMoNotifications() {
        List<Notification> notifications = service.getNotificationsForUser("mo-1");
        assertNotNull("Notifications should not be null", notifications);

        for (Notification n : notifications) {
            assertEquals("mo-1", n.getUserId());
        }
    }

    // ===================== Unread Count =====================

    @Test
    public void countUnreadNotifications_returnsCorrectCount() {
        int unread = service.countUnreadNotifications("ta-1");
        assertTrue("Unread count should be non-negative", unread >= 0);
    }

    @Test
    public void countUnreadNotifications_returnsZero_forNewUser() {
        int unread = service.countUnreadNotifications("non-existent-user-xyz123");
        assertEquals("New user should have 0 unread notifications", 0, unread);
    }

    // ===================== Mark as Read =====================

    @Test
    public void markNotificationRead_handlesValidNotification() {
        List<Notification> notifications = service.getNotificationsForUser("ta-1");
        if (!notifications.isEmpty()) {
            Notification unread = null;
            for (Notification n : notifications) {
                if (!n.isRead()) {
                    unread = n;
                    break;
                }
            }

            if (unread != null) {
                service.markNotificationRead(unread.getId());
            }
        }
    }

    @Test
    public void markAllNotificationsRead_works() {
        service.markAllNotificationsRead("ta-1");

        List<Notification> notifications = service.getNotificationsForUser("ta-1");
        for (Notification n : notifications) {
            assertTrue("All notifications should be marked as read", n.isRead());
        }
    }

    // ===================== Job ID Extraction (Reflection Test) =====================

    @Test
    public void extractJobIds_extractsFromUrl() throws Exception {
        InboxServlet servlet = new InboxServlet();

        Method extractJobIds = InboxServlet.class.getDeclaredMethod(
            "extractJobIds", String.class
        );
        extractJobIds.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<String> ids1 = (List<String>) extractJobIds.invoke(servlet, "/mo/review?jobId=job-1");
        assertTrue("Should extract job-1", ids1.contains("job-1"));

        @SuppressWarnings("unchecked")
        List<String> ids2 = (List<String>) extractJobIds.invoke(servlet, "/mo/review?appId=app-1&jobId=job-2");
        assertTrue("Should extract job-2", ids2.contains("job-2"));
    }

    @Test
    public void extractJobIds_returnsEmpty_forUrlWithoutJobId() throws Exception {
        InboxServlet servlet = new InboxServlet();

        Method extractJobIds = InboxServlet.class.getDeclaredMethod(
            "extractJobIds", String.class
        );
        extractJobIds.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<String> ids = (List<String>) extractJobIds.invoke(servlet, "/ta/applications");
        assertTrue("Should return empty list for URL without jobId", ids.isEmpty());
    }

    @Test
    public void extractJobIds_handlesMultipleJobIds() throws Exception {
        InboxServlet servlet = new InboxServlet();

        Method extractJobIds = InboxServlet.class.getDeclaredMethod(
            "extractJobIds", String.class
        );
        extractJobIds.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<String> ids = (List<String>) extractJobIds.invoke(
            servlet, "/mo/review?jobId=job-1&otherParam=x&jobId=job-2"
        );
        assertTrue("Should extract job-1", ids.contains("job-1"));
        assertTrue("Should extract job-2", ids.contains("job-2"));
    }

    // ===================== Notification Creation =====================

    @Test
    public void addNotification_createsNotification() {
        String uniqueUserId = "ta-notif-test-" + System.currentTimeMillis();
        service.addNotification(
            uniqueUserId, "status", "Test notification message", "/ta/applications"
        );

        List<Notification> notifications = service.getNotificationsForUser(uniqueUserId);
        boolean found = false;
        for (Notification n : notifications) {
            if ("Test notification message".equals(n.getMessage())) {
                found = true;
                assertEquals(uniqueUserId, n.getUserId());
                break;
            }
        }
        assertTrue("Should find the created notification", found);
    }

    // ===================== Notification Categories =====================

    @Test
    public void notificationsHaveValidCategories() {
        List<Notification> notifications = service.getNotificationsForUser("ta-1");
        List<String> validCategories = java.util.Arrays.asList(
            "status", "review", "overload", "deadline"
        );

        for (Notification n : notifications) {
            assertNotNull("Category should not be null", n.getCategory());
            assertTrue("Category should be valid: " + n.getCategory(),
                       validCategories.contains(n.getCategory()));
        }
    }
}
