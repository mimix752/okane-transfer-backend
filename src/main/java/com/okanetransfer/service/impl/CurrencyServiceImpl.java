package com.okanetransfer.service.impl;

import com.okanetransfer.dto.request.CurrencyRequestDTO;
import com.okanetransfer.dto.response.CurrencyResponseDTO;
import com.okanetransfer.entity.Corridor;
import com.okanetransfer.entity.Currency;
import com.okanetransfer.entity.CurrencyRate;
import com.okanetransfer.enums.RateSource;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.CorridorRepository;
import com.okanetransfer.repository.CurrencyRateRepository;
import com.okanetransfer.repository.CurrencyRepository;
import com.okanetransfer.service.AuditService;
import com.okanetransfer.service.CurrencyService;
import com.okanetransfer.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository     currencyRepository;
    private final CorridorRepository     corridorRepository;
    private final CurrencyRateRepository currencyRateRepository;
    private final AuditService           auditService;

    public CurrencyServiceImpl(CurrencyRepository currencyRepository,
                               CorridorRepository corridorRepository,
                               CurrencyRateRepository currencyRateRepository,
                               AuditService auditService) {
        this.currencyRepository     = currencyRepository;
        this.corridorRepository     = corridorRepository;
        this.currencyRateRepository = currencyRateRepository;
        this.auditService           = auditService;
    }


    @Override
    @Transactional(readOnly = true)
    public List<CurrencyResponseDTO> getAllCurrencies() {
        return currencyRepository.findAll()
                .stream()
                .map(CurrencyResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CurrencyResponseDTO> getActiveCurrencies() {
        return currencyRepository.findByActive(true)
                .stream()
                .map(CurrencyResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CurrencyResponseDTO getById(Long id) {
        return CurrencyResponseDTO.fromEntity(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public CurrencyResponseDTO getByCode(String code) {
        Currency currency = currencyRepository
                .findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Currency not found with code: " + code));
        return CurrencyResponseDTO.fromEntity(currency);
    }


    @Override
    @Transactional
    public CurrencyResponseDTO create(CurrencyRequestDTO dto, String adminIp) {
        String code = dto.getCode().toUpperCase();

        if (currencyRepository.existsByCode(code))
            throw new IllegalArgumentException(
                    "Currency with code '" + code + "' already exists");

        Currency currency = Currency.builder()
                .code(code)
                .name(dto.getName())
                .symbol(dto.getSymbol())
                .exchangeRate(dto.getExchangeRate())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();

        Currency saved = currencyRepository.save(currency);

        if (saved.getExchangeRate() != null && !"USD".equals(saved.getCode())) {
            updateAllRatesForCurrency(saved, saved.getExchangeRate());
        }

        auditService.log(
                SecurityUtils.getCurrentUsername(),
                "CREATE_CURRENCY",
                "Currency",
                saved.getId(),
                LocalDateTime.now() + " - Creation de Devise avec code=" + saved.getCode()
                        + ", name=" + saved.getName()
                        + ", symbol=" + saved.getSymbol()
                        + ", rate=" + saved.getExchangeRate()
        );

        return CurrencyResponseDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public CurrencyResponseDTO update(Long id, CurrencyRequestDTO dto, String adminIp) {
        Currency currency = findOrThrow(id);
        String newCode = dto.getCode().toUpperCase();

        if (currencyRepository.existsByCodeAndIdNot(newCode, id))
            throw new IllegalArgumentException(
                    "Currency with code '" + newCode + "' already exists");

        String oldCode   = currency.getCode();
        String oldName   = currency.getName();
        String oldSymbol = currency.getSymbol();
        String oldRate   = String.valueOf(currency.getExchangeRate());

        currency.setCode(newCode);
        currency.setName(dto.getName());
        currency.setSymbol(dto.getSymbol());
        currency.setExchangeRate(dto.getExchangeRate());
        if (dto.getActive() != null)
            currency.setActive(dto.getActive());

        Currency updated = currencyRepository.save(currency);

        if (updated.getExchangeRate() != null && !"USD".equals(updated.getCode())) {
            updateAllRatesForCurrency(updated, updated.getExchangeRate());
        }

        auditService.log(
                SecurityUtils.getCurrentUsername(),
                "UPDATE_CURRENCY",
                "Currency",
                id,
                LocalDateTime.now() + " - Modification de Devise oldCode " + oldCode
                        + ", oldName=" + oldName
                        + ", oldSymbol=" + oldSymbol
                        + ", oldRate=" + oldRate
                        + " -> newCode=" + updated.getCode()
                        + ", newName=" + updated.getName()
                        + ", newSymbol=" + updated.getSymbol()
                        + ", newRate=" + updated.getExchangeRate()
        );

        return CurrencyResponseDTO.fromEntity(updated);
    }

    @Override
    @Transactional
    public void toggle(Long id, String adminIp) {
        Currency currency = findOrThrow(id);

        if (currency.isActive()) {
            boolean usedByActiveCorridor =
                    corridorRepository.findByCurrencyId(id)
                            .stream()
                            .anyMatch(Corridor::isActive);

            if (usedByActiveCorridor)
                throw new IllegalStateException(
                        "Cannot deactivate currency '"
                                + currency.getCode()
                                + "': used by active corridors");
        }

        boolean previous = currency.isActive();
        currency.setActive(!previous);
        currencyRepository.save(currency);

        String oldStatus = previous ? "active" : "inactive";
        String newStatus = !previous ? "active" : "inactive";

        auditService.log(
                SecurityUtils.getCurrentUsername(),
                previous ? "DEACTIVATE_CURRENCY" : "ACTIVATE_CURRENCY",
                "Currency",
                id,
                LocalDateTime.now() + " - Modification de status de Devise old : " + oldStatus
                        + " -> new : " + newStatus +
                        " (code = " + currency.getCode() + ")"
        );
    }

    private Currency findOrThrow(Long id) {
        return currencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Currency not found with id: " + id));
    }

    private void updateAllRatesForCurrency(Currency fromCurrency,
                                           BigDecimal newRateToUsd) {

        List<Currency> allCurrencies = currencyRepository.findByActive(true);

        for (Currency otherCurrency : allCurrencies) {

            if (otherCurrency.getId().equals(fromCurrency.getId())) continue;

            BigDecimal otherToUsd = otherCurrency.getExchangeRate();
            if (otherToUsd == null || otherToUsd.compareTo(BigDecimal.ZERO) == 0)
                continue;

            // ── Sens 1 : fromCurrency → otherCurrency ──────────────
            // FROM → X  =  newRateToUsd  ×  (1 / X → USD)
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
            rowFrom.setSource(RateSource.MANUAL);
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
            rowOther.setSource(RateSource.MANUAL);
            rowOther.setAppliedAt(LocalDateTime.now());
            currencyRateRepository.save(rowOther);
        }
    }
}