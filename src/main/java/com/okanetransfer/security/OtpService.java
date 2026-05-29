package com.okanetransfer.security;

import com.okanetransfer.entity.OtpCode;
import com.okanetransfer.repository.OtpCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private OtpCodeRepository otpCodeRepository;

    public String generateAndSave(String username) {
        // Générer un code à 6 chiffres
        String code = String.format("%06d", new Random().nextInt(999999));

        // Expiration dans 5 minutes
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        OtpCode otpCode = new OtpCode(username, code, expiresAt);
        otpCodeRepository.save(otpCode);

        // TODO: Envoyer par SMS (Twilio, AWS SNS, etc.)
        System.out.println("OTP SMS [" + username + "]: Votre code de vérification est " + code);

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