package com.okanetransfer.service.retrait;

import com.okanetransfer.entity.Transfer;
import com.okanetransfer.enums.TransferStatus;
import org.springframework.stereotype.Service;

@Service
public class VerificationRetraitService {

    public void verifier(Transfer t, String recipientPhone) {
        if (t.getStatus() != TransferStatus.PENDING && t.getStatus() != TransferStatus.VALIDATED)
            throw new RuntimeException("Transfert non disponible au retrait. Statut: " + t.getStatus());
        if (!t.getRecipientPhone().equals(recipientPhone))
            throw new RuntimeException("Numéro de téléphone incorrect");
    }
}
