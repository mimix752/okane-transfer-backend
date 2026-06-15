package com.okanetransfer.service.impl;

import com.okanetransfer.dto.request.CorridorRequestDTO;
import com.okanetransfer.dto.response.CorridorResponseDTO;
import com.okanetransfer.dto.response.CorridorStatsResponseDTO;
import com.okanetransfer.dto.response.CurrencyResponseDTO;
import com.okanetransfer.entity.Corridor;
import com.okanetransfer.entity.Currency;
import com.okanetransfer.entity.FeeGrid;
import com.okanetransfer.entity.Transfer;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.CorridorRepository;
import com.okanetransfer.repository.CurrencyRepository;
import com.okanetransfer.repository.FeeGridRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import com.okanetransfer.repository.TransferRepository;
import com.okanetransfer.service.AuditService;
import com.okanetransfer.service.CorridorService;
import com.okanetransfer.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CorridorServiceImpl implements CorridorService {


    private final CorridorRepository corridorRepository;
    private final CurrencyRepository currencyRepository;
    private final AuditService       auditService;
    private final TransferRepository transferRepository;
    private final FeeGridRepository feeGridRepository;

    public CorridorServiceImpl(CorridorRepository corridorRepository,
                               CurrencyRepository currencyRepository,
                               AuditService auditLogService,
                               TransferRepository transferRepository,
                               FeeGridRepository feeGridRepository) {
        this.corridorRepository  = corridorRepository;
        this.currencyRepository  = currencyRepository;
        this.auditService     = auditLogService;
        this.transferRepository  = transferRepository;
        this.feeGridRepository   = feeGridRepository;
    }
    // ─── Queries ───────────────────────────────────────────────


    @Override
    @Transactional(readOnly = true)
    public CorridorResponseDTO findByCountries(String from, String to) {
        Corridor corridor = corridorRepository
                .findBySourceCountryAndDestinationCountryAndActiveTrue(from, to)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active corridor from " + from + " to " + to));
        return CorridorResponseDTO.fromEntity(corridor);
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
    public List<CorridorResponseDTO> getAllCorridors() {
        return corridorRepository.findAll()
                .stream()
                .map(CorridorResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CorridorResponseDTO> getActiveCorridorsPaginated(Pageable pageable) {
        Page<Corridor> page = corridorRepository.findByActive(true, pageable);
        return page.map(CorridorResponseDTO::fromEntity);
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

    // ─── Commands ──────────────────────────────────────────────

    @Override
    @Transactional
    public CorridorResponseDTO create(CorridorRequestDTO dto,
                                      String adminIp) {
        String src  = dto.getSourceCountry().toUpperCase();
        String dest = dto.getDestinationCountry().toUpperCase();

        if (corridorRepository
                .existsBySourceCountryAndDestinationCountry(src, dest))
            throw new IllegalArgumentException(
                    "Corridor " + src + "→" + dest + " already exists");

        Currency srcCurrency  = findCurrencyOrThrow(
                dto.getSourceCurrencyId());
        Currency destCurrency = findCurrencyOrThrow(
                dto.getDestinationCurrencyId());

        Corridor corridor = Corridor.builder()
                .sourceCountry(src)
                .destinationCountry(dest)
                .sourceCurrency(srcCurrency)
                .destinationCurrency(destCurrency)
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();

        Corridor saved = corridorRepository.save(corridor);

        auditService.log(
                SecurityUtils.getCurrentUsername(),
                "CREATE_CORRIDOR",
                "Corridor",
                saved.getId(),
                LocalDateTime.now() + " - Creqtion de corridor=" + src + "→" + dest
                        + " | Devise source : " + srcCurrency.getCode()
                        + " | Devise destination : " + destCurrency.getCode()
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

        if (corridorRepository
                .existsBySourceCountryAndDestinationCountryAndIdNot(
                        src, dest, id))
            throw new IllegalArgumentException(
                    "Corridor " + src + "→" + dest + " already exists");

        String oldCorridor = corridor.getSourceCountry()
                + "→" + corridor.getDestinationCountry();
        String oldSrcCurrency  = corridor.getSourceCurrency().getCode();
        String oldDestCurrency = corridor.getDestinationCurrency().getCode();

        Currency newSrcCurrency  = findCurrencyOrThrow(
                dto.getSourceCurrencyId());
        Currency newDestCurrency = findCurrencyOrThrow(
                dto.getDestinationCurrencyId());

        corridor.setSourceCountry(src);
        corridor.setDestinationCountry(dest);
        corridor.setSourceCurrency(newSrcCurrency);
        corridor.setDestinationCurrency(newDestCurrency);
        if (dto.getActive() != null)
            corridor.setActive(dto.getActive());

        Corridor updated = corridorRepository.save(corridor);

        auditService.log(
                SecurityUtils.getCurrentUsername(),
                "UPDATE_CORRIDOR",
                "Corridor",
                id,
                LocalDateTime.now() + " - Modification de corridor " + oldCorridor
                        + ", oldSrcCurrency=" + oldSrcCurrency
                        + ", oldDestCurrency=" + oldDestCurrency
                        + " -> new corridor : " + src + "→" + dest
                        + ", newSrcCurrency=" + newSrcCurrency.getCode()
                        + ", newDestCurrency=" + newDestCurrency.getCode()
        );

        return CorridorResponseDTO.fromEntity(updated);
    }

    @Override
    @Transactional
    public void toggle(Long id, String adminIp) {
        Corridor corridor = findOrThrow(id);
        boolean previous  = corridor.isActive();
        corridor.setActive(!previous);
        corridorRepository.save(corridor);

        String corridorOldStatus = previous ? "Désactivé" : "Activé";
        String corridorNewStatus = !previous ? "Désactivé" : "Activé";

        auditService.log(
                SecurityUtils.getCurrentUsername(),
                previous ? "DEACTIVATE_CORRIDOR" : "ACTIVATE_CORRIDOR",
                "Corridor",
                id,
                LocalDateTime.now() + " - Modification de status de " + corridorOldStatus
                        + " à " + corridorNewStatus
                        + " pour le corridor " + corridor.getSourceCountry()
                        + "→" + corridor.getDestinationCountry()
        );
    }


    private Corridor findOrThrow(Long id) {
        return corridorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Corridor not found with id: " + id));
    }

    private Currency findCurrencyOrThrow(Long id) {
        return currencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Currency not found with id: " + id));
    }
    @Override
    @Transactional(readOnly = true)
    public List<CurrencyResponseDTO> getActiveCurrencies() {
        return currencyRepository.findByActive(true).stream()
                .map(CurrencyResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CorridorStatsResponseDTO getStats(Long corridorId) {
        Corridor corridor = findOrThrow(corridorId);
        return buildStats(corridor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CorridorStatsResponseDTO> getAllStats() {
        return corridorRepository.findByActive(true)
                .stream()
                .map(this::buildStats)
                .collect(java.util.stream.Collectors.toList());
    }

    private CorridorStatsResponseDTO buildStats(Corridor corridor) {

        LocalDateTime startOfDay   = LocalDate.now().atStartOfDay();
        LocalDateTime startOfMonth =
                LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        // Récupérer tous les transferts de la période mensuelle
        List<Transfer> monthlyTransfers =
                transferRepository.findByCreatedAtBetween(
                                startOfMonth, now)
                        .stream()
                        .filter(t -> t.getAgency() != null
                                && matchesCorridor(t, corridor))
                        .collect(java.util.stream.Collectors.toList());

        // Filtrer ceux du jour
        List<Transfer> dailyTransfers = monthlyTransfers.stream()
                .filter(t -> t.getCreatedAt() != null
                        && !t.getCreatedAt().isBefore(startOfDay))
                .collect(java.util.stream.Collectors.toList());

        // Volumes
        BigDecimal dailyVolume = dailyTransfers.stream()
                .map(Transfer::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal monthlyVolume = monthlyTransfers.stream()
                .map(Transfer::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // CA : calculer les frais pour chaque transfert
        BigDecimal dailyRevenue   = BigDecimal.ZERO;
        BigDecimal monthlyRevenue = BigDecimal.ZERO;
        BigDecimal agencyCommission   = BigDecimal.ZERO;
        BigDecimal centralCommission  = BigDecimal.ZERO;

        for (Transfer t : monthlyTransfers) {
            // Trouver la grille tarifaire applicable
            Optional<FeeGrid> gridOpt = feeGridRepository
                    .findApplicable(corridor.getId(), t.getAmount());

            if (gridOpt.isPresent()) {
                FeeGrid grid = gridOpt.get();

                // Calculer les frais
                BigDecimal fee = grid.getFixedFee().add(
                        t.getAmount()
                                .multiply(grid.getPercentageFee())
                                .divide(BigDecimal.valueOf(100), 2,
                                        java.math.RoundingMode.HALF_UP)
                );

                monthlyRevenue = monthlyRevenue.add(fee);

                // Répartition
                BigDecimal agencyPart = fee
                        .multiply(grid.getAgencyShare())
                        .divide(BigDecimal.valueOf(100), 2,
                                java.math.RoundingMode.HALF_UP);
                BigDecimal centralPart = fee.subtract(agencyPart);

                agencyCommission  = agencyCommission.add(agencyPart);
                centralCommission = centralCommission.add(centralPart);

                // CA jour
                if (!t.getCreatedAt().isBefore(startOfDay)) {
                    dailyRevenue = dailyRevenue.add(fee);
                }
            }
        }

        String label = corridor.getSourceCountry()
                + " → " + corridor.getDestinationCountry();

        return CorridorStatsResponseDTO.builder()
                .corridorId(corridor.getId())
                .label(label)
                .sourceCountry(corridor.getSourceCountry())
                .destinationCountry(corridor.getDestinationCountry())
                .dailyVolume(dailyVolume)
                .monthlyVolume(monthlyVolume)
                .dailyCount(dailyTransfers.size())
                .monthlyCount(monthlyTransfers.size())
                .dailyRevenue(dailyRevenue)
                .monthlyRevenue(monthlyRevenue)
                .agencyCommission(agencyCommission)
                .centralCommission(centralCommission)
                .build();
    }


    private boolean matchesCorridor(Transfer t, Corridor corridor) {
        if (t.getCurrency() == null) return false;
        String transferCurrencySrc = t.getCurrency().getCode();
        String corridorCurrencySrc = corridor.getSourceCurrency().getCode();

        String transferCurrencyDest = t.getTargetCurrency().getCode();
        String corridorCurrencyDest = corridor.getDestinationCurrency().getCode();


        return (transferCurrencySrc.equals(corridorCurrencySrc)) && (transferCurrencyDest.equals(corridorCurrencyDest));
    }
}