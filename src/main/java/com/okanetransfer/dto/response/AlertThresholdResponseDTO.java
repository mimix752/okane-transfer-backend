package com.okanetransfer.dto.response;

import com.okanetransfer.entity.AlertThreshold;
import com.okanetransfer.enums.AlertType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AlertThresholdResponseDTO {

    private Long          id;
    private AlertType     alertType;
    private BigDecimal    thresholdValue;
    private String        unit;
    private String        description;
    private int           dedupMinutes;
    private boolean       enabled;
    private LocalDateTime updatedAt;

    public AlertThresholdResponseDTO() {}

    public static AlertThresholdResponseDTO fromEntity(
            AlertThreshold t) {
        AlertThresholdResponseDTO d =
                new AlertThresholdResponseDTO();
        d.id             = t.getId();
        d.alertType      = t.getAlertType();
        d.thresholdValue = t.getThresholdValue();
        d.unit           = t.getUnit();
        d.description    = t.getDescription();
        d.dedupMinutes   = t.getDedupMinutes();
        d.enabled        = t.isEnabled();
        d.updatedAt      = t.getUpdatedAt();
        return d;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AlertType getAlertType() { return alertType; }
    public void setAlertType(AlertType v) { this.alertType = v; }
    public BigDecimal getThresholdValue() { return thresholdValue; }
    public void setThresholdValue(BigDecimal v)
    { this.thresholdValue = v; }
    public String getUnit() { return unit; }
    public void setUnit(String v) { this.unit = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public int getDedupMinutes() { return dedupMinutes; }
    public void setDedupMinutes(int v) { this.dedupMinutes = v; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean v) { this.enabled = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }
}