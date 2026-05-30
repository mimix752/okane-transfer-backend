package com.okanetransfer.entity;

import com.okanetransfer.enums.RateSource;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "currency_rate_history")
public class CurrencyRateHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // La devise dont le taux a changé  ex: USD, EUR
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "currency_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_rate_history_currency")
    )
    private Currency currency;

    // Taux avant le changement
    @Column(name = "old_rate", nullable = false,
            precision = 18, scale = 6)
    private BigDecimal oldRate;

    // Taux après le changement
    @Column(name = "new_rate", nullable = false,
            precision = 18, scale = 6)
    private BigDecimal newRate;

    // Variation en % : (newRate - oldRate) / oldRate * 100
    // Ex: +0.04 pour +0.04%
    @Column(name = "variation_percent",
            precision = 10, scale = 4)
    private BigDecimal variationPercent;

    // Qui a déclenché ce changement
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 30)
    private RateSource source;

    // Quand le changement a eu lieu
    @Column(name = "changed_at", nullable = false,
            updatable = false)
    private LocalDateTime changedAt;

    // ── Lifecycle ────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        if (this.changedAt == null) {
            this.changedAt = LocalDateTime.now();
        }
        // Calcul automatique de la variation
        if (this.variationPercent == null
                && this.oldRate != null
                && this.newRate != null
                && this.oldRate.compareTo(BigDecimal.ZERO) != 0) {
            this.variationPercent = this.newRate
                    .subtract(this.oldRate)
                    .divide(this.oldRate, 6,
                            java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(4, java.math.RoundingMode.HALF_UP);
        }
    }

    // ── Constructeurs ────────────────────────────────────────

    public CurrencyRateHistory() {}

    // ── Builder ──────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Currency      currency;
        private BigDecimal    oldRate;
        private BigDecimal    newRate;
        private BigDecimal    variationPercent;
        private RateSource    source;
        private LocalDateTime changedAt;

        public Builder currency(Currency currency)
        { this.currency = currency; return this; }
        public Builder oldRate(BigDecimal oldRate)
        { this.oldRate = oldRate; return this; }
        public Builder newRate(BigDecimal newRate)
        { this.newRate = newRate; return this; }
        public Builder variationPercent(BigDecimal variationPercent)
        { this.variationPercent = variationPercent; return this; }
        public Builder source(RateSource source)
        { this.source = source; return this; }
        public Builder changedAt(LocalDateTime changedAt)
        { this.changedAt = changedAt; return this; }

        public CurrencyRateHistory build() {
            CurrencyRateHistory h = new CurrencyRateHistory();
            h.currency         = this.currency;
            h.oldRate          = this.oldRate;
            h.newRate          = this.newRate;
            h.variationPercent = this.variationPercent;
            h.source           = this.source;
            h.changedAt        = this.changedAt != null
                    ? this.changedAt
                    : LocalDateTime.now();
            return h;
        }
    }

    // ── Getters / Setters ────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency)
    { this.currency = currency; }

    public BigDecimal getOldRate() { return oldRate; }
    public void setOldRate(BigDecimal oldRate)
    { this.oldRate = oldRate; }

    public BigDecimal getNewRate() { return newRate; }
    public void setNewRate(BigDecimal newRate)
    { this.newRate = newRate; }

    public BigDecimal getVariationPercent()
    { return variationPercent; }
    public void setVariationPercent(BigDecimal variationPercent)
    { this.variationPercent = variationPercent; }

    public RateSource getSource() { return source; }
    public void setSource(RateSource source)
    { this.source = source; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt)
    { this.changedAt = changedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CurrencyRateHistory)) return false;
        return Objects.equals(id, ((CurrencyRateHistory) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "CurrencyRateHistory{currency="
                + (currency != null ? currency.getCode() : "?")
                + ", " + oldRate + "→" + newRate
                + ", " + variationPercent + "%, " + source + "}";
    }
}