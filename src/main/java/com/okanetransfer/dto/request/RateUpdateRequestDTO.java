package com.okanetransfer.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class RateUpdateRequestDTO {

    @NotNull(message = "Currency ID is required")
    @Positive(message = "Currency ID must be positive")
    private Long currencyId;

    @NotNull(message = "New rate is required")
    @Positive(message = "New rate must be positive")
    @Digits(integer = 10, fraction = 6,
            message = "Rate: max 10 digits, 6 decimals")
    private BigDecimal newRate;

    @Size(max = 255, message = "Note too long")
    private String note;

    public RateUpdateRequestDTO() {}

    public RateUpdateRequestDTO(Long currencyId,
                                BigDecimal newRate,
                                String note) {
        this.currencyId = currencyId;
        this.newRate    = newRate;
        this.note       = note;
    }

    public Long getCurrencyId() { return currencyId; }
    public void setCurrencyId(Long currencyId)
    { this.currencyId = currencyId; }

    public BigDecimal getNewRate() { return newRate; }
    public void setNewRate(BigDecimal newRate)
    { this.newRate = newRate; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}