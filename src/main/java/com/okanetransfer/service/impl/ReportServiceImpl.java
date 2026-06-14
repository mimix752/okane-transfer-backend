package com.okanetransfer.service.impl;

import com.okanetransfer.dto.response.CorridorStatsResponseDTO;
import com.okanetransfer.dto.response.ReportResponseDTO;
import com.okanetransfer.entity.Corridor;
import com.okanetransfer.entity.FeeGrid;
import com.okanetransfer.entity.Transfer;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.CorridorRepository;
import com.okanetransfer.repository.FeeGridRepository;
import com.okanetransfer.repository.TransferRepository;
import com.okanetransfer.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private final TransferRepository transferRepository;
    private final CorridorRepository corridorRepository;
    private final FeeGridRepository  feeGridRepository;

    public ReportServiceImpl(TransferRepository transferRepository,
                             CorridorRepository corridorRepository,
                             FeeGridRepository feeGridRepository) {
        this.transferRepository = transferRepository;
        this.corridorRepository = corridorRepository;
        this.feeGridRepository  = feeGridRepository;
    }

    // ════════════════════════════════════════
    // RAPPORT GLOBAL
    // ════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public ReportResponseDTO getGlobalReport(LocalDate from, LocalDate to) {
        List<Transfer> transfers = transferRepository.findByCreatedAtBetween(
                from.atStartOfDay(), to.atTime(LocalTime.MAX));
        return buildReport(from, to, transfers);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponseDTO getReportByCorridor(LocalDate from, LocalDate to,
                                                 String corridor) {
        List<Transfer> transfers = transferRepository
                .findByCreatedAtBetween(from.atStartOfDay(), to.atTime(LocalTime.MAX))
                .stream()
                .filter(t -> t.getAgency() != null
                        && t.getAgency().getName().equalsIgnoreCase(corridor))
                .collect(Collectors.toList());
        return buildReport(from, to, transfers);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponseDTO getReportByStatus(LocalDate from, LocalDate to,
                                               String status) {
        List<Transfer> transfers = transferRepository
                .findByCreatedAtBetween(from.atStartOfDay(), to.atTime(LocalTime.MAX))
                .stream()
                .filter(t -> t.getStatus().name().equalsIgnoreCase(status))
                .collect(Collectors.toList());
        return buildReport(from, to, transfers);
    }

    // ════════════════════════════════════════
    // STATS CORRIDOR — avec période from/to
    // ════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public CorridorStatsResponseDTO getCorridorStats(Long corridorId,
                                                     LocalDate from, LocalDate to) {
        Corridor corridor = corridorRepository.findById(corridorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Corridor not found: " + corridorId));
        return buildCorridorStats(corridor, from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CorridorStatsResponseDTO> getAllCorridorStats(LocalDate from,
                                                              LocalDate to) {
        return corridorRepository.findByActive(true)
                .stream()
                .map(c -> buildCorridorStats(c, from, to))
                .sorted((a, b) -> b.getMonthlyVolume().compareTo(a.getMonthlyVolume()))
                .collect(Collectors.toList());
    }

    // ════════════════════════════════════════════════════════════════
    // MÉTHODE CENTRALE : stats journalier + période par corridor
    // ════════════════════════════════════════════════════════════════

    private CorridorStatsResponseDTO buildCorridorStats(Corridor corridor,
                                                        LocalDate from,
                                                        LocalDate to) {
        LocalDateTime start      = from.atStartOfDay();
        LocalDateTime end        = to.atTime(LocalTime.MAX);
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        // Transferts sur la période filtrée pour ce corridor
        List<Transfer> periodTransfers = transferRepository
                .findByCreatedAtBetween(start, end)
                .stream()
                .filter(t -> transferMatchesCorridor(t, corridor))
                .collect(Collectors.toList());

        // Journalier = ceux créés aujourd'hui dans cette liste
        List<Transfer> dailyTransfers = periodTransfers.stream()
                .filter(t -> t.getCreatedAt() != null
                        && !t.getCreatedAt().isBefore(startOfDay))
                .collect(Collectors.toList());

        // Volumes — somme des montants
        BigDecimal dailyVolume   = sumAmounts(dailyTransfers);
        BigDecimal periodVolume  = sumAmounts(periodTransfers);

        // Revenus — somme des fees déjà persistés sur le transfert
        BigDecimal dailyRevenue  = sumFees(dailyTransfers);
        BigDecimal periodRevenue = sumFees(periodTransfers);

        // Commissions agence / centrale via FeeGrid (agencyShare est sur la grille)
        BigDecimal agencyCommission  = BigDecimal.ZERO;
        BigDecimal centralCommission = BigDecimal.ZERO;

        for (Transfer t : periodTransfers) {
            BigDecimal fee = t.getFees() != null ? t.getFees() : BigDecimal.ZERO;
            if (fee.compareTo(BigDecimal.ZERO) == 0) continue;

            Optional<FeeGrid> gridOpt = feeGridRepository
                    .findApplicable(corridor.getId(), t.getAmount());

            if (gridOpt.isPresent()) {
                BigDecimal agencyPart = fee
                        .multiply(gridOpt.get().getAgencyShare())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                agencyCommission  = agencyCommission.add(agencyPart);
                centralCommission = centralCommission.add(fee.subtract(agencyPart));
            } else {
                // Pas de grille : on attribue tout à la centrale
                centralCommission = centralCommission.add(fee);
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
                .dailyCount(dailyTransfers.size())
                .dailyRevenue(dailyRevenue)
                .monthlyVolume(periodVolume)
                .monthlyCount(periodTransfers.size())
                .monthlyRevenue(periodRevenue)
                .agencyCommission(agencyCommission)
                .centralCommission(centralCommission)
                .build();
    }

    // ════════════════════════════════════════
    // CONSTRUCTION DU RAPPORT GÉNÉRIQUE
    // ════════════════════════════════════════

    private ReportResponseDTO buildReport(LocalDate from, LocalDate to,
                                          List<Transfer> transfers) {
        if (transfers.isEmpty()) {
            return ReportResponseDTO.builder()
                    .from(from).to(to)
                    .totalVolume(BigDecimal.ZERO)
                    .totalFees(BigDecimal.ZERO)
                    .totalTransfers(0)
                    .averageTransfer(BigDecimal.ZERO)
                    .transfersByStatus(new HashMap<>())
                    .volumeByCurrency(new HashMap<>())
                    .byAgency(new ArrayList<>())
                    .byCorridor(new ArrayList<>())
                    .build();
        }

        BigDecimal totalVolume = sumAmounts(transfers);
        BigDecimal totalFees   = sumFees(transfers);   // ← direct depuis t.getFees()
        int        total       = transfers.size();
        BigDecimal average     = totalVolume.divide(
                BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);

        // ── Par statut ──────────────────────────────────────────
        Map<String, Integer> byStatus = new HashMap<>();
        for (Transfer t : transfers) {
            String key = t.getStatus().name();
            byStatus.put(key, byStatus.getOrDefault(key, 0) + 1);
        }

        // ── Par devise ──────────────────────────────────────────
        Map<String, BigDecimal> byCurrency = new HashMap<>();
        for (Transfer t : transfers) {
            if (t.getCurrency() != null) {
                String key = t.getCurrency().getName();
                byCurrency.put(key,
                        byCurrency.getOrDefault(key, BigDecimal.ZERO)
                                .add(t.getAmount()));
            }
        }

        // ── Par agence ──────────────────────────────────────────
        Map<Long, List<Transfer>> byAgencyMap = new HashMap<>();
        for (Transfer t : transfers) {
            if (t.getAgency() != null) {
                byAgencyMap.computeIfAbsent(t.getAgency().getId(),
                                k -> new ArrayList<>())
                        .add(t);
            }
        }

        List<ReportResponseDTO.AgencyReportLine> byAgency = new ArrayList<>();
        for (Map.Entry<Long, List<Transfer>> e : byAgencyMap.entrySet()) {
            Transfer   first  = e.getValue().get(0);
            BigDecimal vol    = sumAmounts(e.getValue());
            BigDecimal agFees = sumFees(e.getValue());  // ← direct depuis t.getFees()

            byAgency.add(ReportResponseDTO.AgencyReportLine.builder()
                    .agencyId(e.getKey())
                    .agencyName(first.getAgency().getName())
                    .country(first.getAgency().getCountry())
                    .volume(vol)
                    .fees(agFees)
                    .transferCount(e.getValue().size())
                    .build());
        }
        byAgency.sort((a, b) -> b.getVolume().compareTo(a.getVolume()));

        // ── Par corridor ─────────────────────────────────────────
        List<Corridor> allCorridors = corridorRepository.findByActive(true);
        List<ReportResponseDTO.CorridorReportLine> byCorridorLines = new ArrayList<>();

        for (Corridor corridor : allCorridors) {
            List<Transfer> ct = transfers.stream()
                    .filter(t -> transferMatchesCorridor(t, corridor))
                    .collect(Collectors.toList());

            if (ct.isEmpty()) continue;

            BigDecimal vol      = sumAmounts(ct);
            BigDecimal corrFees = sumFees(ct);          // ← direct depuis t.getFees()

            String label = corridor.getSourceCountry()
                    + " → " + corridor.getDestinationCountry();

            byCorridorLines.add(ReportResponseDTO.CorridorReportLine.builder()
                    .sourceCountry(corridor.getSourceCountry())
                    .destinationCountry(corridor.getDestinationCountry())
                    .label(label)
                    .volume(vol)
                    .fees(corrFees)
                    .transferCount(ct.size())
                    .build());
        }
        byCorridorLines.sort((a, b) -> b.getVolume().compareTo(a.getVolume()));

        return ReportResponseDTO.builder()
                .from(from).to(to)
                .totalVolume(totalVolume)
                .totalFees(totalFees)
                .totalTransfers(total)
                .averageTransfer(average)
                .transfersByStatus(byStatus)
                .volumeByCurrency(byCurrency)
                .byAgency(byAgency)
                .byCorridor(byCorridorLines)
                .build();
    }

    // ════════════════════════════════════════
    // UTILITAIRES PRIVÉS
    // ════════════════════════════════════════

    /** Somme les montants d'une liste de transferts. */
    private BigDecimal sumAmounts(List<Transfer> transfers) {
        return transfers.stream()
                .map(Transfer::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Somme les frais déjà persistés sur chaque transfert. */
    private BigDecimal sumFees(List<Transfer> transfers) {
        return transfers.stream()
                .map(Transfer::getFees)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Un transfert appartient à un corridor si
     * senderCountry == sourceCountry ET recipientCountry == destinationCountry.
     */
    private boolean transferMatchesCorridor(Transfer t, Corridor corridor) {
        if (corridor.getSourceCountry() == null
                || corridor.getDestinationCountry() == null) return false;
        if (t.getSenderCountry() == null
                || t.getRecipientCountry() == null) return false;

        return t.getSenderCountry()
                .equalsIgnoreCase(corridor.getSourceCountry())
                && t.getRecipientCountry()
                .equalsIgnoreCase(corridor.getDestinationCountry());
    }
}