package com.okanetransfer.controller;

import com.okanetransfer.entity.AgentAuditTrail;
import com.okanetransfer.service.AgentAuditService;
import com.okanetransfer.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit/agents")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Agent Audit Trail", description = "Agent activity monitoring and audit trail")
public class AgentAuditController {

    @Autowired
    private AgentAuditService agentAuditService;

    @GetMapping("/{agentId}")
    @Operation(summary = "Get audit trail for specific agent")
    public ResponseEntity<ApiResponse<List<AgentAuditTrail>>> getAgentAuditTrail(
            @Parameter(description = "Agent ID") @PathVariable Long agentId,
            @Parameter(description = "Start date") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "End date") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        
        List<AgentAuditTrail> auditTrail = agentAuditService.getAgentAuditTrail(agentId, from, to);
        return ResponseEntity.ok(ApiResponse.success("Journal d'audit récupéré avec succès", auditTrail));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "Get audit trail for specific entity")
    public ResponseEntity<ApiResponse<List<AgentAuditTrail>>> getEntityAuditTrail(
            @Parameter(description = "Entity type") @PathVariable String entityType,
            @Parameter(description = "Entity ID") @PathVariable Long entityId) {
        
        List<AgentAuditTrail> auditTrail = agentAuditService.getEntityAuditTrail(entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success("Journal d'audit de l'entité récupéré avec succès", auditTrail));
    }

    @GetMapping("/activity/{agentId}")
    @Operation(summary = "Get agent activity count")
    public ResponseEntity<ApiResponse<Long>> getAgentActivityCount(
            @Parameter(description = "Agent ID") @PathVariable Long agentId,
            @Parameter(description = "Since date") @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        
        long activityCount = agentAuditService.getAgentActivityCount(agentId, since);
        return ResponseEntity.ok(ApiResponse.success("Nombre d'activités récupéré avec succès", activityCount));
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent audit entries")
    public ResponseEntity<ApiResponse<List<AgentAuditTrail>>> getRecentAuditEntries(
            @Parameter(description = "Hours back") @RequestParam(defaultValue = "24") int hours) {
        
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<AgentAuditTrail> auditTrail = agentAuditService.getAgentAuditTrail(null, since, LocalDateTime.now());
        return ResponseEntity.ok(ApiResponse.success("Entrées d'audit récentes récupérées avec succès", auditTrail));
    }
}