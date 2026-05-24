package com.okanetransfer.service;

import com.okanetransfer.entity.Transfer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class NotificationService {

    public void sendReceiptBySMS(Transfer transfer, BigDecimal fees) {
        String message = String.format(
                "Okane Transfer - Code retrait: %s | Montant: %.2f | Frais: %.2f",
                transfer.getTransferCode(),
                transfer.getAmount(),
                fees
        );
        // TODO: Intégrer API SMS (Twilio, AWS SNS, etc.)
    }

    public void sendReceiptByEmail(Transfer transfer, BigDecimal fees, String recipientEmail) {
        String subject = "Reçu de transfert d'argent - Code: " + transfer.getTransferCode();
        String body = String.format(
                "Bonjour,\n\n" +
                        "Votre transfert a été enregistré avec succès.\n\n" +
                        "Code de retrait: %s\n" +
                        "Montant: %.2f %s\n" +
                        "Frais: %.2f %s\n" +
                        "Montant net: %.2f %s\n\n" +
                        "Cordialement,\nOkane Transfer",
                transfer.getTransferCode(),
                transfer.getAmount(),
                transfer.getCurrency(),
                fees,
                transfer.getCurrency(),
                transfer.getAmount().subtract(fees),
                transfer.getCurrency()
        );
        // TODO: Intégrer API Email (JavaMail, SendGrid, etc.)
    }

    public void printReceipt(String receipt) {
        // TODO: Intégrer imprimante thermique
    }
}