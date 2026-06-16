package com.okanetransfer.entity;

import com.okanetransfer.enums.MobileMoneyOperator;
import com.okanetransfer.enums.TransferStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mobile_money_transfers")
public class MobileMoneyTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "transfer_id", nullable = false)
    private Transfer transfer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MobileMoneyOperator operator;

    @Column(name = "mobile_account", nullable = false)
    private String mobileAccount; // numéro de téléphone du compte mobile money

    @Column(name = "operator_ref")
    private String operatorRef; // référence simulée de l'opérateur

    @Enumerated(EnumType.STRING)
    @Column(name = "reconciliation_status", nullable = false)
    private TransferStatus reconciliationStatus = TransferStatus.MOBILE_SENT;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "reconciled_at")
    private LocalDateTime reconciledAt;

    @Column(name = "operator_amount", precision = 18, scale = 2)
    private BigDecimal operatorAmount; // montant confirmé par l'opérateur lors réconciliation

    public MobileMoneyTransfer() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Transfer getTransfer() { return transfer; }
    public void setTransfer(Transfer transfer) { this.transfer = transfer; }
    public MobileMoneyOperator getOperator() { return operator; }
    public void setOperator(MobileMoneyOperator operator) { this.operator = operator; }
    public String getMobileAccount() { return mobileAccount; }
    public void setMobileAccount(String mobileAccount) { this.mobileAccount = mobileAccount; }
    public String getOperatorRef() { return operatorRef; }
    public void setOperatorRef(String operatorRef) { this.operatorRef = operatorRef; }
    public TransferStatus getReconciliationStatus() { return reconciliationStatus; }
    public void setReconciliationStatus(TransferStatus reconciliationStatus) { this.reconciliationStatus = reconciliationStatus; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    public LocalDateTime getReconciledAt() { return reconciledAt; }
    public void setReconciledAt(LocalDateTime reconciledAt) { this.reconciledAt = reconciledAt; }
    public BigDecimal getOperatorAmount() { return operatorAmount; }
    public void setOperatorAmount(BigDecimal operatorAmount) { this.operatorAmount = operatorAmount; }
}
