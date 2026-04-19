package com.cineband.api;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Answers {@code GET /} before MVC so opening port 8080 in a browser does not show Whitelabel HTML.
 */
public class RootPathFilter extends OncePerRequestFilter {

    private static final byte[] BODY =
            "{\"service\":\"CineBand recommendation-api\",\"hint\":\"Use npm run dev (Vite) for the UI. Try GET /api/movies\"}"
                    .getBytes(StandardCharsets.UTF_8);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        String path = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && path.startsWith(ctx)) {
            path = path.substring(ctx.length());
        }
        if (path == null || path.isEmpty()) {
            path = "/";
        }
        if (!"/".equals(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getOutputStream().write(BODY);
    }
}
