package com.okanetransfer.entity;

import com.okanetransfer.enums.Currency;
import com.okanetransfer.enums.TransferStatus;
import com.okanetransfer.security.CryptoConverter;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfers")
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, name = "transfer_code")
    private String transferCode;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    // ─── CINs chiffrés en AES-256 (CDC 4.3) ─────────────────────────────────────
    @Convert(converter = CryptoConverter.class)
    @Column(name = "sender_cin")
    private String senderCIN;

    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "recipient_phone")
    private String recipientPhone;

    @Convert(converter = CryptoConverter.class)
    @Column(name = "recipient_cin")
    private String recipientCIN;
    // ─────────────────────────────────────────────────────────────────────────────

    @Column(name = "recipient_country")
    private String recipientCountry;

    @Column(name = "sender_country")
    private String senderCountry;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(precision = 18, scale = 2)
    private BigDecimal fees = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(name = "converted_amount", precision = 18, scale = 2)
    private BigDecimal convertedAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_currency")
    private Currency targetCurrency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status = TransferStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "agency_id")
    private Agency agency;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Transfer() {}

    public Transfer(Long id, String transferCode, User sender, String senderCIN,
                    String recipientName, String recipientPhone, String recipientCountry,
                    String senderCountry, BigDecimal amount, Currency currency,
                    BigDecimal convertedAmount, Currency targetCurrency,
                    TransferStatus status, Agency agency,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.transferCode = transferCode;
        this.sender = sender;
        this.senderCIN = senderCIN;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.recipientCountry = recipientCountry;
        this.senderCountry = senderCountry;
        this.amount = amount;
        this.currency = currency;
        this.convertedAmount = convertedAmount;
        this.targetCurrency = targetCurrency;
        this.status = status;
        this.agency = agency;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTransferCode() { return transferCode; }
    public void setTransferCode(String transferCode) { this.transferCode = transferCode; }
    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }
    public String getSenderCIN() { return senderCIN; }
    public void setSenderCIN(String senderCIN) { this.senderCIN = senderCIN; }
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }
    public String getRecipientCIN() { return recipientCIN; }
    public void setRecipientCIN(String recipientCIN) { this.recipientCIN = recipientCIN; }
    public String getRecipientCountry() { return recipientCountry; }
    public void setRecipientCountry(String recipientCountry) { this.recipientCountry = recipientCountry; }
    public String getSenderCountry() { return senderCountry; }
    public void setSenderCountry(String senderCountry) { this.senderCountry = senderCountry; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getFees() { return fees; }
    public void setFees(BigDecimal fees) { this.fees = fees; }
    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }
    public BigDecimal getConvertedAmount() { return convertedAmount; }
    public void setConvertedAmount(BigDecimal convertedAmount) { this.convertedAmount = convertedAmount; }
    public Currency getTargetCurrency() { return targetCurrency; }
    public void setTargetCurrency(Currency targetCurrency) { this.targetCurrency = targetCurrency; }
    public TransferStatus getStatus() { return status; }
    public void setStatus(TransferStatus status) { this.status = status; }
    public Agency getAgency() { return agency; }
    public void setAgency(Agency agency) { this.agency = agency; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    void prePersist() { this.createdAt = LocalDateTime.now(); }

    @PreUpdate
    void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}