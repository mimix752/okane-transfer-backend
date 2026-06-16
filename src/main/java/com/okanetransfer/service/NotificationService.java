package com.okanetransfer.service;

import com.okanetransfer.entity.Transfer;
import com.okanetransfer.entity.User;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @Value("${twilio.messaging-service-sid:}")
    private String messagingServiceSid;

    private boolean twilioEnabled = false;

    @PostConstruct
    public void init() {
        if (!accountSid.isBlank() && !authToken.isBlank()
                && !accountSid.startsWith("<")) {
            Twilio.init(accountSid, authToken);
            twilioEnabled = true;
            log.info("Twilio SMS initialized");
        } else {
            log.warn("Twilio not configured — SMS will be logged only");
        }
    }

    public void sendOtp(String phone, String code) {
        String message = "Okane Transfer - Votre code de verification: " + code;
        if (twilioEnabled) {
            sendSms(phone, message);
        } else {
            // Fallback: always log OTP so demo works without Twilio
            log.info(">>> OTP [{}]: {}", sanitize(phone), code);
        }
    }

    public void sendReceiptBySMS(Transfer transfer, BigDecimal fees) {
        String message = String.format(
                "Okane Transfer - Code retrait: %s | Montant: %.2f | Frais: %.2f",
                transfer.getTransferCode(), transfer.getAmount(), fees);
        sendSms(transfer.getRecipientPhone(), message);
    }

    public void sendMobileMoneyNotification(String mobileAccount, String operator,
                                            BigDecimal amount, String transferCode) {
        String safeAccount  = sanitize(mobileAccount);
        String safeCode     = sanitize(transferCode);
        String safeOperator = sanitize(operator);
        String message = String.format(
                "[%s] Vous avez recu %.2f sur votre compte. Code: %s - Okane Transfer",
                safeOperator, amount, safeCode);
        sendSms(safeAccount, message);
    }

    public void sendStatusChangeNotification(User user, String transferCode, String newStatus, String string) {
        if (!user.isNotifyEmail()) {
            log.debug("Email ignoré (préférence désactivée) pour {}", user.getEmail());
            return;
        }
        log.info("NOTIFICATION EMAIL [{}]: statut transfert {} -> {}", user.getEmail(), transferCode, newStatus);
    }

    public void sendProfileUpdateNotification(User user) {
        if (!user.isNotifyEmail()) {
            log.debug("Email ignoré (préférence désactivée) pour {}", user.getEmail());
            return;
        }
        log.info("NOTIFICATION EMAIL [{}]: profil mis a jour pour {}", user.getEmail(), user.getUsername());
    }

    public void sendAccountDeletionNotification(User user) {
        if (!user.isNotifyEmail()) return;
        log.info("NOTIFICATION EMAIL [{}]: compte supprime pour {}", user.getEmail(), user.getUsername());
    }

    public void sendReceiptByEmail(Transfer transfer, BigDecimal fees, String recipientEmail) {
        log.info("NOTIFICATION EMAIL [{}]: recu transfert {}", recipientEmail, transfer.getTransferCode());
    }

    public void printReceipt(String receipt) {
        // TODO: imprimante thermique
    }

    public void sendStatusChangeSmsNotification(User user, String transferCode, String newStatus) {
        if (!user.isNotifySms()) {
            log.debug("SMS ignoré (préférence désactivée) pour {}", user.getEmail());
            return;
        }
        String message = String.format(
                "Okane Transfer - Transfert %s: statut mis a jour -> %s", transferCode, newStatus);
        sendSms(user.getPhone(), message);
    }

    private void sendSms(String to, String body) {
        if (to == null || to.isBlank()) {
            log.warn("SMS skipped: no phone number");
            return;
        }
        if (twilioEnabled) {
            try {
                Message.creator(
                        new PhoneNumber(to),
                        messagingServiceSid,
                        body
                ).create();
                log.info("SMS sent to {}", sanitize(to));
            } catch (Exception e) {
                log.error("SMS failed to {}: {}", sanitize(to), e.getMessage());
                // Fallback log so demo never fully breaks
                log.info(">>> SMS fallback [{}]: {}", sanitize(to), body);
            }
        } else {
            log.info("SMS [{}]: {}", sanitize(to), body);
        }
    }

    private String sanitize(String input) {
        return input != null ? input.replaceAll("[\\r\\n]", "") : "";
    }
}