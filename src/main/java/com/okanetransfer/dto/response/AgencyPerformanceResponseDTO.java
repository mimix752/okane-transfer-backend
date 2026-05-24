package com.okanetransfer.dto.response;

import java.math.BigDecimal;

public class AgencyPerformanceResponseDTO {

    private Long agencyId;
    private String name;
    private BigDecimal dailyVolume;
    private BigDecimal monthlyVolume;
    private int operationCount;
    private BigDecimal dailyRevenue;
    private BigDecimal monthlyRevenue;


    public AgencyPerformanceResponseDTO() {
    }

    public AgencyPerformanceResponseDTO(Long agencyId, String name, BigDecimal dailyVolume, BigDecimal monthlyVolume, int operationCount, BigDecimal dailyRevenue, BigDecimal monthlyRevenue) {
        this.agencyId = agencyId;
        this.name = name;
        this.dailyVolume = dailyVolume;
        this.monthlyVolume = monthlyVolume;
        this.operationCount = operationCount;
        this.dailyRevenue = dailyRevenue;
        this.monthlyRevenue = monthlyRevenue;
    }

    public Long getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(Long agencyId) {
        this.agencyId = agencyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getDailyVolume() {
        return dailyVolume;
    }

    public void setDailyVolume(BigDecimal dailyVolume) {
        this.dailyVolume = dailyVolume;
    }

    public BigDecimal getMonthlyVolume() {
        return monthlyVolume;
    }

    public void setMonthlyVolume(BigDecimal monthlyVolume) {
        this.monthlyVolume = monthlyVolume;
    }

    public int getOperationCount() {
        return operationCount;
    }

    public void setOperationCount(int operationCount) {
        this.operationCount = operationCount;
    }

    public BigDecimal getDailyRevenue() {
        return dailyRevenue;
    }

    public void setDailyRevenue(BigDecimal dailyRevenue) {
        this.dailyRevenue = dailyRevenue;
    }

    public BigDecimal getMonthlyRevenue() {
        return monthlyRevenue;
    }

    public void setMonthlyRevenue(BigDecimal monthlyRevenue) {
        this.monthlyRevenue = monthlyRevenue;
    }
}