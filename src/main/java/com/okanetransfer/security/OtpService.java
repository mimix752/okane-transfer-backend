package com.okanetransfer.security;

import com.okanetransfer.entity.OtpCode;
import com.okanetransfer.repository.OtpCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class OtpService {

    @Autowired
    private OtpCodeRepository otpCodeRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateAndSave(String username) {
        String code = String.format("%06d", secureRandom.nextInt(1_000_000));

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        OtpCode otpCode = new OtpCode(username, code, expiresAt);
        otpCodeRepository.save(otpCode);
        System.out.println("OTP SMS [" + otpCode.getCode() + "]: Code de verification genere (non affiche pour securite)");
        String safeUsername = username != null ? username.replaceAll("[\\r\\n]", "") : "";
        System.out.println("OTP SMS [" + safeUsername + "]: Code de verification genere (non affiche pour securite)");

        return code;
    }

    public boolean verify(String username, String code) {
        return otpCodeRepository
                .findTopByUsernameAndUsedFalseOrderByExpiresAtDesc(username)
                .map(otp -> {
                    if (otp.getExpiresAt().isBefore(LocalDateTime.now())) return false;
                    if (!otp.getCode().equals(code)) return false;
                    otp.setUsed(true);
                    otpCodeRepository.save(otp);
                    return true;
                })
                .orElse(false);
    }
}