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

        // CORS
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String path = request.getRequestURI();
        String method = request.getMethod();

        // ---- PUBLIC ENDPOINTS ----
        if (isPublicEndpoint(request, path, method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // ---- PRIVATE ENDPOINTS -> JWT REQUIRED ----
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

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(HttpServletRequest request, String path, String method) {

        // Auth APIs
        if (path.contains("/signup") ||
                path.contains("/login") ||
                path.contains("/verify-otp") ||
                path.contains("/forgot-password") ||
                path.contains("/reset-password")) {
            return true;
        }

        // Public product GET
        if (path.startsWith("/products") && method.equalsIgnoreCase("GET")) {
            return true;
        }

        // ---- GUEST CART ACCESS (SESSION ID PROVIDED) ----
        if (path.startsWith("/cart") && request.getParameter("sessionId") != null) {
            return true; // allow for add/remove/update/get
        }

        // For logged-in users: /cart/* is protected → requires token
        return false;
    }

}
