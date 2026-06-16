package com.okanetransfer.controller;

import com.okanetransfer.dto.request.AgencyRequestDTO;
import com.okanetransfer.dto.response.AgencyPerformanceResponseDTO;
import com.okanetransfer.dto.response.AgencyResponseDTO;
import com.okanetransfer.dto.response.PageResponse;
import com.okanetransfer.service.AgencyService;
import com.okanetransfer.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/agencies")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Agencies", description = "Agency management endpoints for administrators")
public class AgencyController {

    @Autowired
    private AgencyService agencyService;

    @GetMapping
    @Operation(summary = "Get all agencies", description = "Returns list of agencies filterable by country and status")
    public ResponseEntity<ApiResponse<List<AgencyResponseDTO>>> getAllAgencies(
            @Parameter(description = "Filter by country") @RequestParam(required = false) String country,
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean active) {

        List<AgencyResponseDTO> result = agencyService.getAllAgencies(country, active);
        return ResponseEntity.ok(ApiResponse.success("Agencies retrieved successfully", result));
    }

    @GetMapping("/paginated")
    @Operation(summary = "Get all agencies paginated", description = "Returns paginated list of agencies")
    public ResponseEntity<ApiResponse<PageResponse<AgencyResponseDTO>>> getAllAgenciesPaginated(Pageable pageable) {
        PageResponse<AgencyResponseDTO> result = agencyService.getAllAgenciesPaginated(pageable);
        return ResponseEntity.ok(ApiResponse.success("Agencies retrieved successfully", result));
    }

    @PostMapping
    @Operation(summary = "Create a new agency")
    public ResponseEntity<ApiResponse<AgencyResponseDTO>> createAgency(
            @Valid @RequestBody AgencyRequestDTO dto,
            HttpServletRequest request) {

        AgencyResponseDTO result = agencyService.create(dto, request.getRemoteAddr());
        return ResponseEntity.status(201).body(ApiResponse.success("Agency created successfully", result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get agency by ID")
    public ResponseEntity<ApiResponse<AgencyResponseDTO>> getById(
            @Parameter(description = "Agency ID") @PathVariable Long id) {

        AgencyResponseDTO result = agencyService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Agency retrieved successfully", result));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing agency")
    public ResponseEntity<ApiResponse<AgencyResponseDTO>> updateAgency(
            @Parameter(description = "Agency ID") @PathVariable Long id,
            @Valid @RequestBody AgencyRequestDTO dto,
            HttpServletRequest request) {

        AgencyResponseDTO result = agencyService.update(id, dto, request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success("Agency updated successfully", result));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle agency active status")
    public ResponseEntity<ApiResponse<Void>> toggleAgency(
            @Parameter(description = "Agency ID") @PathVariable Long id,
            HttpServletRequest request) {

        agencyService.toggle(id, request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success("Agency status toggled successfully", null));
    }

    @PostMapping("/{id}/agents")
    @Operation(summary = "Add an agent to an agency")
    public ResponseEntity<ApiResponse<Void>> addAgent(
            @Parameter(description = "Agency ID") @PathVariable Long id,
            @Parameter(description = "User ID to assign as agent") @RequestParam Long userId,
            HttpServletRequest request) {

        agencyService.addAgent(id, userId, request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success("Agent added successfully", null));
    }

    @DeleteMapping("/{id}/agents/{userId}")
    @Operation(summary = "Remove an agent from an agency")
    public ResponseEntity<ApiResponse<Void>> removeAgent(
            @Parameter(description = "Agency ID") @PathVariable Long id,
            @Parameter(description = "User ID of the agent to remove") @PathVariable Long userId,
            HttpServletRequest request) {

        agencyService.removeAgent(id, userId, request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success("Agent removed successfully", null));
    }

    @GetMapping("/{id}/performance")
    @Operation(summary = "Get agency performance metrics")
    public ResponseEntity<ApiResponse<AgencyPerformanceResponseDTO>> getPerformance(
            @Parameter(description = "Agency ID") @PathVariable Long id) {

        AgencyPerformanceResponseDTO result = agencyService.getPerformance(id);
        return ResponseEntity.ok(ApiResponse.success("Performance retrieved successfully", result));
    }

    @PostMapping("/{id}/balance")
    @Operation(summary = "Add balance to agency")
    public ResponseEntity<ApiResponse<Void>> addBalance(
            @Parameter(description = "Agency ID") @PathVariable Long id,
            @Parameter(description = "Amount to add") @RequestParam BigDecimal amount) {

        agencyService.addBalance(id, amount);
        return ResponseEntity.ok(ApiResponse.success("Balance added successfully", null));
    }
}