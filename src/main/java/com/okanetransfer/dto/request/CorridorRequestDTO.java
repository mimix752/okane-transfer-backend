package com.okanetransfer.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorridorRequestDTO {

    @NotBlank(message = "Source country is required")
    @Size(
            min = 2, max = 3,
            message = "Source country code must be 2 or 3 characters"
    )
    @Pattern(
            regexp = "^[A-Z]{2,3}$",
            message = "Source country must be uppercase letters only"
    )
    private String sourceCountry;

    @NotBlank(message = "Destination country is required")
    @Size(
            min = 2, max = 3,
            message = "Destination country code must be 2 or 3 characters"
    )
    @Pattern(
            regexp = "^[A-Z]{2,3}$",
            message = "Destination country must be uppercase letters only"
    )
    private String destinationCountry;

    @NotNull(message = "Source currency ID is required")
    @Positive(message = "Source currency ID must be positive")
    private Long sourceCurrencyId;

    @NotNull(message = "Destination currency ID is required")
    @Positive(message = "Destination currency ID must be positive")
    private Long destinationCurrencyId;

    private Boolean active;
}