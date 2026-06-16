package com.okanetransfer.service;

import com.okanetransfer.dto.request.MobileMoneyRequestDTO;
import com.okanetransfer.dto.response.MobileMoneyResponseDTO;
import com.okanetransfer.entity.MobileMoneyTransfer;
import com.okanetransfer.entity.Transfer;
import com.okanetransfer.enums.MobileMoneyOperator;
import com.okanetransfer.enums.TransferStatus;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.MobileMoneyTransferRepository;
import com.okanetransfer.repository.TransferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MobileMoneyService {

    @Autowired private MobileMoneyTransferRepository mobileMoneyRepository;
    @Autowired private TransferRepository transferRepository;
    @Autowired private NotificationService notificationService;

    // ─── Envoi vers compte Mobile Money ──────────────────────────────────────────

    @Transactional
    public MobileMoneyResponseDTO send(MobileMoneyRequestDTO dto) {
        Transfer transfer = transferRepository.findById(dto.getTransferId())
                .orElseThrow(() -> new ResourceNotFoundException("Transfer not found"));

        if (transfer.getStatus() != TransferStatus.PENDING)
            throw new IllegalStateException("Transfer must be PENDING to send via mobile money");

        // Simulation envoi opérateur
        String operatorRef = simulateOperatorSend(dto.getOperator(), dto.getMobileAccount(), transfer.getConvertedAmount());

        MobileMoneyTransfer mm = new MobileMoneyTransfer();
        mm.setTransfer(transfer);
        mm.setOperator(dto.getOperator());
        mm.setMobileAccount(dto.getMobileAccount());
        mm.setOperatorRef(operatorRef);
        mm.setReconciliationStatus(TransferStatus.MOBILE_SENT);
        mm.setSentAt(LocalDateTime.now());

        transfer.setStatus(TransferStatus.MOBILE_SENT);
        transferRepository.save(transfer);

        MobileMoneyTransfer saved = mobileMoneyRepository.save(mm);

        // Notification SMS simulée
        notificationService.sendMobileMoneyNotification(
                dto.getMobileAccount(),
                dto.getOperator().name(),
                transfer.getConvertedAmount(),
                transfer.getTransferCode()
        );

        return MobileMoneyResponseDTO.fromEntity(saved);
    }

    // ─── Réconciliation automatique ───────────────────────────────────────────────

    @Transactional
    public MobileMoneyResponseDTO reconcile(Long mobileMoneyId) {
        MobileMoneyTransfer mm = mobileMoneyRepository.findById(mobileMoneyId)
                .orElseThrow(() -> new ResourceNotFoundException("Mobile money transfer not found"));

        if (mm.getReconciliationStatus() == TransferStatus.RECONCILED)
            throw new IllegalStateException("Already reconciled");

        // Simulation réconciliation avec relevé opérateur
        BigDecimal operatorAmount = simulateOperatorReconciliation(mm.getOperatorRef());

        mm.setOperatorAmount(operatorAmount);
        mm.setReconciledAt(LocalDateTime.now());
        mm.setReconciliationStatus(TransferStatus.RECONCILED);

        mm.getTransfer().setStatus(TransferStatus.PAID);
        transferRepository.save(mm.getTransfer());

        return MobileMoneyResponseDTO.fromEntity(mobileMoneyRepository.save(mm));
    }

    // ─── Dashboard ────────────────────────────────────────────────────────────────

    public List<MobileMoneyResponseDTO> getAll() {
        return mobileMoneyRepository.findAll().stream()
                .map(MobileMoneyResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<MobileMoneyResponseDTO> getByOperator(MobileMoneyOperator operator) {
        return mobileMoneyRepository.findByOperator(operator).stream()
                .map(MobileMoneyResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<MobileMoneyResponseDTO> getPending() {
        return mobileMoneyRepository.findByReconciliationStatus(TransferStatus.MOBILE_SENT).stream()
                .map(MobileMoneyResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<MobileMoneyResponseDTO> getByAgent(Long agentId) {
        return mobileMoneyRepository.findByTransfer_Sender_Id(agentId).stream()
                .map(MobileMoneyResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ─── Simulation opérateur ─────────────────────────────────────────────────────

    private String simulateOperatorSend(MobileMoneyOperator operator, String account, BigDecimal amount) {
        String ref = operator.name().substring(0, 2) + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String safeAccount = account.replaceAll("[\\r\\n]", "");
        System.out.printf("[SIMULATION %s] Envoi %.2f vers %s -> ref: %s%n", operator, amount, safeAccount, ref);
        return ref;
    }

    private BigDecimal simulateOperatorReconciliation(String operatorRef) {
        String safeRef = operatorRef != null ? operatorRef.replaceAll("[\\r\\n]", "") : "null";
        System.out.printf("[SIMULATION RECONCILIATION] ref: %s -> confirme%n", safeRef);
        return null;
    }
}
