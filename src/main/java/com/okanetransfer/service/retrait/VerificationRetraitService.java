package com.okanetransfer.service.retrait;

import com.okanetransfer.entity.Transfer;
import com.okanetransfer.enums.TransferStatus;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.TransferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VerificationRetraitService {

    @Autowired private TransferRepository transferRepository;

    public Transfer findByCodeOrPhone(String transferCode, String recipientPhone) {
        if (transferCode != null && !transferCode.isBlank()) {
            return transferRepository.findByCode(transferCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Transfert introuvable: " + transferCode));
        }
        return transferRepository.findByRecipientPhone(recipientPhone)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun transfert en attente pour ce numéro"));
    }

    public void verifier(Transfer t, String recipientPhone, String recipientCIN) {
        if (t.getStatus() == TransferStatus.PAID || t.getStatus() == TransferStatus.CANCELLED) {
            throw new RuntimeException("Transfert déjà traité. Statut: " + t.getStatus());
        }
        if (t.getStatus() != TransferStatus.PENDING && t.getStatus() != TransferStatus.VALIDATED)
            throw new RuntimeException("Transfert non disponible au retrait. Statut: " + t.getStatus());
        if (!t.getRecipientPhone().equals(recipientPhone))
            throw new RuntimeException("Numéro de téléphone incorrect");
        if (recipientCIN == null || recipientCIN.isBlank())
            throw new RuntimeException("La pièce d'identité du bénéficiaire est obligatoire");
    }
}
