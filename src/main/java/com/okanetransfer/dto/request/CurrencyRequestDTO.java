package com.okanetransfer.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyRequestDTO {

    @NotBlank(message = "Code is required")
    @Size(min = 3, max = 3, message = "Code must be exactly 3 characters")
    @Pattern(
            regexp = "^[A-Z]{3}$",
            message = "Code must be 3 uppercase letters"
    )
    private String code;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Symbol is required")
    @Size(max = 10, message = "Symbol must not exceed 10 characters")
    private String symbol;

    @NotNull(message = "Exchange rate is required")
    @Positive(message = "Exchange rate must be positive")
    @Digits(
            integer = 10,
            fraction = 6,
            message = "Exchange rate: max 10 digits, 6 decimals"
    )
    private BigDecimal exchangeRate;

    private Boolean active;
}