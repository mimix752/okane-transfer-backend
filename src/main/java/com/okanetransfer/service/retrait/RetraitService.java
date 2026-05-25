package com.okanetransfer.service.retrait;

import com.okanetransfer.dto.request.RetraitRequestDTO;
import com.okanetransfer.dto.response.RetraitResponseDTO;
import com.okanetransfer.entity.Transfer;
import com.okanetransfer.enums.TransferStatus;
import com.okanetransfer.exception.TransferNotFoundException;
import com.okanetransfer.repository.TransferRepository;
import com.okanetransfer.service.AgencyService;
import com.okanetransfer.service.AgentAuditService;
import com.okanetransfer.service.ReceiptPrintingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class RetraitService {

    @Autowired private TransferRepository transferRepository;
    @Autowired private VerificationRetraitService verificationService;
    @Autowired private AgencyService agencyService;
    @Autowired private AgentAuditService agentAuditService;
    @Autowired private ReceiptPrintingService receiptPrintingService;

    @Transactional
    public RetraitResponseDTO retirer(RetraitRequestDTO dto) {
        Transfer t = transferRepository.findByCode(dto.getTransferCode())
                .orElseThrow(() -> new TransferNotFoundException("Transfert introuvable: " + dto.getTransferCode()));

        verificationService.verifier(t, dto.getRecipientPhone());

        // Restaurer le solde à l'agence de destination lors du retrait
        agencyService.addBalance(t.getAgency().getId(), t.getAmount());

        t.setStatus(TransferStatus.PAID);
        
        // Enregistrer dans l'audit trail
        agentAuditService.logTransferWithdrawal(
            t.getId(),
            t.getTransferCode(),
            dto.getRecipientPhone()
        );
        
        // Imprimer le reçu de retrait
        String receiptContent = receiptPrintingService.generateWithdrawalReceipt(t);
        receiptPrintingService.printReceipt(receiptContent);
        return toDTO(t);
    }

    public void printDailyReport(String agencyName, java.time.LocalDate date, 
                               int totalTransfers, BigDecimal totalAmount,
                               BigDecimal totalFees, BigDecimal cashBalance) {
        String reportContent = receiptPrintingService.generateDailyReport(
            agencyName, date, totalTransfers, totalAmount, totalFees, cashBalance
        );
        receiptPrintingService.printReceipt(reportContent);
    }

    private RetraitResponseDTO toDTO(Transfer t) {
        RetraitResponseDTO dto = new RetraitResponseDTO();
        dto.setId(t.getId());
        dto.setTransferCode(t.getTransferCode());
        dto.setRecipientName(t.getRecipientName());
        dto.setPaidAmount(t.getConvertedAmount());
        dto.setStatus(t.getStatus());
        dto.setPaidAt(LocalDateTime.now());
        return dto;
    }
}
