package com.okanetransfer.security;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
public class BruteForceProtectionService {

    private final Map<String, LoginAttempt> loginAttempts = new HashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MINUTES = 15;

    public class LoginAttempt {
        public int attempts;
        public LocalDateTime lastAttempt;
        public LocalDateTime lockedUntil;

        public LoginAttempt(int attempts, LocalDateTime lastAttempt, LocalDateTime lockedUntil) {
            this.attempts = attempts;
            this.lastAttempt = lastAttempt;
            this.lockedUntil = lockedUntil;
        }
    }

    public boolean isAccountLocked(String username) {
        LoginAttempt attempt = loginAttempts.get(username);
        if (attempt == null) return false;

        if (attempt.lockedUntil != null && LocalDateTime.now().isBefore(attempt.lockedUntil)) {
            return true;
        }

        if (attempt.lockedUntil != null && LocalDateTime.now().isAfter(attempt.lockedUntil)) {
            loginAttempts.remove(username);
        }

        return false;
    }

    public void recordFailedLogin(String username) {
        LocalDateTime now = LocalDateTime.now();
        LoginAttempt attempt = loginAttempts.get(username);

        if (attempt == null) {
            loginAttempts.put(username, new LoginAttempt(1, now, null));
        } else {
            attempt.attempts++;
            attempt.lastAttempt = now;

            if (attempt.attempts >= MAX_ATTEMPTS) {
                attempt.lockedUntil = now.plus(LOCKOUT_DURATION_MINUTES, ChronoUnit.MINUTES);
            }

            loginAttempts.put(username, attempt);
        }
    }

    public void recordSuccessfulLogin(String username) {
        loginAttempts.remove(username);
    }

    public int getFailedAttempts(String username) {
        LoginAttempt attempt = loginAttempts.get(username);
        return attempt != null ? attempt.attempts : 0;
    }
}
