package com.okanetransfer.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "journal_audit")
public class JournalAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "performed_by", nullable = false)
    private String performedBy;

    @Column(nullable = false)
    private String action;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "performed_at", nullable = false, updatable = false)
    private LocalDateTime performedAt;

    @Column(columnDefinition = "TEXT")
    private String details;

    public JournalAudit() {
    }

    public JournalAudit(Long id, String performedBy, String action, String entityType, Long entityId, LocalDateTime performedAt, String details) {
        this.id = id;
        this.performedBy = performedBy;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.performedAt = performedAt;
        this.details = details;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public LocalDateTime getPerformedAt() {
        return performedAt;
    }

    public void setPerformedAt(LocalDateTime performedAt) {
        this.performedAt = performedAt;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @PrePersist
    protected void onCreate() {
        this.performedAt = LocalDateTime.now();
    }
}