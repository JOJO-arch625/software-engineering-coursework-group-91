package com.group91.tars.web;

import com.group91.tars.model.FlashMessage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Utility for storing and consuming one-time flash messages across redirects.
 * Messages are stored in the HTTP session and automatically removed when consumed
 * on the next GET request, implementing the PRG (Post-Redirect-Get) pattern.
 */
public final class FlashScope {
    private static final String FLASH_KEY = "flashMessage";

    private FlashScope() {
    }

    /**
     * Stores a flash message in the session for display on the next request.
     *
     * @param request the current HTTP request
     * @param level   severity level: "success", "info", or "error"
     * @param text    the message text to display
     */
    public static void put(HttpServletRequest request, String level, String text) {
        HttpSession session = request.getSession();
        session.setAttribute(FLASH_KEY, new FlashMessage(level, text));
    }

    /**
     * Retrieves and removes the flash message from the session.
     *
     * @param request the current HTTP request
     * @return the stored FlashMessage, or null if none exists
     */
    public static FlashMessage consume(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        FlashMessage message = (FlashMessage) session.getAttribute(FLASH_KEY);
        if (message != null) {
            session.removeAttribute(FLASH_KEY);
        }
        return message;
    }
}
