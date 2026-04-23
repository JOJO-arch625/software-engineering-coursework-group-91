package com.group91.tars.web;

import com.group91.tars.model.FlashMessage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public final class FlashScope {
    private static final String FLASH_KEY = "flashMessage";

    private FlashScope() {
    }

    public static void put(HttpServletRequest request, String level, String text) {
        HttpSession session = request.getSession();
        session.setAttribute(FLASH_KEY, new FlashMessage(level, text));
    }

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
