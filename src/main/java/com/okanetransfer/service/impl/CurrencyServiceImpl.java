package com.okanetransfer.service.impl;

import com.okanetransfer.dto.request.CurrencyRequestDTO;
import com.okanetransfer.dto.response.CurrencyResponseDTO;
import com.okanetransfer.entity.Currency;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.CorridorRepository;
import com.okanetransfer.repository.CurrencyRepository;
import com.okanetransfer.service.AuditService;
import com.okanetransfer.service.CurrencyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final CorridorRepository corridorRepository;
    private final AuditService auditLogService;

    // Constructeur manuel (remplace @RequiredArgsConstructor)
    public CurrencyServiceImpl(CurrencyRepository currencyRepository,
                               CorridorRepository corridorRepository,
                               AuditService auditLogService) {
        this.currencyRepository = currencyRepository;
        this.corridorRepository = corridorRepository;
        this.auditLogService    = auditLogService;
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
    public CurrencyResponseDTO create(CurrencyRequestDTO dto,
                                      String adminIp) {
        String code = dto.getCode().toUpperCase();
        if (currencyRepository.existsByCode(code)) {
            throw new IllegalArgumentException(
                    "Currency with code '" + code + "' already exists");
        }

        Currency currency = Currency.builder()
                .code(code)
                .name(dto.getName())
                .symbol(dto.getSymbol())
                .exchangeRate(dto.getExchangeRate())
                .active(dto.getActive() != null
                        ? dto.getActive() : true)
                .build();

        Currency saved = currencyRepository.save(currency);

        auditLogService.logAction("SYSTEM", "CREATE_CURRENCY",
                "currency", saved.getId(), null,
                "code=" + saved.getCode(), adminIp);

        return CurrencyResponseDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public CurrencyResponseDTO update(Long id,
                                      CurrencyRequestDTO dto,
                                      String adminIp) {
        Currency currency = findOrThrow(id);
        String newCode = dto.getCode().toUpperCase();

        if (currencyRepository.existsByCodeAndIdNot(newCode, id)) {
            throw new IllegalArgumentException(
                    "Currency with code '" + newCode + "' already exists");
        }

        String oldValue = "code=" + currency.getCode()
                + ", rate=" + currency.getExchangeRate();

        currency.setCode(newCode);
        currency.setName(dto.getName());
        currency.setSymbol(dto.getSymbol());
        currency.setExchangeRate(dto.getExchangeRate());
        if (dto.getActive() != null) {
            currency.setActive(dto.getActive());
        }

        Currency updated = currencyRepository.save(currency);

        auditLogService.logAction("SYSTEM", "UPDATE_CURRENCY",
                "currency", id, oldValue,
                "code=" + updated.getCode(), adminIp);

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

            if (usedByActiveCorridor) {
                throw new IllegalStateException(
                        "Cannot deactivate currency '"
                                + currency.getCode()
                                + "': used by active corridors");
            }
        }

        String oldStatus = String.valueOf(currency.isActive());
        currency.setActive(!currency.isActive());
        currencyRepository.save(currency);

        auditLogService.logAction("SYSTEM",
                currency.isActive()
                        ? "ACTIVATE_CURRENCY"
                        : "DEACTIVATE_CURRENCY",
                "currency", id,
                "active=" + oldStatus,
                "active=" + currency.isActive(), adminIp);
    }

    private Currency findOrThrow(Long id) {
        return currencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Currency not found with id: " + id));
    }
}