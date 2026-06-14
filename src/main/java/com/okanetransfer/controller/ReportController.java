package com.okanetransfer.controller;

import com.okanetransfer.dto.response.CorridorStatsResponseDTO;
import com.okanetransfer.dto.response.ReportResponseDTO;
import com.okanetransfer.service.ReportService;
import com.okanetransfer.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('ADMIN')")
@Tag(
        name        = "Reports",
        description = "Admin financial reports and statistics"
)
@SecurityRequirement(name = "Bearer Authentication")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // RAPPORT GLOBAL

    @Operation(
            summary     = "Global financial report",
            description = "Complete report for a period: volume, fees, "
                    + "breakdown by agency and currency."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description  = "Report generated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description  = "Invalid date format or range")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<ReportResponseDTO>>
    getGlobalReport(
            @Parameter(description = "Start date yyyy-MM-dd",
                    example = "2024-01-01")
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @Parameter(description = "End date yyyy-MM-dd",
                    example = "2024-01-31")
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to) {

        validateDateRange(from, to);
        return ResponseEntity.ok(ApiResponse.success(
                "Report generated",
                reportService.getGlobalReport(from, to)));
    }

    // RAPPORT PAR CORRIDOR — VOLUME JOURNALIER + MENSUEL

    @Operation(
            summary     = "Corridor stats — daily & monthly volumes",
            description = "Returns volume journalier ET mensuel "
                    + "pour un corridor précis. "
                    + "Inclut : nombre d'opérations, CA, "
                    + "commissions agence/centrale. "
                    + "Période calculée automatiquement : "
                    + "aujourd'hui pour le journalier, "
                    + "1er du mois à maintenant pour le mensuel."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description  = "Stats retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description  = "Corridor not found")
    })
    @GetMapping("/by-corridor/{corridorId}")
    public ResponseEntity<ApiResponse<CorridorStatsResponseDTO>>
    getCorridorStats(
            @PathVariable Long corridorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(ApiResponse.success(
                "Corridor stats retrieved",
                reportService.getCorridorStats(corridorId, from, to)));
    }

    @Operation(
            summary     = "All corridors stats — daily & monthly",
            description = "Retourne les volumes journaliers ET mensuels "
                    + "de TOUS les corridors actifs. "
                    + "Triés par volume mensuel décroissant."
    )
    @GetMapping("/by-corridor")
    public ResponseEntity<ApiResponse<List<CorridorStatsResponseDTO>>>
    getAllCorridorStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(ApiResponse.success(
                "All corridor stats retrieved",
                reportService.getAllCorridorStats(from, to)));
    }


    // RAPPORT PAR STATUT

    @Operation(
            summary     = "Report filtered by transfer status",
            description = "Valeurs autorisées : "
                    + "PENDING, VALIDATED, PAID, CANCELLED, EXPIRED"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description  = "Report generated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description  = "Invalid status value")
    })
    @GetMapping("/by-status")
    public ResponseEntity<ApiResponse<ReportResponseDTO>>
    getReportByStatus(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,

            @Parameter(description = "Transfer status",
                    example = "PAID")
            @RequestParam String status) {

        validateDateRange(from, to);
        validateStatus(status);

        return ResponseEntity.ok(ApiResponse.success(
                "Status report generated",
                reportService.getReportByStatus(from, to, status)));
    }

    // RAPPORT REVENUS

    @Operation(
            summary     = "Revenue report",
            description = "CA total sur les transferts PAID uniquement."
    )
    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<ReportResponseDTO>>
    getRevenueReport(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to) {

        validateDateRange(from, to);

        return ResponseEntity.ok(ApiResponse.success(
                "Revenue report generated",
                reportService.getReportByStatus(from, to, "PAID")));
    }

    // VALIDATIONS PRIVÉES

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "'from' doit être avant ou égal à 'to'");
        }
        if (from.plusMonths(12).isBefore(to)) {
            throw new IllegalArgumentException(
                    "La période ne peut pas dépasser 12 mois");
        }
    }

    private void validateStatus(String status) {
        List<String> allowed = List.of(
                "PENDING", "VALIDATED",
                "PAID", "CANCELLED", "EXPIRED"
        );
        if (!allowed.contains(status.toUpperCase())) {
            throw new IllegalArgumentException(
                    "Statut invalide : '" + status
                            + "'. Autorisés : " + allowed);
        }
    }
}