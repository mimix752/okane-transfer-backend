package com.okanetransfer.controller;

import com.okanetransfer.dto.request.RateUpdateRequestDTO;
import com.okanetransfer.dto.response.ApiSyncResponseDTO;
import com.okanetransfer.dto.response.CurrencyRateHistoryResponseDTO;
import com.okanetransfer.service.ExchangeRateService;
import com.okanetransfer.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/exchange-rates")
@PreAuthorize("hasRole('ADMIN')")
@Tag(
        name        = "Exchange Rate Management",
        description = "Manual & automatic exchange rate updates"
)
@SecurityRequirement(name = "Bearer Authentication")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(
            ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    // ── Mise à jour manuelle ─────────────────────────────────

    @Operation(
            summary     = "Manual rate update",
            description = "Admin sets a new exchange rate manually. "
                    + "Saves history and triggers anomaly check."
    )
    @PostMapping("/manual")
    public ResponseEntity<ApiResponse<CurrencyRateHistoryResponseDTO>>
    updateManually(
            @Valid @RequestBody RateUpdateRequestDTO dto,
            HttpServletRequest request) {

        CurrencyRateHistoryResponseDTO result =
                exchangeRateService.updateManually(
                        dto, request.getRemoteAddr());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Rate updated successfully", result));
    }

    // ── Synchronisation automatique ──────────────────────────

    @Operation(
            summary     = "Auto sync from external API",
            description = "Fetches latest rates from external API "
                    + "(Yahoo Finance). Updates all active currencies. "
                    + "Returns summary: X updated, Y unchanged."
    )
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<ApiSyncResponseDTO>>
    syncFromApi(HttpServletRequest request) {

        ApiSyncResponseDTO result =
                exchangeRateService.syncFromApi(
                        request.getRemoteAddr());

        return ResponseEntity.ok(
                ApiResponse.success(result.getSummary(), result));
    }

    // ── Historique ───────────────────────────────────────────

    @Operation(
            summary     = "Get rate change history",
            description = "Returns paginated history of all rate changes. "
                    + "Shown in 'Historique des variations' in the UI."
    )
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<
            List<CurrencyRateHistoryResponseDTO>>>
    getHistory(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size) {

        List<CurrencyRateHistoryResponseDTO> history =
                exchangeRateService.getHistory(page, size);

        return ResponseEntity.ok(
                ApiResponse.success("History retrieved", history));
    }

    @Operation(
            summary     = "Get history for a specific currency",
            description = "Returns all rate changes for one currency"
    )
    @GetMapping("/history/currency/{currencyId}")
    public ResponseEntity<ApiResponse<
            List<CurrencyRateHistoryResponseDTO>>>
    getHistoryByCurrency(
            @Parameter(description = "Currency ID")
            @PathVariable Long currencyId) {

        List<CurrencyRateHistoryResponseDTO> history =
                exchangeRateService.getHistoryByCurrency(currencyId);

        return ResponseEntity.ok(
                ApiResponse.success("History retrieved", history));
    }
}