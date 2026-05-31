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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    private final CurrencyRepository             currencyRepository;
    private final CurrencyRateRepository         currencyRateRepository;
    private final CurrencyRateHistoryRepository  historyRepository;
    private final AuditService                auditLogService;
    private final AlertService                   alertService;

    public ExchangeRateServiceImpl(
            CurrencyRepository currencyRepository,
            CurrencyRateRepository currencyRateRepository,
            CurrencyRateHistoryRepository historyRepository,
            AuditService auditLogService,
            AlertService alertService) {
        this.currencyRepository    = currencyRepository;
        this.currencyRateRepository = currencyRateRepository;
        this.historyRepository     = historyRepository;
        this.auditLogService       = auditLogService;
        this.alertService          = alertService;
    }


    @Override
    @Transactional
    public CurrencyRateHistoryResponseDTO updateManually(
            RateUpdateRequestDTO dto,
            String adminIp) {

        // 1. Trouver la devise
        Currency currency = currencyRepository
                .findById(dto.getCurrencyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Currency not found: " + dto.getCurrencyId()
                ));

        BigDecimal oldRate = currency.getExchangeRate();
        BigDecimal newRate = dto.getNewRate();

        currency.setExchangeRate(newRate);
        currencyRepository.save(currency);

        CurrencyRateHistory history = saveHistory(
                currency, oldRate, newRate, RateSource.MANUAL);

        saveCurrencyRateSnapshot(currency, newRate,
                RateSource.MANUAL);

        checkRateAnomaly(currency, oldRate, newRate,
                RateSource.MANUAL);

        auditLogService.log(
                "SYSTEM", "MANUAL_RATE_UPDATE",
                "currency", currency.getId(),
                "rate=" + oldRate +
                "rate=" + newRate
                        + (dto.getNote() != null
                        ? ", note=" + dto.getNote() : "")
        );

        return CurrencyRateHistoryResponseDTO.fromEntity(history);
    }


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

            // MAD est la devise de référence, on ne la met pas à jour
            if ("MAD".equals(currency.getCode())) {
                unchanged++;
                continue;
            }

            try {
                // Appel API externe pour récupérer le taux
                BigDecimal fetchedRate =
                        fetchRateFromApi(currency.getCode(), "MAD");

                if (fetchedRate == null) {
                    failed++;
                    errorList.add(currency.getCode());
                    // Générer alerte ECHEC_API
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

                // Mettre à jour
                currency.setExchangeRate(fetchedRate);
                currencyRepository.save(currency);

                saveHistory(currency, oldRate, fetchedRate,
                        RateSource.API_YAHOO_FINANCE);
                saveCurrencyRateSnapshot(currency, fetchedRate,
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

        // Journaliser la synchro
        auditLogService.log(
                "SYSTEM", "AUTO_RATE_SYNC", "currency",
                null,
                "updated=" + updated + ", failed=" + failed
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

    /** Sauvegarde une ligne dans currency_rate_history */
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

    /** Sauvegarde un snapshot dans currency_rate */
    private void saveCurrencyRateSnapshot(
            Currency fromCurrency,
            BigDecimal rate,
            RateSource source) {

        // La devise "to" est MAD (devise de référence)
        Currency mad = currencyRepository
                .findByCode("MAD")
                .orElse(null);

        if (mad == null) return;

        CurrencyRate snapshot = CurrencyRate.builder()
                .fromCurrency(fromCurrency.getCode())
                .toCurrency(mad.getCode())
                .rate(rate)
                .source(source)
                .appliedAt(LocalDateTime.now())
                .build();

        currencyRateRepository.save(snapshot);
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
                    oldRate, newRate, variation, source
            );
        }
    }

    private BigDecimal fetchRateFromApi(String fromCode,
                                        String toCode) {
        try {

            String apiUrl = System.getProperty(
                    "exchange.rate.api.url",
                    // URL de test publique
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

            if (response.statusCode() != 200) {
                return null;
            }

            String body = response.body();
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
                    + fromCode + "/" + toCode + ": " + e.getMessage());
            return null;
        }
    }
}