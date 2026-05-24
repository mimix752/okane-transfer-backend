package com.okanetransfer.dto.response;

import com.okanetransfer.entity.FeeGrid;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FeeGridResponseDTO {

    private Long       id;
    private Long       corridorId;
    private String     corridorLabel;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal fixedFee;
    private BigDecimal percentageFee;
    private BigDecimal agencyShare;
    private BigDecimal centralShare;
    private boolean    active;
    private LocalDateTime createdAt;
    private BigDecimal simulatedFeeForMaxAmount;

    public FeeGridResponseDTO() {}

    public static FeeGridResponseDTO fromEntity(FeeGrid feeGrid) {
        String label = feeGrid.getCorridor().getSourceCountry()
                + " → "
                + feeGrid.getCorridor().getDestinationCountry();

        FeeGridResponseDTO d = new FeeGridResponseDTO();
        d.id            = feeGrid.getId();
        d.corridorId    = feeGrid.getCorridor().getId();
        d.corridorLabel = label;
        d.minAmount     = feeGrid.getMinAmount();
        d.maxAmount     = feeGrid.getMaxAmount();
        d.fixedFee      = feeGrid.getFixedFee();
        d.percentageFee = feeGrid.getPercentageFee();
        d.agencyShare   = feeGrid.getAgencyShare();
        d.centralShare  = feeGrid.getCentralShare();
        d.active        = feeGrid.isActive();
        d.createdAt     = feeGrid.getCreatedAt();
        return d;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long       id;
        private Long       corridorId;
        private String     corridorLabel;
        private BigDecimal minAmount;
        private BigDecimal maxAmount;
        private BigDecimal fixedFee;
        private BigDecimal percentageFee;
        private BigDecimal agencyShare;
        private BigDecimal centralShare;
        private boolean    active;
        private LocalDateTime createdAt;
        private BigDecimal simulatedFeeForMaxAmount;

        public Builder id(Long id)
        { this.id = id; return this; }
        public Builder corridorId(Long corridorId)
        { this.corridorId = corridorId; return this; }
        public Builder corridorLabel(String corridorLabel)
        { this.corridorLabel = corridorLabel; return this; }
        public Builder minAmount(BigDecimal minAmount)
        { this.minAmount = minAmount; return this; }
        public Builder maxAmount(BigDecimal maxAmount)
        { this.maxAmount = maxAmount; return this; }
        public Builder fixedFee(BigDecimal fixedFee)
        { this.fixedFee = fixedFee; return this; }
        public Builder percentageFee(BigDecimal percentageFee)
        { this.percentageFee = percentageFee; return this; }
        public Builder agencyShare(BigDecimal agencyShare)
        { this.agencyShare = agencyShare; return this; }
        public Builder centralShare(BigDecimal centralShare)
        { this.centralShare = centralShare; return this; }
        public Builder active(boolean active)
        { this.active = active; return this; }
        public Builder createdAt(LocalDateTime createdAt)
        { this.createdAt = createdAt; return this; }
        public Builder simulatedFeeForMaxAmount(BigDecimal v)
        { this.simulatedFeeForMaxAmount = v; return this; }

        public FeeGridResponseDTO build() {
            FeeGridResponseDTO d = new FeeGridResponseDTO();
            d.id                       = this.id;
            d.corridorId               = this.corridorId;
            d.corridorLabel            = this.corridorLabel;
            d.minAmount                = this.minAmount;
            d.maxAmount                = this.maxAmount;
            d.fixedFee                 = this.fixedFee;
            d.percentageFee            = this.percentageFee;
            d.agencyShare              = this.agencyShare;
            d.centralShare             = this.centralShare;
            d.active                   = this.active;
            d.createdAt                = this.createdAt;
            d.simulatedFeeForMaxAmount = this.simulatedFeeForMaxAmount;
            return d;
        }
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCorridorId() { return corridorId; }
    public void setCorridorId(Long corridorId)
    { this.corridorId = corridorId; }

    public String getCorridorLabel() { return corridorLabel; }
    public void setCorridorLabel(String corridorLabel)
    { this.corridorLabel = corridorLabel; }

    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount)
    { this.minAmount = minAmount; }

    public BigDecimal getMaxAmount() { return maxAmount; }
    public void setMaxAmount(BigDecimal maxAmount)
    { this.maxAmount = maxAmount; }

    public BigDecimal getFixedFee() { return fixedFee; }
    public void setFixedFee(BigDecimal fixedFee)
    { this.fixedFee = fixedFee; }

    public BigDecimal getPercentageFee() { return percentageFee; }
    public void setPercentageFee(BigDecimal percentageFee)
    { this.percentageFee = percentageFee; }

    public BigDecimal getAgencyShare() { return agencyShare; }
    public void setAgencyShare(BigDecimal agencyShare)
    { this.agencyShare = agencyShare; }

    public BigDecimal getCentralShare() { return centralShare; }
    public void setCentralShare(BigDecimal centralShare)
    { this.centralShare = centralShare; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)
    { this.createdAt = createdAt; }

    public BigDecimal getSimulatedFeeForMaxAmount()
    { return simulatedFeeForMaxAmount; }
    public void setSimulatedFeeForMaxAmount(BigDecimal v)
    { this.simulatedFeeForMaxAmount = v; }
}