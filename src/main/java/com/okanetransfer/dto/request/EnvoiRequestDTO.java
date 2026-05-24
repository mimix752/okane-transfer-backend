package com.okanetransfer.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnvoiRequestDTO {

    // ─── Expéditeur ───
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

    // ─── Bénéficiaire ───
    @NotBlank(message = "Recipient name is required")
    private String recipientName;

    @NotBlank(message = "Recipient first name is required")
    private String recipientFirstName;

    @NotBlank(message = "Recipient phone is required")
    @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Invalid phone format")
    private String recipientPhone;

    @NotBlank(message = "Recipient country is required")
    @Size(min = 2, max = 3, message = "Country code must be 2-3 characters")
    private String recipientCountry;

    // Montant & Devise
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @DecimalMax(value = "999999.99", message = "Amount exceeds maximum limit")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    private String currency;

    // ─── Corridor ───
    @NotNull(message = "Corridor ID is required")
    @Positive(message = "Corridor ID must be positive")
    private Long corridorId;
}
