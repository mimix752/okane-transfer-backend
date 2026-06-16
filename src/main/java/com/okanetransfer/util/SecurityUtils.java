package com.okanetransfer.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return "anonymous";
        return auth.getName();
    }

    public static Long getCurrentUserId() {
        // Retourner null pour éviter les dépendances circulaires
        // L'audit trail utilisera le username à la place
        return null;
    }
}