package com.okanetransfer.entity;

import com.okanetransfer.enums.AlertType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Seuils configurables pour les alertes automatiques.
 *
 * Remplace les valeurs hardcodées dans AlertServiceImpl.
 * Un admin peut modifier les seuils depuis l'interface.
 *
 * Exemples :
 *   VOLUME_INHABITUEL    → thresholdValue = 500000  (500k MAD/heure)
 *   SOLDE_AGENCE_BAS     → thresholdValue = 50000   (50k MAD)
 *   TAUX_CHANGE_ANOMALIE → thresholdValue = 5        (5%)
 *   ECHEC_API_PARTENAIRE → thresholdValue = 3        (3 échecs consec.)
 */
@Entity
@Table(
    name = "alert_threshold",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_alert_threshold_type",
        columnNames = {"alert_type"}
    )
)
public class AlertThreshold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Le type d'alerte concerné (1 seuil par type)
    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 40)
    private AlertType alertType;

    // La valeur du seuil
    @NotNull
    @Positive
    @Column(name = "threshold_value",
            nullable = false,
            precision = 18, scale = 2)
    private BigDecimal thresholdValue;

    // Unité lisible  ex: "MAD", "%", "nombre"
    @Column(name = "unit", length = 20)
    private String unit;

    // Description pour l'admin
    @Column(name = "description", length = 255)
    private String description;

    // Fenêtre anti-doublon en minutes
    @Column(name = "dedup_minutes", nullable = false)
    private int dedupMinutes = 30;

    // Alerte active ou non
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_at",
            nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── Lifecycle ────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ── Constructeurs ────────────────────────────────────────

    public AlertThreshold() {}

    // ── Builder ──────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private AlertType   alertType;
        private BigDecimal  thresholdValue;
        private String      unit;
        private String      description;
        private int         dedupMinutes = 30;
        private boolean     enabled = true;

        public Builder alertType(AlertType alertType)
            { this.alertType = alertType; return this; }
        public Builder thresholdValue(BigDecimal thresholdValue)
            { this.thresholdValue = thresholdValue; return this; }
        public Builder unit(String unit)
            { this.unit = unit; return this; }
        public Builder description(String description)
            { this.description = description; return this; }
        public Builder dedupMinutes(int dedupMinutes)
            { this.dedupMinutes = dedupMinutes; return this; }
        public Builder enabled(boolean enabled)
            { this.enabled = enabled; return this; }

        public AlertThreshold build() {
            AlertThreshold t = new AlertThreshold();
            t.alertType      = this.alertType;
            t.thresholdValue = this.thresholdValue;
            t.unit           = this.unit;
            t.description    = this.description;
            t.dedupMinutes   = this.dedupMinutes;
            t.enabled        = this.enabled;
            return t;
        }
    }

    // ── Getters / Setters ────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)
        { this.updatedAt = updatedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)
        { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AlertThreshold)) return false;
        return Objects.equals(id, ((AlertThreshold) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
