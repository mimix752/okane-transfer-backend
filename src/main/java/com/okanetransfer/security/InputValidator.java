package com.okanetransfer.security;

import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

@Component
public class InputValidator {

    private static final Pattern SAFE_STRING_PATTERN = Pattern.compile("^[a-zA-Z0-9\\-_.@\\s]*$");
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            ".*(;|'|--|/\\*|\\*/).*", Pattern.CASE_INSENSITIVE);

    public boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }

    public boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.isBlank()) return false;
        return phone.matches("^[+]?[0-9]{7,15}$");
    }

    public boolean isValidCountryCode(String code) {
        if (code == null || code.isBlank()) return false;
        return code.matches("^[A-Z]{2,3}$");
    }

    public boolean isSafeString(String input) {
        if (input == null) return true;
        return SAFE_STRING_PATTERN.matcher(input).matches();
    }

    public boolean isSqlInjectionAttempt(String input) {
        if (input == null) return false;
        return SQL_INJECTION_PATTERN.matcher(input).matches();
    }

    public String sanitizeInput(String input) {
        if (input == null) return null;
        return input.trim()
                .replaceAll("[<>\"'%;()&+]", "")
                .replaceAll("\\s+", " ");
    }
}
