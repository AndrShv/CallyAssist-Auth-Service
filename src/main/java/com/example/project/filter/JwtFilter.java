package com.example.project.filter;


import com.example.project.services.custom.CustomUserDetailsService;
import com.example.project.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractJwtFromRequest(request);
        String email = null;

        log.debug("Processing request: {} {}", request.getMethod(), request.getRequestURI());
        log.debug("Token found: {}", token != null ? "YES (first 20 chars: " + token.substring(0, Math.min(20, token.length())) + "...)" : "NO");

        if (request.getRequestURI().startsWith("/oauth2")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        if (token != null) {
            try {
                if (jwtUtil.validateToken(token)) {
                    email = jwtUtil.getEmailFromToken(token);
                    log.debug("Token is valid. Email: {}", email);
                } else {
                    log.warn("Token validation failed");
                }
            } catch (Exception e) {
                log.error("Error validating token: {}", e.getMessage());
                token = null;
            }
        } else {
            log.warn("No JWT token found in request");
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UUID userId = jwtUtil.getUserIdFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            log.debug("User loaded: {}, userId: {}, authorities: {}",
                    userDetails.getUsername(), userId, userDetails.getAuthorities());

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId.toString(), null, userDetails.getAuthorities());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("✅ Authentication set for user: {} (userId: {}) with roles: {}",
                    email, userId, userDetails.getAuthorities());
        } else if (email == null) {
            log.warn("❌ Email is null, authentication not set");
        } else {
            log.debug("Authentication already exists in context");
        }

        filterChain.doFilter(request, response);
    }


    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        log.debug("Authorization header: {}", bearerToken != null ? bearerToken.substring(0, Math.min(30, bearerToken.length())) + "..." : "null");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            log.debug("Extracted token from Bearer header");
            return token;
        }

        if (request.getCookies() != null) {
            log.debug("Checking cookies for JWT...");
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> {
                        log.debug("Cookie found: {} = {}", cookie.getName(), cookie.getValue().substring(0, Math.min(20, cookie.getValue().length())) + "...");
                        return "jwt".equals(cookie.getName());
                    })
                    .map(cookie -> {
                        log.debug("JWT cookie found!");
                        return cookie.getValue();
                    })
                    .findFirst()
                    .orElse(null);
        }

        log.debug("No cookies in request");
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        boolean shouldSkip =
                path.startsWith("/api/auth/") && !path.equals("/api/auth/me") ||
                        path.startsWith("/api/password/reset/") ||
                        path.startsWith("/actuator/") ||
                        path.startsWith("/auth/") ||
                        path.startsWith("/oauth2/") ||
                        path.startsWith("/css/") ||
                        path.startsWith("/static/") ||
                        path.startsWith("/images/") ||
                        path.startsWith("/login/oauth2/") ||
                        path.startsWith("/js/");


        log.debug("Path: {}, shouldNotFilter: {}", path, shouldSkip);
        return shouldSkip;
    }

}
