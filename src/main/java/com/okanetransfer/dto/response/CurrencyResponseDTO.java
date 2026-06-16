package com.okanetransfer.dto.response;

import com.okanetransfer.entity.Currency;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CurrencyResponseDTO {

    private Long          id;
    private String        code;
    private String        name;
    private String        symbol;
    private BigDecimal    exchangeRate;
    private boolean       active;
    private LocalDateTime lastUpdated;
    private LocalDateTime createdAt;

    public CurrencyResponseDTO() {}

    public CurrencyResponseDTO(Long id, String code, String name,
                               String symbol,
                               BigDecimal exchangeRate,
                               boolean active,
                               LocalDateTime lastUpdated,
                               LocalDateTime createdAt) {
        this.id           = id;
        this.code         = code;
        this.name         = name;
        this.symbol       = symbol;
        this.exchangeRate = exchangeRate;
        this.active       = active;
        this.lastUpdated  = lastUpdated;
        this.createdAt    = createdAt;
    }

    public static CurrencyResponseDTO fromEntity(Currency currency) {
        CurrencyResponseDTO d = new CurrencyResponseDTO();
        d.id           = currency.getId();
        d.code         = currency.getCode();
        d.name         = currency.getName();
        d.symbol       = currency.getSymbol();
        d.exchangeRate = currency.getExchangeRate();
        d.active       = currency.isActive();
        d.lastUpdated  = currency.getLastUpdated();
        d.createdAt    = currency.getCreatedAt();
        return d;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long          id;
        private String        code;
        private String        name;
        private String        symbol;
        private BigDecimal    exchangeRate;
        private boolean       active;
        private LocalDateTime lastUpdated;
        private LocalDateTime createdAt;

        public Builder id(Long id)
        { this.id = id; return this; }
        public Builder code(String code)
        { this.code = code; return this; }
        public Builder name(String name)
        { this.name = name; return this; }
        public Builder symbol(String symbol)
        { this.symbol = symbol; return this; }
        public Builder exchangeRate(BigDecimal exchangeRate)
        { this.exchangeRate = exchangeRate; return this; }
        public Builder active(boolean active)
        { this.active = active; return this; }
        public Builder lastUpdated(LocalDateTime lastUpdated)
        { this.lastUpdated = lastUpdated; return this; }
        public Builder createdAt(LocalDateTime createdAt)
        { this.createdAt = createdAt; return this; }

        public CurrencyResponseDTO build() {
            CurrencyResponseDTO d = new CurrencyResponseDTO();
            d.id           = this.id;
            d.code         = this.code;
            d.name         = this.name;
            d.symbol       = this.symbol;
            d.exchangeRate = this.exchangeRate;
            d.active       = this.active;
            d.lastUpdated  = this.lastUpdated;
            d.createdAt    = this.createdAt;
            return d;
        }
    }

    // Getters / Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate)
    { this.exchangeRate = exchangeRate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated)
    { this.lastUpdated = lastUpdated; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)
    { this.createdAt = createdAt; }
}