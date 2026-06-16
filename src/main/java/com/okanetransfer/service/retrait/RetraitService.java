package com.okanetransfer.service.retrait;

import com.okanetransfer.dto.request.RetraitRequestDTO;
import com.okanetransfer.dto.response.RetraitResponseDTO;
import com.okanetransfer.entity.Agent;
import com.okanetransfer.entity.Transfer;
import com.okanetransfer.enums.TransferStatus;
import com.okanetransfer.repository.AgentRepository;
import com.okanetransfer.service.AgencyService;
import com.okanetransfer.service.AgentAuditService;
import com.okanetransfer.service.ReceiptPrintingService;
import com.okanetransfer.service.caisse.CashRegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class RetraitService {
    @Autowired private AgentRepository agentRepository;
    @Autowired private VerificationRetraitService verificationService;
    @Autowired private AgencyService agencyService;
    @Autowired private AgentAuditService agentAuditService;
    @Autowired private ReceiptPrintingService receiptPrintingService;
    @Autowired private CashRegisterService cashRegisterService;

    @Transactional
    public RetraitResponseDTO retirer(RetraitRequestDTO dto, Long agentId) {
        Transfer t = verificationService.findByCodeOrPhone(dto.getTransferCode(), dto.getSenderPhone());
        verificationService.verifier(t, dto.getSenderPhone(), dto.getSenderCIN());

        Agent payingAgent = agentRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found"));

        if (!t.getAgency().isActive()) {
            throw new IllegalStateException("Agency is suspended, cannot process withdrawal");
        }

        // Deduct convertedAmount from the PAYING agency's balance (EUR side)
        agencyService.checkAndDeductBalance(payingAgent.getAgency().getId(), t.getConvertedAmount());

        // Debit the paying agent's caisse (also in EUR)
        try {
            cashRegisterService.debiter(agentId, t.getConvertedAmount(), "RETRAIT", t.getTransferCode());
        } catch (Exception e) {
            System.out.println("Caisse debit warning: " + e.getMessage());
        }

        t.setStatus(TransferStatus.PAID);

        agentAuditService.logTransferWithdrawal(t.getId(), t.getTransferCode(), dto.getSenderPhone());

        String receiptContent = receiptPrintingService.generateWithdrawalReceipt(t);
        receiptPrintingService.printReceipt(receiptContent);
        return toDTO(t);
    }
    public RetraitResponseDTO rechercher(String code, String telephone) {
        Transfer t = verificationService.findByCodeOrPhone(code, telephone);
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
