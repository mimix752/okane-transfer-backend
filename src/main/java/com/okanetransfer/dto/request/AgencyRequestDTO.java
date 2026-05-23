package com.okanetransfer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgencyRequestDTO {

    @NotBlank
    private String name;

    @NotBlank
    private String address;

    @NotBlank
    private String country;

    private String destinationCountry;

    @NotNull
    @Positive
    private BigDecimal dailyLimit;
}