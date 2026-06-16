package com.okanetransfer.dto.response;

import com.okanetransfer.entity.Transfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EnvoiResponseDTO {

    private Long id;
    private String transferCode;
    private String senderName;
    private String recipientName;
    private String recipientCountry;
    private BigDecimal amount;
    private String currency;
    private BigDecimal fees;
    private BigDecimal netAmount;       // what recipient receives (convertedAmount)
    private BigDecimal convertedAmount; // explicit field for frontend display
    private String targetCurrency;      // recipient's currency code
    private String status;
    private LocalDateTime createdAt;
    private String receipt;

    public EnvoiResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTransferCode() { return transferCode; }
    public void setTransferCode(String transferCode) { this.transferCode = transferCode; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getRecipientCountry() { return recipientCountry; }
    public void setRecipientCountry(String recipientCountry) { this.recipientCountry = recipientCountry; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getFees() { return fees; }
    public void setFees(BigDecimal fees) { this.fees = fees; }

    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }

    public BigDecimal getConvertedAmount() { return convertedAmount; }
    public void setConvertedAmount(BigDecimal convertedAmount) { this.convertedAmount = convertedAmount; }

    public String getTargetCurrency() { return targetCurrency; }
    public void setTargetCurrency(String targetCurrency) { this.targetCurrency = targetCurrency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getReceipt() { return receipt; }
    public void setReceipt(String receipt) { this.receipt = receipt; }

    public static EnvoiResponseDTO fromEntity(Transfer transfer, BigDecimal fees) {
        // netAmount = what the recipient actually receives, in target currency
        BigDecimal convertedAmount = transfer.getConvertedAmount();
        BigDecimal netAmount = convertedAmount != null
                ? convertedAmount
                : transfer.getAmount().subtract(fees);

        String targetCurrencyCode = transfer.getTargetCurrency() != null
                ? transfer.getTargetCurrency().getCode()
                : transfer.getCurrency().getCode();

        EnvoiResponseDTO dto = new EnvoiResponseDTO();
        dto.setId(transfer.getId());
        dto.setTransferCode(transfer.getTransferCode());
        dto.setSenderName(transfer.getSender().getUsername());
        dto.setRecipientName(transfer.getRecipientName());
        dto.setRecipientCountry(transfer.getRecipientCountry());
        dto.setAmount(transfer.getAmount());
        dto.setCurrency(transfer.getCurrency().getCode());
        dto.setFees(fees);
        dto.setNetAmount(netAmount);
        dto.setConvertedAmount(convertedAmount);
        dto.setTargetCurrency(targetCurrencyCode);
        dto.setStatus(transfer.getStatus().toString());
        dto.setCreatedAt(transfer.getCreatedAt());
        dto.setReceipt(generateReceipt(transfer, fees, netAmount));
        return dto;
    }

    private static String generateReceipt(Transfer transfer, BigDecimal fees, BigDecimal netAmount) {
        BigDecimal receivedAmount = transfer.getConvertedAmount() != null
                ? transfer.getConvertedAmount()
                : netAmount;
        String targetCurrencyCode = transfer.getTargetCurrency() != null
                ? transfer.getTargetCurrency().getCode()
                : transfer.getCurrency().getCode();

        return String.format(
                "         REÇU DE TRANSFERT D'ARGENT\n" +
                        "Code de retrait: %s\n" +
                        "Expéditeur: %s\n" +
                        "Bénéficiaire: %s\n" +
                        "Montant envoyé: %.2f %s\n" +
                        "Frais: %.2f %s\n" +
                        "Montant reçu par bénéficiaire: %.2f %s\n" +
                        "Date: %s\n" +
                        "Statut: %s\n",
                transfer.getTransferCode(),
                transfer.getSender().getUsername(),
                transfer.getRecipientName(),
                transfer.getAmount(),
                transfer.getCurrency().getCode(),
                fees,
                transfer.getCurrency().getCode(),
                receivedAmount,
                targetCurrencyCode,
                transfer.getCreatedAt(),
                transfer.getStatus()
        );
    }
}