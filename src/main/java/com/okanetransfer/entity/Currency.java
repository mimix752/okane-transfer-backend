package com.okanetransfer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "currency")
public class Currency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 3)
    @Column(name = "code", nullable = false, unique = true, length = 3)
    private String code;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank
    @Column(name = "symbol", nullable = false, length = 10)
    private String symbol;

    @NotNull
    @Positive
    @Column(name = "exchange_rate", nullable = false,
            precision = 18, scale = 6)
    private BigDecimal exchangeRate;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @PrePersist
    protected void onCreate() {
        this.createdAt   = LocalDateTime.now();
        this.updatedAt   = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt   = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }


    public Currency() {}

    public Currency(Long id, String code, String name,
                    String symbol, BigDecimal exchangeRate,
                    boolean active, LocalDateTime lastUpdated,
                    LocalDateTime createdAt,
                    LocalDateTime updatedAt) {
        this.id           = id;
        this.code         = code;
        this.name         = name;
        this.symbol       = symbol;
        this.exchangeRate = exchangeRate;
        this.active       = active;
        this.lastUpdated  = lastUpdated;
        this.createdAt    = createdAt;
        this.updatedAt    = updatedAt;
    }


    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long          id;
        private String        code;
        private String        name;
        private String        symbol;
        private BigDecimal    exchangeRate;
        private boolean       active = true;
        private LocalDateTime lastUpdated;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

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
        public Builder updatedAt(LocalDateTime updatedAt)
        { this.updatedAt = updatedAt; return this; }

        public Currency build() {
            Currency c = new Currency();
            c.id           = this.id;
            c.code         = this.code;
            c.name         = this.name;
            c.symbol       = this.symbol;
            c.exchangeRate = this.exchangeRate;
            c.active       = this.active;
            c.lastUpdated  = this.lastUpdated;
            c.createdAt    = this.createdAt;
            c.updatedAt    = this.updatedAt;
            return c;
        }
    }


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

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)
    { this.updatedAt = updatedAt; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Currency)) return false;
        Currency c = (Currency) o;
        return Objects.equals(id, c.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Currency{id=" + id + ", code='" + code
                + "', active=" + active + "}";
    }
}