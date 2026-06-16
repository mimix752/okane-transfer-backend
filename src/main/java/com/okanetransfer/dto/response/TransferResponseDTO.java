package com.okanetransfer.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.okanetransfer.entity.Transfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferResponseDTO {

    private Long id;
    private String transferCode;
    private String senderUsername;
    private String recipientName;
    private String recipientPhone;
    private BigDecimal amount;
    private String currency;
    private BigDecimal convertedAmount;
    private String targetCurrency;
    private String status;
    private String agencyName;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public TransferResponseDTO() {
    }

    public TransferResponseDTO(Long id, String transferCode, String senderUsername, String recipientName, String recipientPhone, BigDecimal amount, String currency, BigDecimal convertedAmount, String targetCurrency, String status, String agencyName, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.transferCode = transferCode;
        this.senderUsername = senderUsername;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.amount = amount;
        this.currency = currency;
        this.convertedAmount = convertedAmount;
        this.targetCurrency = targetCurrency;
        this.status = status;
        this.agencyName = agencyName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getConvertedAmount() {
        return convertedAmount;
    }

    public void setConvertedAmount(BigDecimal convertedAmount) {
        this.convertedAmount = convertedAmount;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(String targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAgencyName() {
        return agencyName;
    }

    public void setAgencyName(String agencyName) {
        this.agencyName = agencyName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static TransferResponseDTO fromEntity(Transfer transfer) {
        TransferResponseDTO dto = new TransferResponseDTO();
        dto.setId(transfer.getId());
        dto.setTransferCode(transfer.getTransferCode());
        dto.setSenderUsername(
                transfer.getSenderUser() != null
                        ? transfer.getSenderUser().getUsername()
                        : transfer.getSenderPhone()
        );
        dto.setRecipientName(transfer.getRecipientName());
        dto.setRecipientPhone(transfer.getRecipientPhone());
        dto.setAmount(transfer.getAmount());
        dto.setCurrency(transfer.getCurrency() != null ? transfer.getCurrency().getCode() : null);
        dto.setConvertedAmount(transfer.getConvertedAmount());
        dto.setTargetCurrency(transfer.getTargetCurrency() != null ? transfer.getTargetCurrency().getCode() : null);
        dto.setStatus(transfer.getStatus().toString());
        dto.setAgencyName(transfer.getAgency() != null ? transfer.getAgency().getName() : null);
        dto.setCreatedAt(transfer.getCreatedAt());
        dto.setUpdatedAt(transfer.getUpdatedAt());
        return dto;
    }
}