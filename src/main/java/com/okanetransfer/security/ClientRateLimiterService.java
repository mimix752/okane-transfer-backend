package com.okanetransfer.security;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ClientRateLimiterService {

    private final ConcurrentHashMap<String, LocalDateTime> lastActionTime = new ConcurrentHashMap<>();

    public void check(String username, String action, long hours) {
        String key = action + ":" + username;
        LocalDateTime last = lastActionTime.get(key);

        if (last != null && last.plusHours(hours).isAfter(LocalDateTime.now())) {
            long minutesLeft = ChronoUnit.MINUTES.between(LocalDateTime.now(), last.plusHours(hours));
            long hoursLeft = minutesLeft / 60;
            long minsLeft = minutesLeft % 60;
            throw new IllegalStateException(
                    "Action non autorisée. Réessayez dans " + hoursLeft + "h " + minsLeft + "min."
            );
        }

        lastActionTime.put(key, LocalDateTime.now());
    }
}