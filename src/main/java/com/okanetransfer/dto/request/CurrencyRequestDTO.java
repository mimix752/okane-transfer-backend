package com.okanetransfer.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Objects;

public class CurrencyRequestDTO {

    @NotBlank(message = "Code is required")
    @Size(min = 3, max = 3, message = "Code must be exactly 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$",
            message = "Code must be 3 uppercase letters")
    private String code;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Symbol is required")
    @Size(max = 10, message = "Symbol must not exceed 10 characters")
    private String symbol;

    @NotNull(message = "Exchange rate is required")
    @Positive(message = "Exchange rate must be positive")
    @Digits(integer = 10, fraction = 6,
            message = "Exchange rate: max 10 digits, 6 decimals")
    private BigDecimal exchangeRate;

    private Boolean active;

    // Constructeurs
    public CurrencyRequestDTO() {}

    public CurrencyRequestDTO(String code, String name,
                              String symbol,
                              BigDecimal exchangeRate,
                              Boolean active) {
        this.code         = code;
        this.name         = name;
        this.symbol       = symbol;
        this.exchangeRate = exchangeRate;
        this.active       = active;
    }

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String     code;
        private String     name;
        private String     symbol;
        private BigDecimal exchangeRate;
        private Boolean    active;

        public Builder code(String code)
        { this.code = code; return this; }
        public Builder name(String name)
        { this.name = name; return this; }
        public Builder symbol(String symbol)
        { this.symbol = symbol; return this; }
        public Builder exchangeRate(BigDecimal exchangeRate)
        { this.exchangeRate = exchangeRate; return this; }
        public Builder active(Boolean active)
        { this.active = active; return this; }

        public CurrencyRequestDTO build() {
            CurrencyRequestDTO d = new CurrencyRequestDTO();
            d.code         = this.code;
            d.name         = this.name;
            d.symbol       = this.symbol;
            d.exchangeRate = this.exchangeRate;
            d.active       = this.active;
            return d;
        }
    }

    // Getters / Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate)
    { this.exchangeRate = exchangeRate; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    @Override
    public String toString() {
        return "CurrencyRequestDTO{code='" + code + "'}";
    }
}