package com.okanetransfer.security;

import com.okanetransfer.entity.OtpCode;
import com.okanetransfer.repository.OtpCodeRepository;
import com.okanetransfer.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    @Autowired
    private OtpCodeRepository otpCodeRepository;

    @Autowired
    private NotificationService notificationService;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateAndSave(String username) {
        String code = String.format("%06d", secureRandom.nextInt(1_000_000));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        OtpCode otpCode = new OtpCode(username, code, expiresAt);
        otpCodeRepository.save(otpCode);

        // Try SMS, fallback to log for demo
        try {
            notificationService.sendOtp(username, code);
        } catch (Exception e) {
            log.warn("SMS failed, fallback to log: {}", e.getMessage());
            log.info(">>> OTP for [{}]: {}", username, code);
        }

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