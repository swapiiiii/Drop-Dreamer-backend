package com.dropdreamer.backend.filter;

import com.dropdreamer.backend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // ✅ Public routes
        if (isPublicEndpoint(path, method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ Validate JWT for protected routes
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);
        try {
            String email = jwtUtil.extractUsername(token);

            if (email == null || jwtUtil.isTokenExpired(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or expired token");
                return;
            }

            // (Optional) You could attach user info to the request here

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String path, String method) {
        // Public routes
        if (path.contains("/signup") ||
                path.contains("/login") ||
                path.contains("/verify-otp") ||
                path.contains("/forgot-password") ||
                path.contains("/reset-password")) {
            return true;
        }

        // Allow GET on products (public browsing)
        if (path.startsWith("/products") && method.equalsIgnoreCase("GET")) {
            return true;
        }

        return false;
    }
}
