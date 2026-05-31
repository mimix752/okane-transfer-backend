package com.okanetransfer.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class EnvoiRequestDTO {

    @NotBlank(message = "Sender name is required")
    private String senderName;

    @NotBlank(message = "Sender first name is required")
    private String senderFirstName;

    @NotBlank(message = "Sender CIN/Passport is required")
    private String senderCIN;

    @NotBlank(message = "Sender phone is required")
    @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Invalid phone format")
    private String senderPhone;

    @NotBlank(message = "Sender country is required")
    @Size(min = 2, max = 3, message = "Country code must be 2-3 characters")
    private String senderCountry;

    @NotBlank(message = "Recipient name is required")
    private String recipientName;

    @NotBlank(message = "Recipient first name is required")
    private String recipientFirstName;

    @NotBlank(message = "Recipient phone is required")
    @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Invalid phone format")
    private String recipientPhone;

    private String recipientCIN;

    @NotBlank(message = "Recipient country is required")
    @Size(min = 2, max = 3, message = "Country code must be 2-3 characters")
    private String recipientCountry;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @DecimalMax(value = "999999.99", message = "Amount exceeds maximum limit")
    private BigDecimal amount;

    @NotNull(message = "Currency ID is required")
    @Positive(message = "Currency ID must be positive")
    private Long currencyId;

    @NotNull(message = "Corridor ID is required")
    @Positive(message = "Corridor ID must be positive")
    private Long corridorId;

    public EnvoiRequestDTO() {}

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getSenderFirstName() { return senderFirstName; }
    public void setSenderFirstName(String senderFirstName) { this.senderFirstName = senderFirstName; }
    public String getSenderCIN() { return senderCIN; }
    public void setSenderCIN(String senderCIN) { this.senderCIN = senderCIN; }
    public String getSenderPhone() { return senderPhone; }
    public void setSenderPhone(String senderPhone) { this.senderPhone = senderPhone; }
    public String getSenderCountry() { return senderCountry; }
    public void setSenderCountry(String senderCountry) { this.senderCountry = senderCountry; }
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    public String getRecipientFirstName() { return recipientFirstName; }
    public void setRecipientFirstName(String recipientFirstName) { this.recipientFirstName = recipientFirstName; }
    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }
    public String getRecipientCIN() { return recipientCIN; }
    public void setRecipientCIN(String recipientCIN) { this.recipientCIN = recipientCIN; }
    public String getRecipientCountry() { return recipientCountry; }
    public void setRecipientCountry(String recipientCountry) { this.recipientCountry = recipientCountry; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Long getCurrencyId() { return currencyId; }
    public void setCurrencyId(Long currencyId) { this.currencyId = currencyId; }
    public Long getCorridorId() { return corridorId; }
    public void setCorridorId(Long corridorId) { this.corridorId = corridorId; }
}
