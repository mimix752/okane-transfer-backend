package com.okanetransfer.controller;

import com.okanetransfer.dto.request.FeeGridRequestDTO;
import com.okanetransfer.dto.response.FeeGridResponseDTO;
import com.okanetransfer.service.FeeGridService;
import com.okanetransfer.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/fee-grids")
@PreAuthorize("hasRole('ADMIN')")
@Tag(
        name        = "Fee Grid Management",
        description = "Admin endpoints for managing fee grids (pricing by amount range)"
)
@SecurityRequirement(name = "Bearer Authentication")
public class FeeGridController {

    private final FeeGridService feeGridService;

    public FeeGridController(FeeGridService feeGridService) {
        this.feeGridService = feeGridService;
    }

    @Operation(
            summary     = "Get fee grids for a corridor",
            description = "Returns all active fee grids for a specific corridor. "
                    + "Use ?corridorId to filter."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<FeeGridResponseDTO>>>
    getByCorridorId(
            @Parameter(description = "Corridor ID",
                    example = "1")
            @RequestParam Long corridorId) {

        List<FeeGridResponseDTO> result =
                feeGridService.getByCorridor(corridorId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Fee grids retrieved for corridor " + corridorId,
                        result));
    }

    @Operation(summary = "Get fee grid by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FeeGridResponseDTO>>
    getById(
            @Parameter(description = "Fee Grid ID")
            @PathVariable Long id) {

        FeeGridResponseDTO result = feeGridService.getById(id);

        return ResponseEntity.ok(
                ApiResponse.success("Fee grid retrieved", result));
    }

    @Operation(
            summary     = "Create a new fee grid",
            description = "Creates a new pricing tier for a corridor. "
                    + "Amount ranges must not overlap."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<FeeGridResponseDTO>>
    create(
            @Valid @RequestBody FeeGridRequestDTO dto,
            HttpServletRequest request) {

        FeeGridResponseDTO created = feeGridService.create(
                dto, request.getRemoteAddr());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Fee grid created successfully", created));
    }

    @Operation(
            summary     = "Update a fee grid",
            description = "Updates an existing fee grid (amount range, fees, shares)."
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FeeGridResponseDTO>>
    update(
            @Parameter(description = "Fee Grid ID")
            @PathVariable Long id,
            @Valid @RequestBody FeeGridRequestDTO dto,
            HttpServletRequest request) {

        FeeGridResponseDTO updated = feeGridService.update(
                id, dto, request.getRemoteAddr());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Fee grid updated successfully", updated));
    }

    @Operation(
            summary     = "Toggle fee grid activation",
            description = "Enable or disable a fee grid without deleting it."
    )
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<Void>>
    toggle(
            @Parameter(description = "Fee Grid ID")
            @PathVariable Long id,
            HttpServletRequest request) {

        feeGridService.toggle(id, request.getRemoteAddr());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Fee grid status toggled", null));
    }

    @Operation(
            summary     = "Simulate fees for an amount",
            description = "Calculates and displays fees that would be applied "
                    + "to a specific transfer amount on a corridor, "
                    + "without creating a real transfer. "
                    + "Useful for validation before transfer creation."
    )
    @PostMapping("/simulate")
    public ResponseEntity<ApiResponse<FeeGridResponseDTO>>
    simulate(
            @Parameter(description = "Corridor ID")
            @RequestParam Long corridorId,

            @Parameter(description = "Amount to check (in MAD)",
                    example = "5000.00")
            @RequestParam BigDecimal amount) {

        FeeGridResponseDTO result = feeGridService.simulate(
                corridorId, amount);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Fee simulation completed", result));
    }
}

