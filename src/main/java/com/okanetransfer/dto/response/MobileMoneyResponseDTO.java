package com.okanetransfer.dto.response;

import com.okanetransfer.entity.MobileMoneyTransfer;
import com.okanetransfer.enums.MobileMoneyOperator;
import com.okanetransfer.enums.TransferStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MobileMoneyResponseDTO {

    private Long id;
    private Long transferId;
    private String transferCode;
    private MobileMoneyOperator operator;
    private String mobileAccount;
    private String operatorRef;
    private BigDecimal amount;
    private TransferStatus reconciliationStatus;
    private LocalDateTime sentAt;
    private LocalDateTime reconciledAt;
    private BigDecimal operatorAmount;

    public static MobileMoneyResponseDTO fromEntity(MobileMoneyTransfer m) {
        MobileMoneyResponseDTO dto = new MobileMoneyResponseDTO();
        dto.id = m.getId();
        dto.transferId = m.getTransfer().getId();
        dto.transferCode = m.getTransfer().getTransferCode();
        dto.operator = m.getOperator();
        dto.mobileAccount = m.getMobileAccount();
        dto.operatorRef = m.getOperatorRef();
        dto.amount = m.getTransfer().getConvertedAmount();
        dto.reconciliationStatus = m.getReconciliationStatus();
        dto.sentAt = m.getSentAt();
        dto.reconciledAt = m.getReconciledAt();
        dto.operatorAmount = m.getOperatorAmount();
        return dto;
    }

    public Long getId() { return id; }
    public Long getTransferId() { return transferId; }
    public String getTransferCode() { return transferCode; }
    public MobileMoneyOperator getOperator() { return operator; }
    public String getMobileAccount() { return mobileAccount; }
    public String getOperatorRef() { return operatorRef; }
    public BigDecimal getAmount() { return amount; }
    public TransferStatus getReconciliationStatus() { return reconciliationStatus; }
    public LocalDateTime getSentAt() { return sentAt; }
    public LocalDateTime getReconciledAt() { return reconciledAt; }
    public BigDecimal getOperatorAmount() { return operatorAmount; }
}
