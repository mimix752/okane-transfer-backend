package com.okanetransfer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "currency_rate")
public class CurrencyRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "from_currency", nullable = false, length = 3)
    private String fromCurrency;

    @NotBlank
    @Column(name = "to_currency", nullable = false, length = 3)
    private String toCurrency;

    @NotNull
    @Positive
    @Column(name = "rate", nullable = false, precision = 18, scale = 6)
    private BigDecimal rate;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "source", length = 50)
    private String source;

    @NotNull
    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.appliedAt == null) {
            this.appliedAt = LocalDateTime.now();
        }
    }


    public CurrencyRate() {}

    public CurrencyRate(Long id, String fromCurrency, String toCurrency, BigDecimal rate,
                        boolean active, String source, LocalDateTime appliedAt,
                        LocalDateTime createdAt) {
        this.id = id;
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rate = rate;
        this.active = active;
        this.source = source;
        this.appliedAt = appliedAt;
        this.createdAt = createdAt;
    }


    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long          id;
        private String        pair;
        private BigDecimal    rate;
        private String        source;
        private LocalDateTime appliedAt;
        private LocalDateTime createdAt;

        public Builder id(Long id)
        { this.id = id; return this; }
        public Builder pair(String pair)
        { this.pair = pair; return this; }
        public Builder rate(BigDecimal rate)
        { this.rate = rate; return this; }
        public Builder source(String source)
        { this.source = source; return this; }
        public Builder appliedAt(LocalDateTime appliedAt)
        { this.appliedAt = appliedAt; return this; }
        public Builder createdAt(LocalDateTime createdAt)
        { this.createdAt = createdAt; return this; }

        public CurrencyRate build() {
            CurrencyRate r = new CurrencyRate();
            r.id        = this.id;
            r.pair      = this.pair;
            r.rate      = this.rate;
            r.source    = this.source;
            r.appliedAt = this.appliedAt;
            r.createdAt = this.createdAt;
            return r;
        }
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFromCurrency() { return fromCurrency; }
    public void setFromCurrency(String fromCurrency) { this.fromCurrency = fromCurrency; }

    public String getToCurrency() { return toCurrency; }
    public void setToCurrency(String toCurrency) { this.toCurrency = toCurrency; }

    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt)
    { this.appliedAt = appliedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)
    { this.createdAt = createdAt; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CurrencyRate)) return false;
        CurrencyRate r = (CurrencyRate) o;
        return Objects.equals(id, r.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "CurrencyRate{id=" + id + ", pair='" + pair
                + "', rate=" + rate + "}";
    }
}