package com.okanetransfer.dto.request;

import com.okanetransfer.enums.AlertType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class AlertThresholdRequestDTO {

    @NotNull(message = "Alert type is required")
    private AlertType alertType;

    @NotNull(message = "Threshold value is required")
    @Positive(message = "Threshold value must be positive")
    private BigDecimal thresholdValue;

    @Size(max = 20)
    private String unit;

    @Size(max = 255)
    private String description;

    @Min(value = 1, message = "Dedup minutes must be >= 1")
    @Max(value = 1440, message = "Dedup minutes must be <= 1440 (24h)")
    private int dedupMinutes = 30;

    private boolean enabled = true;

    public AlertThresholdRequestDTO() {}

    public AlertType getAlertType() { return alertType; }
    public void setAlertType(AlertType alertType)
    { this.alertType = alertType; }

    public BigDecimal getThresholdValue() { return thresholdValue; }
    public void setThresholdValue(BigDecimal thresholdValue)
    { this.thresholdValue = thresholdValue; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getDescription() { return description; }
    public void setDescription(String description)
    { this.description = description; }

    public int getDedupMinutes() { return dedupMinutes; }
    public void setDedupMinutes(int dedupMinutes)
    { this.dedupMinutes = dedupMinutes; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled)
    { this.enabled = enabled; }
}