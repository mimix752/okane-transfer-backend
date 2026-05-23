package com.okanetransfer.service.impl;

import com.okanetransfer.dto.request.CorridorRequestDTO;
import com.okanetransfer.dto.response.CorridorResponseDTO;
import com.okanetransfer.entity.Corridor;
import com.okanetransfer.entity.Currency;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.CorridorRepository;
import com.okanetransfer.repository.CurrencyRepository;
import com.okanetransfer.service.AuditLogService;
import com.okanetransfer.service.CorridorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CorridorServiceImpl implements CorridorService {

    private final CorridorRepository  corridorRepository;
    private final CurrencyRepository  currencyRepository;
    private final AuditLogService     auditLogService;


    @Override
    @Transactional(readOnly = true)
    public List<CorridorResponseDTO> getAllCorridors() {
        return corridorRepository.findAll()
                .stream()
                .map(CorridorResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CorridorResponseDTO> getActiveCorridors() {
        return corridorRepository.findByActive(true)
                .stream()
                .map(CorridorResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CorridorResponseDTO> getBySourceCountry(
            String sourceCountry) {
        return corridorRepository
                .findBySourceCountry(sourceCountry.toUpperCase())
                .stream()
                .map(CorridorResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CorridorResponseDTO getById(Long id) {
        return CorridorResponseDTO.fromEntity(findOrThrow(id));
    }


    @Override
    @Transactional
    public CorridorResponseDTO create(CorridorRequestDTO dto,
                                      String adminIp) {

        String src  = dto.getSourceCountry().toUpperCase();
        String dest = dto.getDestinationCountry().toUpperCase();

        if (src.equals(dest)) {
            throw new IllegalArgumentException(
                    "Source and destination countries must be different"
            );
        }

        if (corridorRepository
                .existsBySourceCountryAndDestinationCountry(
                        src, dest)) {
            throw new IllegalArgumentException(
                    "Corridor " + src + " → " + dest
                            + " already exists"
            );
        }

        Currency srcCurrency  = findCurrencyOrThrow(
                dto.getSourceCurrencyId());
        Currency destCurrency = findCurrencyOrThrow(
                dto.getDestinationCurrencyId());

        Corridor corridor = Corridor.builder()
                .sourceCountry(src)
                .destinationCountry(dest)
                .sourceCurrency(srcCurrency)
                .destinationCurrency(destCurrency)
                .active(dto.getActive() != null
                        ? dto.getActive()
                        : true)
                .build();

        Corridor saved = corridorRepository.save(corridor);

        auditLogService.logAction(
                "SYSTEM", "CREATE_CORRIDOR", "corridor",
                saved.getId(), null,
                src + "→" + dest, adminIp
        );

        return CorridorResponseDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public CorridorResponseDTO update(Long id,
                                      CorridorRequestDTO dto,
                                      String adminIp) {

        Corridor corridor = findOrThrow(id);
        String src  = dto.getSourceCountry().toUpperCase();
        String dest = dto.getDestinationCountry().toUpperCase();

        if (src.equals(dest)) {
            throw new IllegalArgumentException(
                    "Source and destination countries must be different"
            );
        }

        if (corridorRepository
                .existsBySourceCountryAndDestinationCountryAndIdNot(
                        src, dest, id)) {
            throw new IllegalArgumentException(
                    "Corridor " + src + " → " + dest
                            + " already exists"
            );
        }

        String oldValue = corridor.getSourceCountry()
                + "→" + corridor.getDestinationCountry();

        corridor.setSourceCountry(src);
        corridor.setDestinationCountry(dest);
        corridor.setSourceCurrency(
                findCurrencyOrThrow(dto.getSourceCurrencyId()));
        corridor.setDestinationCurrency(
                findCurrencyOrThrow(dto.getDestinationCurrencyId()));
        if (dto.getActive() != null) {
            corridor.setActive(dto.getActive());
        }

        Corridor updated = corridorRepository.save(corridor);

        auditLogService.logAction(
                "SYSTEM", "UPDATE_CORRIDOR", "corridor",
                id, oldValue, src + "→" + dest, adminIp
        );

        return CorridorResponseDTO.fromEntity(updated);
    }

    @Override
    @Transactional
    public void toggle(Long id, String adminIp) {
        Corridor corridor = findOrThrow(id);
        String oldStatus  = String.valueOf(corridor.isActive());
        corridor.setActive(!corridor.isActive());
        corridorRepository.save(corridor);

        auditLogService.logAction(
                "SYSTEM",
                corridor.isActive()
                        ? "ACTIVATE_CORRIDOR"
                        : "DEACTIVATE_CORRIDOR",
                "corridor", id,
                "active=" + oldStatus,
                "active=" + corridor.isActive(),
                adminIp
        );
    }

    private Corridor findOrThrow(Long id) {
        return corridorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Corridor not found with id: " + id
                ));
    }

    private Currency findCurrencyOrThrow(Long id) {
        return currencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Currency not found with id: " + id
                ));
    }
}