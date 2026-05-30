package com.okanetransfer.dto.response;

import com.okanetransfer.entity.Alert;
import com.okanetransfer.enums.AlertLevel;
import com.okanetransfer.enums.AlertType;
import java.time.LocalDateTime;

public class AlertResponseDTO {

    private Long          id;
    private AlertLevel    level;       // CRITIQUE / ATTENTION / INFO
    private AlertType     type;        // VOLUME_INHABITUEL / etc.
    private String        description;
    private String        entityName;  // "Agence Casablanca-04"
    private String        entityType;  // "AGENCY"
    private Long          entityId;
    private boolean       isRead;
    private LocalDateTime createdAt;

    public AlertResponseDTO() {}

    public static AlertResponseDTO fromEntity(Alert alert) {
        AlertResponseDTO d = new AlertResponseDTO();
        d.id          = alert.getId();
        d.level       = alert.getLevel();
        d.type        = alert.getType();
        d.description = alert.getDescription();
        d.entityName  = alert.getEntityName();
        d.entityType  = alert.getEntityType();
        d.entityId    = alert.getEntityId();
        d.isRead      = alert.isRead();
        d.createdAt   = alert.getCreatedAt();
        return d;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AlertLevel getLevel() { return level; }
    public void setLevel(AlertLevel v) { this.level = v; }
    public AlertType getType() { return type; }
    public void setType(AlertType v) { this.type = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getEntityName() { return entityName; }
    public void setEntityName(String v) { this.entityName = v; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String v) { this.entityType = v; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long v) { this.entityId = v; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean v) { this.isRead = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}