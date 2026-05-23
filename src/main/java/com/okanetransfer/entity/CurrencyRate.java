package com.okanetransfer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "currency_rate")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "pair",
            nullable = false,
            length = 10)
    private String pair;

    @NotNull
    @Positive
    @Column(name = "rate",
            nullable = false,
            precision = 18,
            scale = 6)
    private BigDecimal rate;

    @Column(name = "source", length = 50)
    private String source;

    @NotNull
    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt;

    @Column(name = "created_at",
            nullable = false,
            updatable = false)
    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.appliedAt == null) {
            this.appliedAt = LocalDateTime.now();
        }
    }
}