package com.okanetransfer.controller;

import com.okanetransfer.dto.response.ReportResponseDTO;
import com.okanetransfer.service.ReportService;
import com.okanetransfer.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(
        name        = "Reports",
        description = "Admin financial reports and statistics"
)
@SecurityRequirement(name = "Bearer Authentication")
public class ReportController {

    private final ReportService reportService;

    @Operation(
            summary     = "Global financial report",
            description = "Returns a complete financial report "
                    + "for a given period. "
                    + "Includes total volume, fees, "
                    + "breakdown by agency and by currency."
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

            @Parameter(
                    description = "Start date. Format: yyyy-MM-dd",
                    example     = "2024-01-01"
            )
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @Parameter(
                    description = "End date. Format: yyyy-MM-dd",
                    example     = "2024-01-31"
            )
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to) {

        validateDateRange(from, to);

        ReportResponseDTO report =
                reportService.getGlobalReport(from, to);

        return ResponseEntity.ok(
                ApiResponse.success("Report generated", report)
        );
    }

    @Operation(
            summary     = "Report filtered by corridor",
            description = "Returns a report filtered by a specific "
                    + "corridor. corridor param = agency name. "
                    + "Ex: 'Agence Casablanca'"
    )
    @GetMapping("/by-corridor")
    public ResponseEntity<ApiResponse<ReportResponseDTO>>
    getReportByCorridor(

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,

            @Parameter(
                    description = "Agency name or corridor code",
                    example     = "Agence Casablanca"
            )
            @RequestParam String corridor) {

        validateDateRange(from, to);

        ReportResponseDTO report =
                reportService.getReportByCorridor(
                        from, to, corridor);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Corridor report generated", report)
        );
    }

    @Operation(
            summary     = "Report filtered by transfer status",
            description = "Returns a report filtered by status. "
                    + "Allowed values: "
                    + "PENDING, VALIDATED, PAID, "
                    + "CANCELLED, EXPIRED"
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

            @Parameter(
                    description = "Transfer status",
                    example     = "PAID"
            )
            @RequestParam String status) {

        validateDateRange(from, to);
        validateStatus(status);

        ReportResponseDTO report =
                reportService.getReportByStatus(from, to, status);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Status report generated", report)
        );
    }

    @Operation(
            summary     = "Revenue report",
            description = "Returns total revenue and fees "
                    + "collected for a period. "
                    + "Only includes PAID transfers."
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

        ReportResponseDTO report =
                reportService.getReportByStatus(from, to, "PAID");

        return ResponseEntity.ok(
                ApiResponse.success("Revenue report generated", report)
        );
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "'from' date must be before or equal to 'to' date"
            );
        }

        if (from.plusMonths(12).isBefore(to)) {
            throw new IllegalArgumentException(
                    "Date range cannot exceed 12 months"
            );
        }
    }

    private void validateStatus(String status) {
        List<String> allowed = List.of(
                "PENDING", "VALIDATED",
                "PAID", "CANCELLED", "EXPIRED"
        );
        if (!allowed.contains(status.toUpperCase())) {
            throw new IllegalArgumentException(
                    "Invalid status: '" + status
                            + "'. Allowed: " + allowed
            );
        }
    }
}