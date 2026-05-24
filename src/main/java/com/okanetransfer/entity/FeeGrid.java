package com.okanetransfer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "fee_grid")
public class FeeGrid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @PositiveOrZero
    @Column(name = "min_amount", nullable = false,
            precision = 15, scale = 2)
    private BigDecimal minAmount;

    @NotNull
    @Positive
    @Column(name = "max_amount", nullable = false,
            precision = 15, scale = 2)
    private BigDecimal maxAmount;

    @NotNull
    @PositiveOrZero
    @Column(name = "fixed_fee", nullable = false,
            precision = 10, scale = 2)
    private BigDecimal fixedFee;

    @NotNull
    @PositiveOrZero
    @DecimalMax("100.0")
    @Column(name = "percentage_fee", nullable = false,
            precision = 5, scale = 2)
    private BigDecimal percentageFee;

    @NotNull
    @PositiveOrZero
    @DecimalMax("100.0")
    @Column(name = "agency_share", nullable = false,
            precision = 5, scale = 2)
    private BigDecimal agencyShare;

    @NotNull
    @PositiveOrZero
    @DecimalMax("100.0")
    @Column(name = "central_share", nullable = false,
            precision = 5, scale = 2)
    private BigDecimal centralShare;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corridor_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_feegrid_corridor"))
    private Corridor corridor;

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


    public FeeGrid() {}

    public FeeGrid(Long id, BigDecimal minAmount,
                   BigDecimal maxAmount, BigDecimal fixedFee,
                   BigDecimal percentageFee, BigDecimal agencyShare,
                   BigDecimal centralShare, Corridor corridor,
                   boolean active, LocalDateTime createdAt,
                   LocalDateTime updatedAt) {
        this.id            = id;
        this.minAmount     = minAmount;
        this.maxAmount     = maxAmount;
        this.fixedFee      = fixedFee;
        this.percentageFee = percentageFee;
        this.agencyShare   = agencyShare;
        this.centralShare  = centralShare;
        this.corridor      = corridor;
        this.active        = active;
        this.createdAt     = createdAt;
        this.updatedAt     = updatedAt;
    }


    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long          id;
        private BigDecimal    minAmount;
        private BigDecimal    maxAmount;
        private BigDecimal    fixedFee;
        private BigDecimal    percentageFee;
        private BigDecimal    agencyShare;
        private BigDecimal    centralShare;
        private Corridor      corridor;
        private boolean       active = true;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id)
        { this.id = id; return this; }
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
        public Builder corridor(Corridor corridor)
        { this.corridor = corridor; return this; }
        public Builder active(boolean active)
        { this.active = active; return this; }
        public Builder createdAt(LocalDateTime createdAt)
        { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt)
        { this.updatedAt = updatedAt; return this; }

        public FeeGrid build() {
            FeeGrid f = new FeeGrid();
            f.id            = this.id;
            f.minAmount     = this.minAmount;
            f.maxAmount     = this.maxAmount;
            f.fixedFee      = this.fixedFee;
            f.percentageFee = this.percentageFee;
            f.agencyShare   = this.agencyShare;
            f.centralShare  = this.centralShare;
            f.corridor      = this.corridor;
            f.active        = this.active;
            f.createdAt     = this.createdAt;
            f.updatedAt     = this.updatedAt;
            return f;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public Corridor getCorridor() { return corridor; }
    public void setCorridor(Corridor corridor)
    { this.corridor = corridor; }

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
        if (!(o instanceof FeeGrid)) return false;
        FeeGrid f = (FeeGrid) o;
        return Objects.equals(id, f.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "FeeGrid{id=" + id
                + ", range=[" + minAmount + "-" + maxAmount
                + "], active=" + active + "}";
    }
}