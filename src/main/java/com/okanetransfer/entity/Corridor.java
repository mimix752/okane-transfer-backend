package com.okanetransfer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

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
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Corridor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 3)
    @Column(name = "source_country",
            nullable = false,
            length = 3)
    private String sourceCountry;

    @NotBlank
    @Size(min = 2, max = 3)
    @Column(name = "destination_country",
            nullable = false,
            length = 3)
    private String destinationCountry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_currency_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_corridor_source_currency"))
    private Currency sourceCurrency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_currency_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_corridor_dest_currency"))
    private Currency destinationCurrency;

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