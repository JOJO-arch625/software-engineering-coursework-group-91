package com.group91.tars.i18n;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter that ensures all HTTP requests and responses use UTF-8 encoding.
 * This resolves character encoding issues for internationalized content,
 * particularly for Chinese characters in JSP pages.
 */
@WebFilter("/*")
public class EncodingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        // Set character encoding for request
        request.setCharacterEncoding("UTF-8");

        // Set character encoding for response
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        chain.doFilter(req, resp);
    }

    @Override
    public void init(FilterConfig f) {}

    @Override
    public void destroy() {}
}
