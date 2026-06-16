package com.okanetransfer.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting sur /api/auth/login et /api/auth/verify-otp
 * Max 5 tentatives par 10 minutes par IP (CDC 4.3)
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 10 * 60 * 1000L; // 10 minutes

    // Map : IP -> [compteur, timestamp première tentative de la fenêtre]
    private final Map<String, long[]> attempts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        // Appliquer uniquement sur les endpoints d'authentification
        boolean isAuthEndpoint = uri.contains("/api/auth/login")
                || uri.contains("/api/auth/verify-otp");

        if (!isAuthEndpoint) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = getClientIp(request);
        long now = Instant.now().toEpochMilli();

        attempts.compute(ip, (key, val) -> {
            if (val == null || (now - val[1]) > WINDOW_MS) {
                // Première tentative ou fenêtre expirée → reset
                return new long[]{1, now};
            }
            val[0]++; // incrémenter compteur
            return val;
        });

        long[] data = attempts.get(ip);

        if (data[0] > MAX_ATTEMPTS) {
            long retryAfterSec = (WINDOW_MS - (now - data[1])) / 1000;
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"error\": \"Trop de tentatives de connexion. Réessayez dans "
                            + retryAfterSec + " secondes.\","
                            + "\"status\": 429}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}