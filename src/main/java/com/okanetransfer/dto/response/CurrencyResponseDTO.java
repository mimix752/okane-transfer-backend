package com.okanetransfer.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyResponseDTO {

    private Long id;
    private String code;
    private String name;
    private String symbol;
    private BigDecimal exchangeRate;
    private boolean active;
    private LocalDateTime lastUpdated;
    private LocalDateTime createdAt;

    public static CurrencyResponseDTO fromEntity(
            com.okanetransfer.entity.Currency currency) {

        return CurrencyResponseDTO.builder()
                .id(currency.getId())
                .code(currency.getCode())
                .name(currency.getName())
                .symbol(currency.getSymbol())
                .exchangeRate(currency.getExchangeRate())
                .active(currency.isActive())
                .lastUpdated(currency.getLastUpdated())
                .createdAt(currency.getCreatedAt())
                .build();
    }
}