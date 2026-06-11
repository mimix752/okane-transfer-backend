package com.okanetransfer.service.impl;

import com.okanetransfer.dto.request.CurrencyRequestDTO;
import com.okanetransfer.dto.response.CurrencyResponseDTO;
import com.okanetransfer.entity.Currency;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.CorridorRepository;
import com.okanetransfer.repository.CurrencyRepository;
import com.okanetransfer.service.AuditService;
import com.okanetransfer.service.CurrencyService;
import com.okanetransfer.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final CorridorRepository corridorRepository;
    private final AuditService       auditService;

    public CurrencyServiceImpl(CurrencyRepository currencyRepository,
                               CorridorRepository corridorRepository,
                               AuditService auditService) {
        this.currencyRepository = currencyRepository;
        this.corridorRepository = corridorRepository;
        this.auditService       = auditService;
    }

    // ─── Queries ───────────────────────────────────────────────

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

    // ─── Commands ──────────────────────────────────────────────

    @Override
    @Transactional
    public CurrencyResponseDTO create(CurrencyRequestDTO dto,
                                      String adminIp) {
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
    public CurrencyResponseDTO update(Long id,
                                      CurrencyRequestDTO dto,
                                      String adminIp) {
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
                            .anyMatch(c -> c.isActive());

            if (usedByActiveCorridor)
                throw new IllegalStateException(
                        "Cannot deactivate currency '"
                                + currency.getCode()
                                + "': used by active corridors");
        }

        boolean previous = currency.isActive();
        currency.setActive(!previous);
        currencyRepository.save(currency);

        auditService.log(
                SecurityUtils.getCurrentUsername(),
                previous ? "DEACTIVATE_CURRENCY" : "ACTIVATE_CURRENCY",
                "Currency",
                id,
                "old=" + previous
                        + " | new=" + !previous
                        + " | code=" + currency.getCode()
                        + " | ip=" + adminIp
        );
    }

    // ─── Helpers ───────────────────────────────────────────────

    private Currency findOrThrow(Long id) {
        return currencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Currency not found with id: " + id));
    }
}