package com.okanetransfer.dto.response;

import com.okanetransfer.entity.CurrencyRateHistory;
import com.okanetransfer.enums.RateSource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CurrencyRateHistoryResponseDTO {

    private Long          id;
    private String        currencyCode;   // "USD"
    private String        currencyName;   // "US Dollar"
    private BigDecimal    oldRate;        // 10.08
    private BigDecimal    newRate;        // 10.12
    private BigDecimal    variationPercent; // +0.04
    private RateSource    source;         // API_REUTERS
    private LocalDateTime changedAt;

    public CurrencyRateHistoryResponseDTO() {}

    public static CurrencyRateHistoryResponseDTO fromEntity(
            CurrencyRateHistory h) {
        CurrencyRateHistoryResponseDTO d =
                new CurrencyRateHistoryResponseDTO();
        d.id                = h.getId();
        d.currencyCode      = h.getCurrency().getCode();
        d.currencyName      = h.getCurrency().getName();
        d.oldRate           = h.getOldRate();
        d.newRate           = h.getNewRate();
        d.variationPercent  = h.getVariationPercent();
        d.source            = h.getSource();
        d.changedAt         = h.getChangedAt();
        return d;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String v) { this.currencyCode = v; }
    public String getCurrencyName() { return currencyName; }
    public void setCurrencyName(String v) { this.currencyName = v; }
    public BigDecimal getOldRate() { return oldRate; }
    public void setOldRate(BigDecimal v) { this.oldRate = v; }
    public BigDecimal getNewRate() { return newRate; }
    public void setNewRate(BigDecimal v) { this.newRate = v; }
    public BigDecimal getVariationPercent()
    { return variationPercent; }
    public void setVariationPercent(BigDecimal v)
    { this.variationPercent = v; }
    public RateSource getSource() { return source; }
    public void setSource(RateSource v) { this.source = v; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime v) { this.changedAt = v; }
}