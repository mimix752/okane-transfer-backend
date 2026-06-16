package com.okanetransfer.controller;

import com.okanetransfer.dto.request.AlertThresholdRequestDTO;
import com.okanetransfer.dto.response.AlertThresholdResponseDTO;
import com.okanetransfer.entity.AlertThreshold;
import com.okanetransfer.enums.AlertType;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.AlertThresholdRepository;
import com.okanetransfer.service.AuditService;
import com.okanetransfer.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/alert-thresholds")
@PreAuthorize("hasRole('ADMIN')")
@Tag(
        name        = "Alert Thresholds",
        description = "Configure alert thresholds (volume, balance, rate...)"
)
@SecurityRequirement(name = "Bearer Authentication")
public class AlertThresholdController {

    private final AlertThresholdRepository thresholdRepository;
    private final AuditService          auditLogService;

    public AlertThresholdController(
            AlertThresholdRepository thresholdRepository,
            AuditService auditLogService) {
        this.thresholdRepository = thresholdRepository;
        this.auditLogService     = auditLogService;
    }

    @Operation(
            summary     = "Get all alert thresholds",
            description = "Returns all configured thresholds. "
                    + "One threshold per alert type."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<
            List<AlertThresholdResponseDTO>>> getAll() {

        List<AlertThresholdResponseDTO> result =
                thresholdRepository.findAll()
                        .stream()
                        .map(AlertThresholdResponseDTO::fromEntity)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(
                ApiResponse.success("Thresholds retrieved", result));
    }

    @Operation(
            summary     = "Get threshold by alert type",
            description = "Ex: GET /alert-thresholds/type/VOLUME_INHABITUEL"
    )
    @GetMapping("/type/{alertType}")
    public ResponseEntity<ApiResponse<AlertThresholdResponseDTO>>
    getByType(@PathVariable AlertType alertType) {

        AlertThreshold threshold = thresholdRepository
                .findByAlertType(alertType)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No threshold for type: " + alertType));

        return ResponseEntity.ok(
                ApiResponse.success(
                        AlertThresholdResponseDTO.fromEntity(threshold)));
    }

    @Operation(
            summary     = "Create or update a threshold",
            description = "If threshold for this type already exists, "
                    + "it is updated. Otherwise, created."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<AlertThresholdResponseDTO>>
    createOrUpdate(
            @Valid @RequestBody AlertThresholdRequestDTO dto,
            HttpServletRequest request) {

        AlertThreshold threshold = thresholdRepository
                .findByAlertType(dto.getAlertType())
                .orElse(new AlertThreshold());

        String oldValue = threshold.getThresholdValue() != null
                ? threshold.getThresholdValue().toPlainString()
                : "null";

        threshold.setAlertType(dto.getAlertType());
        threshold.setThresholdValue(dto.getThresholdValue());
        threshold.setUnit(dto.getUnit());
        threshold.setDescription(dto.getDescription());
        threshold.setDedupMinutes(dto.getDedupMinutes());
        threshold.setEnabled(dto.isEnabled());

        AlertThreshold saved =
                thresholdRepository.save(threshold);

        auditLogService.log(
                "SYSTEM",
                "UPDATE_ALERT_THRESHOLD",
                "alert_threshold",
                saved.getId(),
                "value=" + oldValue + " -> " + dto.getThresholdValue()
        );

        boolean isNew = threshold.getId() == null;

        return ResponseEntity
                .status(isNew
                        ? HttpStatus.CREATED
                        : HttpStatus.OK)
                .body(ApiResponse.success(
                        isNew ? "Threshold created"
                                : "Threshold updated",
                        AlertThresholdResponseDTO.fromEntity(saved)));
    }

    @Operation(
            summary     = "Toggle threshold enabled/disabled",
            description = "Enables or disables a specific alert type "
                    + "without changing its value."
    )
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggle(
            @PathVariable Long id,
            HttpServletRequest request) {

        AlertThreshold threshold = thresholdRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Threshold not found: " + id));

        threshold.setEnabled(!threshold.isEnabled());
        thresholdRepository.save(threshold);

        auditLogService.log(
                "SYSTEM",
                threshold.isEnabled()
                        ? "ENABLE_ALERT_THRESHOLD"
                        : "DISABLE_ALERT_THRESHOLD",
                "alert_threshold", id,
                "enabled=" + threshold.isEnabled()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Threshold " + (threshold.isEnabled()
                                ? "enabled" : "disabled"), null));
    }
}