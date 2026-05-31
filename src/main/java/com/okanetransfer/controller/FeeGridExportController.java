package com.okanetransfer.controller;

import com.okanetransfer.service.FeeGridExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/fee-grids/export")
@PreAuthorize("hasRole('ADMIN')")
@Tag(
        name        = "Fee Grid Export",
        description = "Export fee grids as PDF or CSV"
)
@SecurityRequirement(name = "Bearer Authentication")
public class FeeGridExportController {

    private final FeeGridExportService exportService;

    public FeeGridExportController(
            FeeGridExportService exportService) {
        this.exportService = exportService;
    }

    @Operation(
            summary     = "Export fee grid as PDF",
            description = "Exports the fee grid for a specific corridor "
                    + "as a formatted PDF. "
                    + "Use corridorId=null to export ALL corridors."
    )
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @Parameter(description = "Corridor ID (optional). "
                    + "Omit to export all corridors.")
            @RequestParam(required = false) Long corridorId) {

        ByteArrayOutputStream pdf;
        String filename;

        if (corridorId != null) {
            pdf      = exportService.exportPdf(corridorId);
            filename = "grille-tarifaire-corridor-"
                    + corridorId + "-"
                    + LocalDate.now() + ".pdf";
        } else {
            pdf      = exportService.exportAllPdf();
            filename = "grilles-tarifaires-tous-corridors-"
                    + LocalDate.now() + ".pdf";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf.toByteArray());
    }

    @Operation(
            summary     = "Export fee grid as CSV",
            description = "Exports the fee grid for a specific corridor "
                    + "as CSV (semicolon-separated, UTF-8 BOM for Excel). "
                    + "Omit corridorId to export all corridors."
    )
    @GetMapping("/csv")
    public ResponseEntity<byte[]> exportCsv(
            @Parameter(description = "Corridor ID (optional).")
            @RequestParam(required = false) Long corridorId) {

        ByteArrayOutputStream csv;
        String filename;

        if (corridorId != null) {
            csv      = exportService.exportCsv(corridorId);
            filename = "grille-tarifaire-corridor-"
                    + corridorId + "-"
                    + LocalDate.now() + ".csv";
        } else {
            csv      = exportService.exportAllCsv();
            filename = "grilles-tarifaires-tous-corridors-"
                    + LocalDate.now() + ".csv";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                // text/csv avec charset UTF-8
                .contentType(MediaType.parseMediaType(
                        "text/csv; charset=UTF-8"))
                .body(csv.toByteArray());
    }
}