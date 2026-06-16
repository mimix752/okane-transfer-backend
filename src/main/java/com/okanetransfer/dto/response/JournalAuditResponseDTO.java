package com.okanetransfer.dto.response;

import java.time.LocalDateTime;

public class JournalAuditResponseDTO {

    private Long id;
    private String performedBy;
    private String action;
    private String entityType;
    private Long entityId;
    private LocalDateTime performedAt;
    private String details;

    public JournalAuditResponseDTO() {}

    public JournalAuditResponseDTO(Long id, String performedBy, String action,
                                   String entityType, Long entityId,
                                   LocalDateTime performedAt, String details) {
        this.id          = id;
        this.performedBy = performedBy;
        this.action      = action;
        this.entityType  = entityType;
        this.entityId    = entityId;
        this.performedAt = performedAt;
        this.details     = details;
    }

    public Long getId()                  { return id; }
    public void setId(Long id)           { this.id = id; }

    public String getPerformedBy()                   { return performedBy; }
    public void setPerformedBy(String performedBy)   { this.performedBy = performedBy; }

    public String getAction()                { return action; }
    public void setAction(String action)     { this.action = action; }

    public String getEntityType()                    { return entityType; }
    public void setEntityType(String entityType)     { this.entityType = entityType; }

    public Long getEntityId()                { return entityId; }
    public void setEntityId(Long entityId)   { this.entityId = entityId; }

    public LocalDateTime getPerformedAt()                    { return performedAt; }
    public void setPerformedAt(LocalDateTime performedAt)    { this.performedAt = performedAt; }

    public String getDetails()               { return details; }
    public void setDetails(String details)   { this.details = details; }
}