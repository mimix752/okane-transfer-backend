package com.okanetransfer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fee_grid")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeGrid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @PositiveOrZero
    @Column(name = "min_amount",
            nullable = false,
            precision = 15,
            scale = 2)
    private BigDecimal minAmount;

    @NotNull
    @Positive
    @Column(name = "max_amount",
            nullable = false,
            precision = 15,
            scale = 2)
    private BigDecimal maxAmount;

    @NotNull
    @PositiveOrZero
    @Column(name = "fixed_fee",
            nullable = false,
            precision = 10,
            scale = 2)
    private BigDecimal fixedFee;

    @NotNull
    @PositiveOrZero
    @DecimalMax("100.0")
    @Column(name = "percentage_fee",
            nullable = false,
            precision = 5,
            scale = 2)
    private BigDecimal percentageFee;

    @NotNull
    @PositiveOrZero
    @DecimalMax("100.0")
    @Column(name = "agency_share",
            nullable = false,
            precision = 5,
            scale = 2)
    private BigDecimal agencyShare;

    @NotNull
    @PositiveOrZero
    @DecimalMax("100.0")
    @Column(name = "central_share",
            nullable = false,
            precision = 5,
            scale = 2)
    private BigDecimal centralShare;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corridor_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_feegrid_corridor"))
    private Corridor corridor;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at",
            nullable = false,
            updatable = false)
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
}