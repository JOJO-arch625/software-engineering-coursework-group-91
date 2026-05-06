package com.group91.tars.i18n;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;

/**
 * Servlet filter that manages the user's locale preference across all requests.
 * Inspects the {@code lang} query parameter and, if present, stores the
 * corresponding {@link Locale} in the session. Defaults to English.
 */
@WebFilter("/*")
public class LocaleFilter implements Filter {

    public static final String SESSION_LOCALE_KEY = "locale";
    public static final String LANG_EN = "en";
    public static final String LANG_ZH = "zh";

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpSession session = request.getSession(true);

        String langParam = request.getParameter("lang");
        if (langParam != null) {
            if (LANG_ZH.equals(langParam)) {
                session.setAttribute(SESSION_LOCALE_KEY, Locale.SIMPLIFIED_CHINESE);
            } else if (LANG_EN.equals(langParam)) {
                session.setAttribute(SESSION_LOCALE_KEY, Locale.ENGLISH);
            }
        }

        if (session.getAttribute(SESSION_LOCALE_KEY) == null) {
            session.setAttribute(SESSION_LOCALE_KEY, Locale.ENGLISH);
        }

        chain.doFilter(req, resp);
    }

    @Override
    public void init(FilterConfig f) {}

    @Override
    public void destroy() {}
}
