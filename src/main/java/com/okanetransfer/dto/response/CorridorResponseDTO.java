package com.okanetransfer.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorridorResponseDTO {

    private Long id;

    private String sourceCountry;
    private String destinationCountry;

    private CurrencyInfo sourceCurrency;
    private CurrencyInfo destinationCurrency;

    private boolean active;
    private LocalDateTime createdAt;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrencyInfo {
        private Long id;
        private String code;    // "MAD"
        private String symbol;  // "MAD"
        private String name;    // "Dirham Marocain"
    }

    public static CorridorResponseDTO fromEntity(
            com.okanetransfer.entity.Corridor corridor) {

        return CorridorResponseDTO.builder()
                .id(corridor.getId())
                .sourceCountry(corridor.getSourceCountry())
                .destinationCountry(corridor.getDestinationCountry())
                .sourceCurrency(
                        CurrencyInfo.builder()
                                .id(corridor.getSourceCurrency().getId())
                                .code(corridor.getSourceCurrency().getCode())
                                .symbol(corridor.getSourceCurrency().getSymbol())
                                .name(corridor.getSourceCurrency().getName())
                                .build()
                )
                .destinationCurrency(
                        CurrencyInfo.builder()
                                .id(corridor.getDestinationCurrency().getId())
                                .code(corridor.getDestinationCurrency().getCode())
                                .symbol(corridor.getDestinationCurrency().getSymbol())
                                .name(corridor.getDestinationCurrency().getName())
                                .build()
                )
                .active(corridor.isActive())
                .createdAt(corridor.getCreatedAt())
                .build();
    }
}