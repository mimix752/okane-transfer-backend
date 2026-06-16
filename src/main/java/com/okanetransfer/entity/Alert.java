package com.okanetransfer.entity;

import com.okanetransfer.enums.AlertLevel;
import com.okanetransfer.enums.AlertType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "alert")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 20)
    private AlertLevel level;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 40)
    private AlertType type;

    @Column(name = "description", nullable = false,
            columnDefinition = "TEXT")
    private String description;

    @Column(name = "entity_name", length = 200)
    private String entityName;

    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "created_at", nullable = false,
            updatable = false)
    private LocalDateTime createdAt;


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Alert() {}


    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private AlertLevel    level;
        private AlertType     type;
        private String        description;
        private String        entityName;
        private String        entityType;
        private Long          entityId;
        private boolean       isRead = false;

        public Builder level(AlertLevel level)
        { this.level = level; return this; }
        public Builder type(AlertType type)
        { this.type = type; return this; }
        public Builder description(String description)
        { this.description = description; return this; }
        public Builder entityName(String entityName)
        { this.entityName = entityName; return this; }
        public Builder entityType(String entityType)
        { this.entityType = entityType; return this; }
        public Builder entityId(Long entityId)
        { this.entityId = entityId; return this; }
        public Builder isRead(boolean isRead)
        { this.isRead = isRead; return this; }

        public Alert build() {
            Alert a = new Alert();
            a.level       = this.level;
            a.type        = this.type;
            a.description = this.description;
            a.entityName  = this.entityName;
            a.entityType  = this.entityType;
            a.entityId    = this.entityId;
            a.isRead      = this.isRead;
            return a;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AlertLevel getLevel() { return level; }
    public void setLevel(AlertLevel level) { this.level = level; }

    public AlertType getType() { return type; }
    public void setType(AlertType type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description)
    { this.description = description; }

    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName)
    { this.entityName = entityName; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType)
    { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId)
    { this.entityId = entityId; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean isRead) { this.isRead = isRead; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)
    { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Alert)) return false;
        return Objects.equals(id, ((Alert) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Alert{" + level + ", " + type
                + ", entity=" + entityName + "}";
    }
}