package com.okanetransfer.dto.response;

import java.math.BigDecimal;

public class CorridorStatsResponseDTO {

    private Long       corridorId;
    private String     label;
    private String     sourceCountry;
    private String     destinationCountry;


    private BigDecimal dailyVolume;
    private BigDecimal monthlyVolume;
    private int        dailyCount;
    private int        monthlyCount;


    private BigDecimal dailyRevenue;
    private BigDecimal monthlyRevenue;

    private BigDecimal agencyCommission;
    private BigDecimal centralCommission;

    public CorridorStatsResponseDTO() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long       corridorId;
        private String     label;
        private String     sourceCountry;
        private String     destinationCountry;
        private BigDecimal dailyVolume    = BigDecimal.ZERO;
        private BigDecimal monthlyVolume  = BigDecimal.ZERO;
        private int        dailyCount;
        private int        monthlyCount;
        private BigDecimal dailyRevenue   = BigDecimal.ZERO;
        private BigDecimal monthlyRevenue = BigDecimal.ZERO;
        private BigDecimal agencyCommission  = BigDecimal.ZERO;
        private BigDecimal centralCommission = BigDecimal.ZERO;

        public Builder corridorId(Long v)
        { this.corridorId = v; return this; }
        public Builder label(String v)
        { this.label = v; return this; }
        public Builder sourceCountry(String v)
        { this.sourceCountry = v; return this; }
        public Builder destinationCountry(String v)
        { this.destinationCountry = v; return this; }
        public Builder dailyVolume(BigDecimal v)
        { this.dailyVolume = v; return this; }
        public Builder monthlyVolume(BigDecimal v)
        { this.monthlyVolume = v; return this; }
        public Builder dailyCount(int v)
        { this.dailyCount = v; return this; }
        public Builder monthlyCount(int v)
        { this.monthlyCount = v; return this; }
        public Builder dailyRevenue(BigDecimal v)
        { this.dailyRevenue = v; return this; }
        public Builder monthlyRevenue(BigDecimal v)
        { this.monthlyRevenue = v; return this; }
        public Builder agencyCommission(BigDecimal v)
        { this.agencyCommission = v; return this; }
        public Builder centralCommission(BigDecimal v)
        { this.centralCommission = v; return this; }

        public CorridorStatsResponseDTO build() {
            CorridorStatsResponseDTO d =
                    new CorridorStatsResponseDTO();
            d.corridorId         = corridorId;
            d.label              = label;
            d.sourceCountry      = sourceCountry;
            d.destinationCountry = destinationCountry;
            d.dailyVolume        = dailyVolume;
            d.monthlyVolume      = monthlyVolume;
            d.dailyCount         = dailyCount;
            d.monthlyCount       = monthlyCount;
            d.dailyRevenue       = dailyRevenue;
            d.monthlyRevenue     = monthlyRevenue;
            d.agencyCommission   = agencyCommission;
            d.centralCommission  = centralCommission;
            return d;
        }
    }

    public Long getCorridorId() { return corridorId; }
    public void setCorridorId(Long v) { this.corridorId = v; }
    public String getLabel() { return label; }
    public void setLabel(String v) { this.label = v; }
    public String getSourceCountry() { return sourceCountry; }
    public void setSourceCountry(String v)
    { this.sourceCountry = v; }
    public String getDestinationCountry()
    { return destinationCountry; }
    public void setDestinationCountry(String v)
    { this.destinationCountry = v; }
    public BigDecimal getDailyVolume() { return dailyVolume; }
    public void setDailyVolume(BigDecimal v)
    { this.dailyVolume = v; }
    public BigDecimal getMonthlyVolume() { return monthlyVolume; }
    public void setMonthlyVolume(BigDecimal v)
    { this.monthlyVolume = v; }
    public int getDailyCount() { return dailyCount; }
    public void setDailyCount(int v) { this.dailyCount = v; }
    public int getMonthlyCount() { return monthlyCount; }
    public void setMonthlyCount(int v) { this.monthlyCount = v; }
    public BigDecimal getDailyRevenue() { return dailyRevenue; }
    public void setDailyRevenue(BigDecimal v)
    { this.dailyRevenue = v; }
    public BigDecimal getMonthlyRevenue() { return monthlyRevenue; }
    public void setMonthlyRevenue(BigDecimal v)
    { this.monthlyRevenue = v; }
    public BigDecimal getAgencyCommission()
    { return agencyCommission; }
    public void setAgencyCommission(BigDecimal v)
    { this.agencyCommission = v; }
    public BigDecimal getCentralCommission()
    { return centralCommission; }
    public void setCentralCommission(BigDecimal v)
    { this.centralCommission = v; }
}