package com.okanetransfer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "corridor",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_corridor_source_dest",
                        columnNames = {"source_country", "destination_country"}
                )
        }
)
public class Corridor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 3)
    @Column(name = "source_country", nullable = false, length = 3)
    private String sourceCountry;

    @NotBlank
    @Size(min = 2, max = 3)
    @Column(name = "destination_country", nullable = false, length = 3)
    private String destinationCountry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_currency_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_corridor_source_currency"))
    private Currency sourceCurrency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_currency_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_corridor_dest_currency"))
    private Currency destinationCurrency;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Corridor() {}

    public Corridor(Long id, String sourceCountry,
                    String destinationCountry,
                    Currency sourceCurrency,
                    Currency destinationCurrency,
                    boolean active,
                    LocalDateTime createdAt,
                    LocalDateTime updatedAt) {
        this.id                  = id;
        this.sourceCountry       = sourceCountry;
        this.destinationCountry  = destinationCountry;
        this.sourceCurrency      = sourceCurrency;
        this.destinationCurrency = destinationCurrency;
        this.active              = active;
        this.createdAt           = createdAt;
        this.updatedAt           = updatedAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long          id;
        private String        sourceCountry;
        private String        destinationCountry;
        private Currency      sourceCurrency;
        private Currency      destinationCurrency;
        private boolean       active = true;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id)
        { this.id = id; return this; }
        public Builder sourceCountry(String sourceCountry)
        { this.sourceCountry = sourceCountry; return this; }
        public Builder destinationCountry(String destinationCountry)
        { this.destinationCountry = destinationCountry; return this; }
        public Builder sourceCurrency(Currency sourceCurrency)
        { this.sourceCurrency = sourceCurrency; return this; }
        public Builder destinationCurrency(Currency destinationCurrency)
        { this.destinationCurrency = destinationCurrency; return this; }
        public Builder active(boolean active)
        { this.active = active; return this; }
        public Builder createdAt(LocalDateTime createdAt)
        { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt)
        { this.updatedAt = updatedAt; return this; }

        public Corridor build() {
            Corridor c = new Corridor();
            c.id                  = this.id;
            c.sourceCountry       = this.sourceCountry;
            c.destinationCountry  = this.destinationCountry;
            c.sourceCurrency      = this.sourceCurrency;
            c.destinationCurrency = this.destinationCurrency;
            c.active              = this.active;
            c.createdAt           = this.createdAt;
            c.updatedAt           = this.updatedAt;
            return c;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSourceCountry() { return sourceCountry; }
    public void setSourceCountry(String sourceCountry)
    { this.sourceCountry = sourceCountry; }

    public String getDestinationCountry() { return destinationCountry; }
    public void setDestinationCountry(String destinationCountry)
    { this.destinationCountry = destinationCountry; }

    public Currency getSourceCurrency() { return sourceCurrency; }
    public void setSourceCurrency(Currency sourceCurrency)
    { this.sourceCurrency = sourceCurrency; }

    public Currency getDestinationCurrency() { return destinationCurrency; }
    public void setDestinationCurrency(Currency destinationCurrency)
    { this.destinationCurrency = destinationCurrency; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)
    { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)
    { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Corridor)) return false;
        Corridor c = (Corridor) o;
        return Objects.equals(id, c.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Corridor{id=" + id
                + ", " + sourceCountry
                + "→" + destinationCountry
                + ", active=" + active + "}";
    }
}