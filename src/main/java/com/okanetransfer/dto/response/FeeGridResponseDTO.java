package com.okanetransfer.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeGridResponseDTO {

    private Long id;

    private Long corridorId;
    private String corridorLabel;


    private BigDecimal minAmount;
    private BigDecimal maxAmount;


    private BigDecimal fixedFee;
    private BigDecimal percentageFee;


    private BigDecimal agencyShare;
    private BigDecimal centralShare;

    private boolean active;
    private LocalDateTime createdAt;

    private BigDecimal simulatedFeeForMaxAmount;

    public static FeeGridResponseDTO fromEntity(
            com.okanetransfer.entity.FeeGrid feeGrid) {

        String label = feeGrid.getCorridor().getSourceCountry()
                + " → "
                + feeGrid.getCorridor().getDestinationCountry();

        return FeeGridResponseDTO.builder()
                .id(feeGrid.getId())
                .corridorId(feeGrid.getCorridor().getId())
                .corridorLabel(label)
                .minAmount(feeGrid.getMinAmount())
                .maxAmount(feeGrid.getMaxAmount())
                .fixedFee(feeGrid.getFixedFee())
                .percentageFee(feeGrid.getPercentageFee())
                .agencyShare(feeGrid.getAgencyShare())
                .centralShare(feeGrid.getCentralShare())
                .active(feeGrid.isActive())
                .createdAt(feeGrid.getCreatedAt())
                .build();
    }
}