package com.okanetransfer.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AgencyPerformanceResponseDTO {

    private Long       agencyId;
    private String     name;          // ton champ
    private String     agencyName;    // champ camarade (alias)
    private String     country;

    // Volume journalier
    private BigDecimal dailyVolume;
    private int        dailyTransferCount;
    private BigDecimal dailyRevenue;

    // Volume mensuel
    private BigDecimal monthlyVolume;
    private int        monthlyTransferCount;
    private BigDecimal monthlyRevenue;

    // ton champ
    private int        operationCount;

    // Caisse
    private BigDecimal currentBalance;
    private BigDecimal dailyLimit;
    private BigDecimal dailyLimitUsedPercent;

    // Taux de succès
    private BigDecimal successRate;

    private LocalDate  reportDate;

    public AgencyPerformanceResponseDTO() {}

    public AgencyPerformanceResponseDTO(Long agencyId, String name,
                                        BigDecimal dailyVolume,
                                        BigDecimal monthlyVolume,
                                        int operationCount,
                                        BigDecimal dailyRevenue,
                                        BigDecimal monthlyRevenue) {
        this.agencyId        = agencyId;
        this.name            = name;
        this.agencyName      = name;
        this.dailyVolume     = dailyVolume;
        this.monthlyVolume   = monthlyVolume;
        this.operationCount  = operationCount;
        this.dailyRevenue    = dailyRevenue;
        this.monthlyRevenue  = monthlyRevenue;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long       agencyId;
        private String     name;
        private String     agencyName;
        private String     country;
        private BigDecimal dailyVolume           = BigDecimal.ZERO;
        private int        dailyTransferCount;
        private BigDecimal dailyRevenue          = BigDecimal.ZERO;
        private BigDecimal monthlyVolume         = BigDecimal.ZERO;
        private int        monthlyTransferCount;
        private BigDecimal monthlyRevenue        = BigDecimal.ZERO;
        private int        operationCount;
        private BigDecimal currentBalance        = BigDecimal.ZERO;
        private BigDecimal dailyLimit            = BigDecimal.ZERO;
        private BigDecimal dailyLimitUsedPercent = BigDecimal.ZERO;
        private BigDecimal successRate           = BigDecimal.ZERO;
        private LocalDate  reportDate            = LocalDate.now();

        public Builder agencyId(Long v)
        { this.agencyId = v; return this; }
        public Builder name(String v)
        { this.name = v; this.agencyName = v; return this; }
        public Builder agencyName(String v)
        { this.agencyName = v; this.name = v; return this; }
        public Builder country(String v)
        { this.country = v; return this; }
        public Builder dailyVolume(BigDecimal v)
        { this.dailyVolume = v; return this; }
        public Builder dailyTransferCount(int v)
        { this.dailyTransferCount = v; return this; }
        public Builder dailyRevenue(BigDecimal v)
        { this.dailyRevenue = v; return this; }
        public Builder monthlyVolume(BigDecimal v)
        { this.monthlyVolume = v; return this; }
        public Builder monthlyTransferCount(int v)
        { this.monthlyTransferCount = v; return this; }
        public Builder monthlyRevenue(BigDecimal v)
        { this.monthlyRevenue = v; return this; }
        public Builder operationCount(int v)
        { this.operationCount = v; return this; }
        public Builder currentBalance(BigDecimal v)
        { this.currentBalance = v; return this; }
        public Builder dailyLimit(BigDecimal v)
        { this.dailyLimit = v; return this; }
        public Builder dailyLimitUsedPercent(BigDecimal v)
        { this.dailyLimitUsedPercent = v; return this; }
        public Builder successRate(BigDecimal v)
        { this.successRate = v; return this; }
        public Builder reportDate(LocalDate v)
        { this.reportDate = v; return this; }

        public AgencyPerformanceResponseDTO build() {
            AgencyPerformanceResponseDTO d =
                    new AgencyPerformanceResponseDTO();
            d.agencyId              = agencyId;
            d.name                  = name;
            d.agencyName            = agencyName;
            d.country               = country;
            d.dailyVolume           = dailyVolume;
            d.dailyTransferCount    = dailyTransferCount;
            d.dailyRevenue          = dailyRevenue;
            d.monthlyVolume         = monthlyVolume;
            d.monthlyTransferCount  = monthlyTransferCount;
            d.monthlyRevenue        = monthlyRevenue;
            d.operationCount        = operationCount;
            d.currentBalance        = currentBalance;
            d.dailyLimit            = dailyLimit;
            d.dailyLimitUsedPercent = dailyLimitUsedPercent;
            d.successRate           = successRate;
            d.reportDate            = reportDate;
            return d;
        }
    }

    // ─── Getters / Setters ───────────────────────────────────
    public Long getAgencyId() { return agencyId; }
    public void setAgencyId(Long v) { this.agencyId = v; }

    public String getName() { return name; }
    public void setName(String v) { this.name = v; this.agencyName = v; }

    public String getAgencyName() { return agencyName; }
    public void setAgencyName(String v) { this.agencyName = v; this.name = v; }

    public String getCountry() { return country; }
    public void setCountry(String v) { this.country = v; }

    public BigDecimal getDailyVolume() { return dailyVolume; }
    public void setDailyVolume(BigDecimal v) { this.dailyVolume = v; }

    public int getDailyTransferCount() { return dailyTransferCount; }
    public void setDailyTransferCount(int v) { this.dailyTransferCount = v; }

    public BigDecimal getDailyRevenue() { return dailyRevenue; }
    public void setDailyRevenue(BigDecimal v) { this.dailyRevenue = v; }

    public BigDecimal getMonthlyVolume() { return monthlyVolume; }
    public void setMonthlyVolume(BigDecimal v) { this.monthlyVolume = v; }

    public int getMonthlyTransferCount() { return monthlyTransferCount; }
    public void setMonthlyTransferCount(int v) { this.monthlyTransferCount = v; }

    public BigDecimal getMonthlyRevenue() { return monthlyRevenue; }
    public void setMonthlyRevenue(BigDecimal v) { this.monthlyRevenue = v; }

    public int getOperationCount() { return operationCount; }
    public void setOperationCount(int v) { this.operationCount = v; }

    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal v) { this.currentBalance = v; }

    public BigDecimal getDailyLimit() { return dailyLimit; }
    public void setDailyLimit(BigDecimal v) { this.dailyLimit = v; }

    public BigDecimal getDailyLimitUsedPercent() { return dailyLimitUsedPercent; }
    public void setDailyLimitUsedPercent(BigDecimal v) { this.dailyLimitUsedPercent = v; }

    public BigDecimal getSuccessRate() { return successRate; }
    public void setSuccessRate(BigDecimal v) { this.successRate = v; }

    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate v) { this.reportDate = v; }
}