package com.okanetransfer.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgencyResponseDTO {

    private Long id;
    private String name;
    private String address;
    private String country;
    private int agentCount;
    private BigDecimal dailyLimit;
    private boolean active;
}