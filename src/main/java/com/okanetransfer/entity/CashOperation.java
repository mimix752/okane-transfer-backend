package com.okanetransfer.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cash_operations")
public class CashOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cash_register_id", nullable = false)
    private CashRegister cashRegister;

    @Column(nullable = false)
    private String type; // ENVOI, RETRAIT

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(precision = 18, scale = 2)
    private BigDecimal balanceAfter;

    private String transferCode;

    private LocalDateTime createdAt = LocalDateTime.now();

    public CashOperation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public CashRegister getCashRegister() { return cashRegister; }
    public void setCashRegister(CashRegister cashRegister) { this.cashRegister = cashRegister; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }

    public String getTransferCode() { return transferCode; }
    public void setTransferCode(String transferCode) { this.transferCode = transferCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
