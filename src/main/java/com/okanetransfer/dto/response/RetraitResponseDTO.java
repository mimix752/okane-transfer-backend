package com.okanetransfer.dto.response;

import com.okanetransfer.enums.TransferStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RetraitResponseDTO {
    private Long id;
    private String transferCode;
    private String recipientName;
    private BigDecimal paidAmount;
    private TransferStatus status;
    private LocalDateTime paidAt;

    public RetraitResponseDTO() {
    }

    public RetraitResponseDTO(Long id, String transferCode, String recipientName, BigDecimal paidAmount, TransferStatus status, LocalDateTime paidAt) {
        this.id = id;
        this.transferCode = transferCode;
        this.recipientName = recipientName;
        this.paidAmount = paidAmount;
        this.status = status;
        this.paidAt = paidAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransferCode() {
        return transferCode;
    }

    public void setTransferCode(String transferCode) {
        this.transferCode = transferCode;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public TransferStatus getStatus() {
        return status;
    }

    public void setStatus(TransferStatus status) {
        this.status = status;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}
