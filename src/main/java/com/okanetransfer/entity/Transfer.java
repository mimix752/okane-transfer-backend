package com.okanetransfer.entity;

import com.okanetransfer.enums.Currency;
import com.okanetransfer.enums.TransferStatus;
import lombok.Data;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transfers")
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String transferCode;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    private String recipientName;
    private String recipientPhone;
    private String recipientCountry;
    private String senderCountry;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(precision = 18, scale = 2)
    private BigDecimal convertedAmount;

    @Enumerated(EnumType.STRING)
    private Currency targetCurrency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status = TransferStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "agency_id")
    private Agency agency;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() { this.createdAt = LocalDateTime.now(); }

    @PreUpdate
    void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}
