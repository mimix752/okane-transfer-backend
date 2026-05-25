package com.okanetransfer.controller;

import com.okanetransfer.dto.response.JournalAuditResponseDTO;
import com.okanetransfer.service.AuditService;
import com.okanetransfer.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
@Tag(
        name        = "Audit Log",
        description = "Read-only access to the complete audit journal"
)
@SecurityRequirement(name = "Bearer Authentication")
public class AuditLogController {

    @Autowired
    private AuditService auditService;

    public AuditLogController(AuditService auditService) {
        this.auditService = auditService;
    }

    @Operation(
            summary     = "Search audit logs",
            description = "Paginated search with optional filters: " +
                    "performedBy, action, entityType, entityId, " +
                    "date range. Max page size: 200."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description  = "Results returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description  = "Invalid parameters")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> search(

            @Parameter(description = "Username (partial match)")
            @RequestParam(required = false) String performedBy,

            @Parameter(description = "Action keyword (partial match)",
                    example = "CREATE_AGENCY")
            @RequestParam(required = false) String action,

            @Parameter(description = "Entity type",
                    example = "Agency")
            @RequestParam(required = false) String entityType,

            @Parameter(description = "Entity ID")
            @RequestParam(required = false) Long entityId,

            @Parameter(description = "From datetime (ISO 8601)",
                    example = "2024-01-01T00:00:00")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,

            @Parameter(description = "To datetime (ISO 8601)",
                    example = "2024-01-31T23:59:59")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to,

            @Parameter(description = "Page number (0-based)",
                    example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size (max 200)",
                    example = "50")
            @RequestParam(defaultValue = "50") int size) {

        Map<String, Object> result = auditService.search(
                performedBy, action, entityType,
                entityId, from, to, page, size);

        return ResponseEntity.ok(
                ApiResponse.success("Audit logs retrieved", result));
    }

    @Operation(summary = "Get audit log entry by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Entry found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JournalAuditResponseDTO>> getById(
            @Parameter(description = "Audit log ID")
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success(auditService.getById(id)));
    }

    @Operation(
            summary     = "Get full history of a specific entity",
            description = "Returns all audit entries for a given " +
                    "entityType + entityId, newest first."
    )
    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<ApiResponse<List<JournalAuditResponseDTO>>>
    getEntityHistory(
            @Parameter(description = "Entity type",
                    example = "Transfer")
            @PathVariable String entityType,

            @Parameter(description = "Entity ID")
            @PathVariable Long entityId) {

        List<JournalAuditResponseDTO> history =
                auditService.getHistoryForEntity(entityType, entityId);

        return ResponseEntity.ok(
                ApiResponse.success("Entity history retrieved", history));
    }
}