package com.okanetransfer.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class FeeGridRequestDTO {

    @NotNull(message = "Corridor ID is required")
    @Positive(message = "Corridor ID must be positive")
    private Long corridorId;

    @NotNull(message = "Minimum amount is required")
    @PositiveOrZero(message = "Minimum amount must be 0 or positive")
    @Digits(integer = 13, fraction = 2,
            message = "Min amount: max 13 digits, 2 decimals")
    private BigDecimal minAmount;

    @NotNull(message = "Maximum amount is required")
    @Positive(message = "Maximum amount must be positive")
    @Digits(integer = 13, fraction = 2,
            message = "Max amount: max 13 digits, 2 decimals")
    private BigDecimal maxAmount;

    @NotNull(message = "Fixed fee is required")
    @PositiveOrZero(message = "Fixed fee must be 0 or positive")
    @Digits(integer = 8, fraction = 2,
            message = "Fixed fee: max 8 digits, 2 decimals")
    private BigDecimal fixedFee;

    @NotNull(message = "Percentage fee is required")
    @PositiveOrZero(message = "Percentage fee must be 0 or positive")
    @DecimalMax(value = "100.0",
            message = "Percentage fee cannot exceed 100%")
    @Digits(integer = 3, fraction = 2,
            message = "Percentage fee: max 3 digits, 2 decimals")
    private BigDecimal percentageFee;

    @NotNull(message = "Agency share is required")
    @PositiveOrZero(message = "Agency share must be 0 or positive")
    @DecimalMax(value = "100.0",
            message = "Agency share cannot exceed 100%")
    @Digits(integer = 3, fraction = 2,
            message = "Agency share: max 3 digits, 2 decimals")
    private BigDecimal agencyShare;

    @NotNull(message = "Central share is required")
    @PositiveOrZero(message = "Central share must be 0 or positive")
    @DecimalMax(value = "100.0",
            message = "Central share cannot exceed 100%")
    @Digits(integer = 3, fraction = 2,
            message = "Central share: max 3 digits, 2 decimals")
    private BigDecimal centralShare;

    private Boolean active;

    public FeeGridRequestDTO() {}

    public FeeGridRequestDTO(Long corridorId, BigDecimal minAmount,
                             BigDecimal maxAmount,
                             BigDecimal fixedFee,
                             BigDecimal percentageFee,
                             BigDecimal agencyShare,
                             BigDecimal centralShare,
                             Boolean active) {
        this.corridorId    = corridorId;
        this.minAmount     = minAmount;
        this.maxAmount     = maxAmount;
        this.fixedFee      = fixedFee;
        this.percentageFee = percentageFee;
        this.agencyShare   = agencyShare;
        this.centralShare  = centralShare;
        this.active        = active;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long       corridorId;
        private BigDecimal minAmount;
        private BigDecimal maxAmount;
        private BigDecimal fixedFee;
        private BigDecimal percentageFee;
        private BigDecimal agencyShare;
        private BigDecimal centralShare;
        private Boolean    active;

        public Builder corridorId(Long corridorId)
        { this.corridorId = corridorId; return this; }
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
        public Builder active(Boolean active)
        { this.active = active; return this; }

        public FeeGridRequestDTO build() {
            FeeGridRequestDTO d = new FeeGridRequestDTO();
            d.corridorId    = this.corridorId;
            d.minAmount     = this.minAmount;
            d.maxAmount     = this.maxAmount;
            d.fixedFee      = this.fixedFee;
            d.percentageFee = this.percentageFee;
            d.agencyShare   = this.agencyShare;
            d.centralShare  = this.centralShare;
            d.active        = this.active;
            return d;
        }
    }

    // Getters / Setters
    public Long getCorridorId() { return corridorId; }
    public void setCorridorId(Long corridorId)
    { this.corridorId = corridorId; }

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

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    @Override
    public String toString() {
        return "FeeGridRequestDTO{corridorId=" + corridorId
                + ", range=[" + minAmount + "-" + maxAmount + "]}";
    }
}