package com.okanetransfer.controller;

import com.okanetransfer.dto.response.AlertResponseDTO;
import com.okanetransfer.enums.AlertLevel;
import com.okanetransfer.service.AlertService;
import com.okanetransfer.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/alerts")
@PreAuthorize("hasRole('ADMIN')")
@Tag(
        name        = "Automatic Alerts",
        description = "Anomaly alerts: volume, balance, API failures"
)
@SecurityRequirement(name = "Bearer Authentication")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @Operation(
            summary     = "Get all alerts",
            description = "Returns paginated list of all alerts. "
                    + "Sorted by date descending."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<AlertResponseDTO>>>
    getAllAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                ApiResponse.success("Alerts retrieved",
                        alertService.getAllAlerts(page, size)));
    }

    @Operation(
            summary     = "Get unread alerts",
            description = "Returns only unread alerts. "
                    + "Used for the notification badge."
    )
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<AlertResponseDTO>>>
    getUnreadAlerts() {

        return ResponseEntity.ok(
                ApiResponse.success("Unread alerts",
                        alertService.getUnreadAlerts()));
    }

    @Operation(
            summary     = "Get alerts by level",
            description = "Filter by CRITIQUE, ATTENTION or INFO"
    )
    @GetMapping("/level/{level}")
    public ResponseEntity<ApiResponse<List<AlertResponseDTO>>>
    getByLevel(
            @Parameter(
                    description = "Alert level",
                    example     = "CRITIQUE")
            @PathVariable AlertLevel level) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        level.name() + " alerts",
                        alertService.getByLevel(level)));
    }

    @Operation(
            summary     = "Count unread alerts by level",
            description = "Returns counts for badge display. "
                    + "Ex: {CRITIQUE:2, ATTENTION:1, INFO:5, TOTAL:8}"
    )
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>>
    countUnread() {

        return ResponseEntity.ok(
                ApiResponse.success("Alert counts",
                        alertService.countUnread()));
    }

    @Operation(
            summary     = "Mark alert as read",
            description = "Marks a single alert as read"
    )
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @Parameter(description = "Alert ID")
            @PathVariable Long id) {

        alertService.markAsRead(id);
        return ResponseEntity.ok(
                ApiResponse.success("Alert marked as read", null));
    }

    @Operation(
            summary     = "Mark all alerts as read",
            description = "Marks all unread alerts as read"
    )
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        alertService.markAllAsRead();
        return ResponseEntity.ok(
                ApiResponse.success("All alerts marked as read",
                        null));
    }

    @Operation(
            summary     = "Trigger anomaly checks manually",
            description = "Forces a check of volume and balance anomalies. "
                    + "Normally runs automatically."
    )
    @PostMapping("/check")
    public ResponseEntity<ApiResponse<Void>> triggerChecks() {
        alertService.checkVolumeAnomalies();
        alertService.checkLowBalances();
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Anomaly checks completed", null));
    }
}