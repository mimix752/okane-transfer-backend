package com.okanetransfer.service.impl;

import com.okanetransfer.dto.request.RateUpdateRequestDTO;
import com.okanetransfer.dto.response.ApiSyncResponseDTO;
import com.okanetransfer.dto.response.CurrencyRateHistoryResponseDTO;
import com.okanetransfer.entity.Currency;
import com.okanetransfer.entity.CurrencyRate;
import com.okanetransfer.entity.CurrencyRateHistory;
import com.okanetransfer.enums.RateSource;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.CurrencyRateHistoryRepository;
import com.okanetransfer.repository.CurrencyRateRepository;
import com.okanetransfer.repository.CurrencyRepository;
import com.okanetransfer.service.AlertService;
import com.okanetransfer.service.AuditService;
import com.okanetransfer.service.ExchangeRateService;
import com.okanetransfer.util.SecurityUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExchangeRateServiceImpl implements ExchangeRateService {

    private static final BigDecimal ANOMALY_THRESHOLD =
            BigDecimal.valueOf(5.0);

    private final CurrencyRepository            currencyRepository;
    private final CurrencyRateRepository        currencyRateRepository;
    private final CurrencyRateHistoryRepository historyRepository;
    private final AuditService                  auditLogService;
    private final AlertService                  alertService;

    public ExchangeRateServiceImpl(
            CurrencyRepository currencyRepository,
            CurrencyRateRepository currencyRateRepository,
            CurrencyRateHistoryRepository historyRepository,
            AuditService auditLogService,
            AlertService alertService) {
        this.currencyRepository     = currencyRepository;
        this.currencyRateRepository = currencyRateRepository;
        this.historyRepository      = historyRepository;
        this.auditLogService        = auditLogService;
        this.alertService           = alertService;
    }

    // ── Mise à jour manuelle ─────────────────────────────────

    @Override
    @Transactional
    public CurrencyRateHistoryResponseDTO updateManually(
            RateUpdateRequestDTO dto,
            String adminIp) {

        Currency currency = currencyRepository
                .findById(dto.getCurrencyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Currency not found: " + dto.getCurrencyId()));

        // USD est la devise de référence, son taux est toujours 1
        if ("USD".equals(currency.getCode())) {
            throw new IllegalArgumentException(
                    "USD est la devise de référence et ne peut pas être modifiée manuellement.");
        }

        BigDecimal oldRate = currency.getExchangeRate();
        BigDecimal newRate = dto.getNewRate();

        currency.setExchangeRate(newRate);
        currencyRepository.save(currency);

        CurrencyRateHistory history = saveHistory(
                currency, oldRate, newRate, RateSource.MANUAL);

        updateAllCurrencyRates(currency, newRate,
                RateSource.MANUAL);

        checkRateAnomaly(currency, oldRate, newRate,
                RateSource.MANUAL);

        auditLogService.log(
                "Admin : " + SecurityUtils.getCurrentUsername(), "MANUAL_RATE_UPDATE",
                "currency", currency.getId(),
                LocalDateTime.now() + " - Changement de Taux de conversion "+ currency.getCode() + " -> USD de : "+ oldRate
                        + " ,à : " + newRate
                        + (dto.getNote() != null
                        ? ", note :" + dto.getNote() : "")
        );

        return CurrencyRateHistoryResponseDTO.fromEntity(history);
    }

    // ── Synchronisation API ──────────────────────────────────

    @Override
    @Transactional
    public ApiSyncResponseDTO syncFromApi(String adminIp) {

        List<Currency> currencies =
                currencyRepository.findByActive(true);

        int updated   = 0;
        int unchanged = 0;
        int failed    = 0;
        List<String> updatedList = new ArrayList<>();
        List<String> errorList   = new ArrayList<>();

        for (Currency currency : currencies) {

            // USD est la devise de référence, on ne la met pas à jour
            if ("USD".equals(currency.getCode())) {
                unchanged++;
                continue;
            }

            try {
                // Récupérer le taux : currency → USD
                BigDecimal fetchedRate =
                        fetchRateFromApi(currency.getCode(), "USD");

                if (fetchedRate == null) {
                    failed++;
                    errorList.add(currency.getCode());
                    alertService.createApiFailureAlert(
                            currency.getCode());
                    continue;
                }

                BigDecimal oldRate = currency.getExchangeRate();

                // Ne mettre à jour que si le taux a changé
                if (fetchedRate.compareTo(oldRate) == 0) {
                    unchanged++;
                    continue;
                }

                currency.setExchangeRate(fetchedRate);
                currencyRepository.save(currency);

                saveHistory(currency, oldRate, fetchedRate,
                        RateSource.API_YAHOO_FINANCE);
                updateAllCurrencyRates(currency, fetchedRate,
                        RateSource.API_YAHOO_FINANCE);
                checkRateAnomaly(currency, oldRate, fetchedRate,
                        RateSource.API_YAHOO_FINANCE);

                updated++;
                updatedList.add(currency.getCode());

            } catch (Exception e) {
                failed++;
                errorList.add(currency.getCode()
                        + ": " + e.getMessage());
                alertService.createApiFailureAlert(
                        currency.getCode());
            }
        }

        auditLogService.log(
                "SYSTEM", "AUTO_RATE_SYNC", "currency",
                null,
                LocalDateTime.now() + " - Tentative de Modification automatique des Taux de conversion -> USD: updated=" + updated + ", failed=" + failed
        );

        return ApiSyncResponseDTO.builder()
                .totalProcessed(currencies.size())
                .updated(updated)
                .unchanged(unchanged)
                .failed(failed)
                .syncedAt(LocalDateTime.now())
                .apiSource("YAHOO_FINANCE")
                .updatedCurrencies(updatedList)
                .errors(errorList)
                .build();
    }

    // ── Historique ───────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<CurrencyRateHistoryResponseDTO> getHistory(
            int page, int size) {
        return historyRepository
                .findRecentHistory(PageRequest.of(page, size))
                .stream()
                .map(CurrencyRateHistoryResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CurrencyRateHistoryResponseDTO> getHistoryByCurrency(
            Long currencyId) {
        Currency currency = currencyRepository
                .findById(currencyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Currency not found: " + currencyId));
        return historyRepository
                .findByCurrencyOrderByChangedAtDesc(currency)
                .stream()
                .map(CurrencyRateHistoryResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ── Méthodes privées ─────────────────────────────────────

    private CurrencyRateHistory saveHistory(
            Currency currency,
            BigDecimal oldRate,
            BigDecimal newRate,
            RateSource source) {

        CurrencyRateHistory history = CurrencyRateHistory.builder()
                .currency(currency)
                .oldRate(oldRate)
                .newRate(newRate)
                .source(source)
                .changedAt(LocalDateTime.now())
                .build();

        return historyRepository.save(history);
    }

    private void updateAllCurrencyRates(
            Currency fromCurrency,
            BigDecimal newRateToUsd,
            RateSource source) {

        List<Currency> allCurrencies = currencyRepository.findByActive(true);

        for (Currency otherCurrency : allCurrencies) {

            if (otherCurrency.getId().equals(fromCurrency.getId())) continue;

            BigDecimal otherToUsd = otherCurrency.getExchangeRate();
            if (otherToUsd == null || otherToUsd.compareTo(BigDecimal.ZERO) == 0)
                continue;

            // ── Sens 1 : fromCurrency → otherCurrency ──────────────
            BigDecimal fromToOther;
            if ("USD".equals(otherCurrency.getCode())) {
                fromToOther = newRateToUsd;
            } else {
                fromToOther = newRateToUsd
                        .multiply(BigDecimal.ONE.divide(otherToUsd, 8, RoundingMode.HALF_UP))
                        .setScale(8, RoundingMode.HALF_UP);
            }

            CurrencyRate rowFrom = currencyRateRepository
                    .findByFromCurrencyAndToCurrencyAndActiveTrueOrderByAppliedAtDesc(
                            fromCurrency.getCode(), otherCurrency.getCode())
                    .orElseGet(() -> {
                        CurrencyRate r = new CurrencyRate();
                        r.setFromCurrency(fromCurrency.getCode());
                        r.setToCurrency(otherCurrency.getCode());
                        r.setPair(fromCurrency.getCode() + "_" + otherCurrency.getCode());
                        return r;
                    });
            rowFrom.setRate(fromToOther);
            rowFrom.setSource(source);
            rowFrom.setAppliedAt(LocalDateTime.now());
            currencyRateRepository.save(rowFrom);

            BigDecimal otherToFrom;
            if ("USD".equals(otherCurrency.getCode())) {
                otherToFrom = BigDecimal.ONE.divide(newRateToUsd, 8, RoundingMode.HALF_UP);
            } else {
                otherToFrom = otherToUsd
                        .multiply(BigDecimal.ONE.divide(newRateToUsd, 8, RoundingMode.HALF_UP))
                        .setScale(8, RoundingMode.HALF_UP);
            }

            CurrencyRate rowOther = currencyRateRepository
                    .findByFromCurrencyAndToCurrencyAndActiveTrueOrderByAppliedAtDesc(
                            otherCurrency.getCode(), fromCurrency.getCode())
                    .orElseGet(() -> {
                        CurrencyRate r = new CurrencyRate();
                        r.setFromCurrency(otherCurrency.getCode());
                        r.setToCurrency(fromCurrency.getCode());
                        r.setPair(otherCurrency.getCode() + "_" + fromCurrency.getCode());
                        return r;
                    });
            rowOther.setRate(otherToFrom);
            rowOther.setSource(source);
            rowOther.setAppliedAt(LocalDateTime.now());
            currencyRateRepository.save(rowOther);
        }
    }

    private void checkRateAnomaly(
            Currency currency,
            BigDecimal oldRate,
            BigDecimal newRate,
            RateSource source) {

        if (oldRate == null
                || oldRate.compareTo(BigDecimal.ZERO) == 0) return;

        BigDecimal variation = newRate.subtract(oldRate)
                .divide(oldRate, 4,
                        java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .abs();

        if (variation.compareTo(ANOMALY_THRESHOLD) > 0) {
            alertService.createRateAnomalyAlert(
                    currency,
                    oldRate, newRate, variation, source);
        }
    }

    private BigDecimal fetchRateFromApi(String fromCode,
                                        String toCode) {
        try {
            String apiUrl = System.getProperty(
                    "exchange.rate.api.url",
                    "https://api.exchangerate-api.com/v4/latest/"
                            + fromCode
            );

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .GET()
                    .timeout(java.time.Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) return null;

            String body   = response.body();
            String search = "\"" + toCode + "\":";
            int idx = body.indexOf(search);
            if (idx == -1) return null;

            int start = idx + search.length();
            int end   = body.indexOf(",", start);
            if (end == -1) end = body.indexOf("}", start);

            String rateStr = body.substring(start, end).trim();
            return new BigDecimal(rateStr);

        } catch (Exception e) {
            System.err.println("API rate fetch failed for "
                    + fromCode + "/" + toCode
                    + ": " + e.getMessage());
            return null;
        }
    }
}