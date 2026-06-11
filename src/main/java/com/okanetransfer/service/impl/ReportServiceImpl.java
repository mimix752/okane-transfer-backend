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

    // RAPPORT GLOBAL

    @Override
    @Transactional(readOnly = true)
    public ReportResponseDTO getGlobalReport(LocalDate from,
                                             LocalDate to) {
        List<Transfer> transfers =
                transferRepository.findByCreatedAtBetween(
                        from.atStartOfDay(),
                        to.atTime(LocalTime.MAX));
        return buildReport(from, to, transfers);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponseDTO getReportByCorridor(LocalDate from,
                                                 LocalDate to,
                                                 String corridor) {
        List<Transfer> transfers =
                transferRepository.findByCreatedAtBetween(
                                from.atStartOfDay(),
                                to.atTime(LocalTime.MAX))
                        .stream()
                        .filter(t -> t.getAgency() != null
                                && t.getAgency().getName()
                                .equalsIgnoreCase(corridor))
                        .collect(Collectors.toList());
        return buildReport(from, to, transfers);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponseDTO getReportByStatus(LocalDate from,
                                               LocalDate to,
                                               String status) {
        List<Transfer> transfers =
                transferRepository.findByCreatedAtBetween(
                                from.atStartOfDay(),
                                to.atTime(LocalTime.MAX))
                        .stream()
                        .filter(t -> t.getStatus().name()
                                .equalsIgnoreCase(status))
                        .collect(Collectors.toList());
        return buildReport(from, to, transfers);
    }

    // ════════════════════════════════════════════════════════
    // STATS CORRIDOR — VOLUME JOURNALIER + MENSUEL
    // ════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public CorridorStatsResponseDTO getCorridorStats(Long corridorId) {
        Corridor corridor = corridorRepository.findById(corridorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Corridor not found: " + corridorId));
        return buildCorridorStats(corridor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CorridorStatsResponseDTO> getAllCorridorStats() {
        return corridorRepository.findByActive(true)
                .stream()
                .map(this::buildCorridorStats)
                .sorted((a, b) -> b.getMonthlyVolume()
                        .compareTo(a.getMonthlyVolume()))
                .collect(Collectors.toList());
    }

    // MÉTHODE CENTRALE : calcul stats journalier + mensuel

    private CorridorStatsResponseDTO buildCorridorStats(
            Corridor corridor) {

        // Bornes de temps
        LocalDateTime startOfDay =
                LocalDate.now().atStartOfDay();
        LocalDateTime startOfMonth =
                LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        // Tous les transferts du mois pour ce corridor
        List<Transfer> monthlyTransfers =
                transferRepository.findByCreatedAtBetween(
                                startOfMonth, now)
                        .stream()
                        .filter(t -> transferMatchesCorridor(
                                t, corridor))
                        .collect(Collectors.toList());

        // Filtrer uniquement ceux du jour
        List<Transfer> dailyTransfers = monthlyTransfers.stream()
                .filter(t -> t.getCreatedAt() != null
                        && !t.getCreatedAt().isBefore(startOfDay))
                .collect(Collectors.toList());

        // Volumes
        BigDecimal dailyVolume = sumAmounts(dailyTransfers);
        BigDecimal monthlyVolume = sumAmounts(monthlyTransfers);

        // CA et commissions
        BigDecimal dailyRevenue      = BigDecimal.ZERO;
        BigDecimal monthlyRevenue    = BigDecimal.ZERO;
        BigDecimal agencyCommission  = BigDecimal.ZERO;
        BigDecimal centralCommission = BigDecimal.ZERO;

        for (Transfer t : monthlyTransfers) {
            BigDecimal fee = computeFeeForTransfer(
                    t, corridor.getId());
            monthlyRevenue = monthlyRevenue.add(fee);

            // Trouver la grille pour calculer la répartition
            Optional<FeeGrid> gridOpt =
                    feeGridRepository.findApplicable(
                            corridor.getId(), t.getAmount());

            if (gridOpt.isPresent()) {
                FeeGrid grid = gridOpt.get();

                BigDecimal agencyPart = fee
                        .multiply(grid.getAgencyShare())
                        .divide(BigDecimal.valueOf(100), 2,
                                RoundingMode.HALF_UP);

                agencyCommission  =
                        agencyCommission.add(agencyPart);
                centralCommission =
                        centralCommission.add(
                                fee.subtract(agencyPart));
            }

            // CA journalier
            if (t.getCreatedAt() != null
                    && !t.getCreatedAt().isBefore(startOfDay)) {
                dailyRevenue = dailyRevenue.add(fee);
            }
        }

        String label = corridor.getSourceCountry()
                + " → "
                + corridor.getDestinationCountry();

        return CorridorStatsResponseDTO.builder()
                .corridorId(corridor.getId())
                .label(label)
                .sourceCountry(corridor.getSourceCountry())
                .destinationCountry(
                        corridor.getDestinationCountry())
                // Journalier
                .dailyVolume(dailyVolume)
                .dailyCount(dailyTransfers.size())
                .dailyRevenue(dailyRevenue)
                // Mensuel
                .monthlyVolume(monthlyVolume)
                .monthlyCount(monthlyTransfers.size())
                .monthlyRevenue(monthlyRevenue)
                // Commissions
                .agencyCommission(agencyCommission)
                .centralCommission(centralCommission)
                .build();
    }

    // CONSTRUCTION DU RAPPORT GÉNÉRIQUE

    private ReportResponseDTO buildReport(LocalDate from,
                                          LocalDate to,
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
        int        total       = transfers.size();

        BigDecimal average = totalVolume.divide(
                BigDecimal.valueOf(total), 2,
                RoundingMode.HALF_UP);

        // Par statut
        Map<String, Integer> byStatus = new HashMap<>();
        for (Transfer t : transfers) {
            String key = t.getStatus().name();
            byStatus.put(key,
                    byStatus.getOrDefault(key, 0) + 1);
        }

        // Par devise
        Map<String, BigDecimal> byCurrency = new HashMap<>();
        for (Transfer t : transfers) {
            if (t.getCurrency() != null) {
                String key = t.getCurrency().getName();
                byCurrency.put(key,
                        byCurrency.getOrDefault(key, BigDecimal.ZERO)
                                .add(t.getAmount()));
            }
        }

        // Par agence
        Map<Long, List<Transfer>> byAgencyMap = new HashMap<>();
        for (Transfer t : transfers) {
            if (t.getAgency() != null) {
                byAgencyMap
                        .computeIfAbsent(t.getAgency().getId(),
                                k -> new ArrayList<>())
                        .add(t);
            }
        }

        List<ReportResponseDTO.AgencyReportLine> byAgency =
                new ArrayList<>();
        for (Map.Entry<Long, List<Transfer>> e
                : byAgencyMap.entrySet()) {
            Transfer first = e.getValue().get(0);
            BigDecimal vol = sumAmounts(e.getValue());
            byAgency.add(
                    ReportResponseDTO.AgencyReportLine.builder()
                            .agencyId(e.getKey())
                            .agencyName(first.getAgency().getName())
                            .country(first.getAgency().getCountry())
                            .volume(vol)
                            .fees(BigDecimal.ZERO)
                            .transferCount(e.getValue().size())
                            .build()
            );
        }

        byAgency.sort((a, b) ->
                b.getVolume().compareTo(a.getVolume()));

        // Par devise → lignes corridor
        List<ReportResponseDTO.CorridorReportLine> byCorridorLines =
                new ArrayList<>();
        for (Map.Entry<String, BigDecimal> e
                : byCurrency.entrySet()) {
            String code = e.getKey();
            List<Transfer> ct = transfers.stream()
                    .filter(t -> t.getCurrency() != null
                            && t.getCurrency().getName().equals(code))
                    .collect(Collectors.toList());

            String target = ct.get(0).getTargetCurrency() != null
                    ? ct.get(0).getTargetCurrency().getName()
                    : "N/A";

            byCorridorLines.add(
                    ReportResponseDTO.CorridorReportLine.builder()
                            .sourceCountry(code)
                            .destinationCountry(target)
                            .label(code + " → " + target)
                            .volume(e.getValue())
                            .fees(BigDecimal.ZERO)
                            .transferCount(ct.size())
                            .build()
            );
        }

        return ReportResponseDTO.builder()
                .from(from).to(to)
                .totalVolume(totalVolume)
                .totalFees(BigDecimal.ZERO)
                .totalTransfers(total)
                .averageTransfer(average)
                .transfersByStatus(byStatus)
                .volumeByCurrency(byCurrency)
                .byAgency(byAgency)
                .byCorridor(byCorridorLines)
                .build();
    }

    // MÉTHODES PRIVÉES UTILITAIRES


    /** Somme les montants d'une liste de transferts */
    private BigDecimal sumAmounts(List<Transfer> transfers) {
        return transfers.stream()
                .map(Transfer::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Vérifie si un transfert appartient à un corridor.
     * On compare la devise du transfert avec la devise
     * source du corridor.
     */
    private boolean transferMatchesCorridor(Transfer t,
                                            Corridor corridor) {
        if (t.getCurrency() == null) return false;
        String transferCurrency = t.getCurrency().getName();
        String corridorFrom =
                corridor.getSourceCurrency().getCode();
        return transferCurrency.equalsIgnoreCase(corridorFrom);
    }

    /**
     * Calcule les frais d'un transfert selon la grille tarifaire.
     * Retourne ZERO si aucune grille applicable.
     *
     * Formule : fixedFee + (amount × percentageFee / 100)
     */
    private BigDecimal computeFeeForTransfer(Transfer t,
                                             Long corridorId) {
        if (t.getAmount() == null) return BigDecimal.ZERO;

        Optional<FeeGrid> gridOpt =
                feeGridRepository.findApplicable(
                        corridorId, t.getAmount());

        if (gridOpt.isEmpty()) return BigDecimal.ZERO;

        FeeGrid grid = gridOpt.get();

        BigDecimal percentPart = t.getAmount()
                .multiply(grid.getPercentageFee())
                .divide(BigDecimal.valueOf(100), 2,
                        RoundingMode.HALF_UP);

        return grid.getFixedFee()
                .add(percentPart)
                .setScale(2, RoundingMode.HALF_UP);
    }
}