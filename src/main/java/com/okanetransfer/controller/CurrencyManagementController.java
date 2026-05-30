package com.okanetransfer.controller;

import com.okanetransfer.entity.Currency;
import com.okanetransfer.entity.CurrencyRate;
import com.okanetransfer.service.CurrencyConversionService;
import com.okanetransfer.repository.CurrencyRateRepository;
import com.okanetransfer.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/currencies")
@Tag(name = "Currency Management", description = "Currency conversion and exchange rate management")
public class CurrencyManagementController {

    @Autowired
    private CurrencyConversionService currencyConversionService;

    @Autowired
    private CurrencyRateRepository currencyRateRepository;

    @GetMapping("/rates")
    @Operation(summary = "Get all active exchange rates")
    public ResponseEntity<ApiResponse<List<CurrencyRate>>> getActiveRates() {
        List<CurrencyRate> rates = currencyRateRepository.findByActiveTrue();
        return ResponseEntity.ok(ApiResponse.success("Taux de change récupérés avec succès", rates));
    }

    @GetMapping("/convert")
    @Operation(summary = "Convert amount between currencies")
    public ResponseEntity<ApiResponse<BigDecimal>> convertCurrency(
            @Parameter(description = "Amount to convert") @RequestParam BigDecimal amount,
            @Parameter(description = "Source currency code") @RequestParam Currency from,
            @Parameter(description = "Target currency code") @RequestParam Currency to) {
        
        BigDecimal convertedAmount = currencyConversionService.convertAmount(amount, from, to);
        return ResponseEntity.ok(ApiResponse.success("Conversion effectuée avec succès", convertedAmount));
    }

    @GetMapping("/rate")
    @Operation(summary = "Get exchange rate between two currencies")
    public ResponseEntity<ApiResponse<BigDecimal>> getExchangeRate(
            @Parameter(description = "Source currency code") @RequestParam Currency from,
            @Parameter(description = "Target currency code") @RequestParam Currency to) {
        
        BigDecimal rate = currencyConversionService.getExchangeRate(from, to);
        return ResponseEntity.ok(ApiResponse.success("Taux de change récupéré avec succès", rate));
    }

    @PostMapping("/rates")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update exchange rate")
    public ResponseEntity<ApiResponse<CurrencyRate>> updateExchangeRate(
            @Parameter(description = "Source currency code") @RequestParam Currency from,
            @Parameter(description = "Target currency code") @RequestParam Currency to,
            @Parameter(description = "Exchange rate") @RequestParam BigDecimal rate) {
        
        CurrencyRate newRate = currencyConversionService.updateExchangeRate(from, to, rate);
        return ResponseEntity.ok(ApiResponse.success("Taux de change mis à jour avec succès", newRate));
    }

    @GetMapping("/supported")
    @Operation(summary = "Get list of supported currencies")
    public ResponseEntity<ApiResponse<List<String>>> getSupportedCurrencies() {
        List<String> currencies = currencyRateRepository.findAllActiveCurrencies();
        return ResponseEntity.ok(ApiResponse.success("Devises supportées récupérées avec succès", currencies));
    }
}