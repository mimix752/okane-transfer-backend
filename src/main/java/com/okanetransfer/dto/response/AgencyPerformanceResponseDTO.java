package com.okanetransfer.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgencyPerformanceResponseDTO {

    private Long agencyId;
    private String name;
    private BigDecimal dailyVolume;
    private BigDecimal monthlyVolume;
    private int operationCount;
    private BigDecimal dailyRevenue;
    private BigDecimal monthlyRevenue;
}