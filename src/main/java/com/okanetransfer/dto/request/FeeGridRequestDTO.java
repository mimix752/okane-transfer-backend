package com.okanetransfer.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeGridRequestDTO {

    @NotNull(message = "Corridor ID is required")
    @Positive(message = "Corridor ID must be positive")
    private Long corridorId;

    @NotNull(message = "Minimum amount is required")
    @PositiveOrZero(message = "Minimum amount must be 0 or positive")
    @Digits(
            integer = 13,
            fraction = 2,
            message = "Min amount: max 13 digits, 2 decimals"
    )
    private BigDecimal minAmount;

    @NotNull(message = "Maximum amount is required")
    @Positive(message = "Maximum amount must be positive")
    @Digits(
            integer = 13,
            fraction = 2,
            message = "Max amount: max 13 digits, 2 decimals"
    )
    private BigDecimal maxAmount;

    @NotNull(message = "Fixed fee is required")
    @PositiveOrZero(message = "Fixed fee must be 0 or positive")
    @Digits(
            integer = 8,
            fraction = 2,
            message = "Fixed fee: max 8 digits, 2 decimals"
    )
    private BigDecimal fixedFee;

    @NotNull(message = "Percentage fee is required")
    @PositiveOrZero(message = "Percentage fee must be 0 or positive")
    @DecimalMax(
            value = "100.0",
            message = "Percentage fee cannot exceed 100%"
    )
    @Digits(
            integer = 3,
            fraction = 2,
            message = "Percentage fee: max 3 digits, 2 decimals"
    )
    private BigDecimal percentageFee;

    @NotNull(message = "Agency share is required")
    @PositiveOrZero(message = "Agency share must be 0 or positive")
    @DecimalMax(
            value = "100.0",
            message = "Agency share cannot exceed 100%"
    )
    @Digits(
            integer = 3,
            fraction = 2,
            message = "Agency share: max 3 digits, 2 decimals"
    )
    private BigDecimal agencyShare;

    @NotNull(message = "Central share is required")
    @PositiveOrZero(message = "Central share must be 0 or positive")
    @DecimalMax(
            value = "100.0",
            message = "Central share cannot exceed 100%"
    )
    @Digits(
            integer = 3,
            fraction = 2,
            message = "Central share: max 3 digits, 2 decimals"
    )
    private BigDecimal centralShare;

    private Boolean active;
}