package com.okanetransfer.service.impl;

import com.okanetransfer.dto.response.ReportResponseDTO;
import com.okanetransfer.entity.Transfer;
import com.okanetransfer.repository.TransferRepository;
import com.okanetransfer.service.ReportService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final TransferRepository transferRepository;

    // ── Rapport global ───────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ReportResponseDTO getGlobalReport(LocalDate from,
                                             LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end   = to.atTime(LocalTime.MAX);

        List<Transfer> transfers =
                transferRepository.findByCreatedAtBetween(
                        start, end);

        return buildReport(from, to, transfers);
    }

    // ── Rapport par corridor ─────────────────────────────────
    // Format corridor attendu : nom d'agence
    // Ex: "Agence Casablanca"
    // Puisque Transfer n'a qu'une seule agence (sendingAgency),
    // on filtre par nom d'agence

    @Override
    @Transactional(readOnly = true)
    public ReportResponseDTO getReportByCorridor(LocalDate from,
                                                 LocalDate to,
                                                 String corridor) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end   = to.atTime(LocalTime.MAX);

        List<Transfer> transfers =
                transferRepository.findByCreatedAtBetween(start, end)
                        .stream()
                        .filter(t -> t.getAgency() != null
                                && t.getAgency().getName()
                                .equalsIgnoreCase(corridor))
                        .collect(Collectors.toList());

        return buildReport(from, to, transfers);
    }

    // ── Rapport par statut ───────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ReportResponseDTO getReportByStatus(LocalDate from,
                                               LocalDate to,
                                               String status) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end   = to.atTime(LocalTime.MAX);

        List<Transfer> transfers =
                transferRepository.findByCreatedAtBetween(start, end)
                        .stream()
                        .filter(t -> t.getStatus().name()
                                .equalsIgnoreCase(status))
                        .collect(Collectors.toList());

        return buildReport(from, to, transfers);
    }

    // ── Construction du rapport ──────────────────────────────

    private ReportResponseDTO buildReport(LocalDate from,
                                          LocalDate to,
                                          List<Transfer> transfers) {

        // Cas liste vide
        if (transfers.isEmpty()) {
            return ReportResponseDTO.builder()
                    .from(from)
                    .to(to)
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

        BigDecimal totalVolume = transfers.stream()
                .map(Transfer::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalFees = BigDecimal.ZERO;

        int totalTransfers = transfers.size();

        BigDecimal averageTransfer = totalVolume
                .divide(BigDecimal.valueOf(totalTransfers),
                        2, RoundingMode.HALF_UP);


        Map<String, Integer> byStatus = transfers.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getStatus().name(),
                        Collectors.collectingAndThen(
                                Collectors.counting(),
                                Long::intValue
                        )
                ));

        Map<String, BigDecimal> byCurrency = transfers.stream()
                .filter(t -> t.getCurrency() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getCurrency().name(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Transfer::getAmount,
                                BigDecimal::add
                        )
                ));

        Map<Long, List<Transfer>> groupedByAgency = transfers
                .stream()
                .filter(t -> t.getAgency() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getAgency().getId()
                ));

        List<ReportResponseDTO.AgencyReportLine> byAgency =
                groupedByAgency.entrySet().stream()
                        .map(entry -> {
                            List<Transfer> agencyTransfers =
                                    entry.getValue();
                            Transfer first = agencyTransfers.get(0);

                            BigDecimal vol = agencyTransfers.stream()
                                    .map(Transfer::getAmount)
                                    .reduce(BigDecimal.ZERO,
                                            BigDecimal::add);

                            return ReportResponseDTO.AgencyReportLine
                                    .builder()
                                    .agencyId(entry.getKey())
                                    .agencyName(
                                            first.getAgency().getName())
                                    .country(
                                            first.getAgency().getCountry())
                                    .volume(vol)
                                    // fees non disponible pour l'instant
                                    .fees(BigDecimal.ZERO)
                                    .transferCount(agencyTransfers.size())
                                    .build();
                        })
                        .sorted(Comparator.comparing(
                                ReportResponseDTO.AgencyReportLine
                                        ::getVolume).reversed())
                        .collect(Collectors.toList());

        // 8. Détail par devise (remplace byCorridor
        //    car ton Transfer n'a pas receivingAgency)
        List<ReportResponseDTO.CorridorReportLine> byCorridor =
                byCurrency.entrySet().stream()
                        .map(entry -> {
                            String currencyCode = entry.getKey();
                            List<Transfer> ct = transfers.stream()
                                    .filter(t -> t.getCurrency() != null
                                            && t.getCurrency().name()
                                            .equals(currencyCode))
                                    .collect(Collectors.toList());

                            return ReportResponseDTO.CorridorReportLine
                                    .builder()
                                    .sourceCountry(currencyCode)
                                    .destinationCountry(
                                            // targetCurrency si disponible
                                            ct.get(0).getTargetCurrency()
                                                    != null
                                                    ? ct.get(0).getTargetCurrency()
                                                    .name()
                                                    : "N/A"
                                    )
                                    .label(currencyCode + " → "
                                            + (ct.get(0).getTargetCurrency()
                                            != null
                                            ? ct.get(0)
                                            .getTargetCurrency()
                                            .name()
                                            : "N/A"))
                                    .volume(entry.getValue())
                                    .fees(BigDecimal.ZERO)
                                    .transferCount(ct.size())
                                    .build();
                        })
                        .collect(Collectors.toList());

        // 9. Assembler et retourner
        return ReportResponseDTO.builder()
                .from(from)
                .to(to)
                .totalVolume(totalVolume)
                .totalFees(totalFees)
                .totalTransfers(totalTransfers)
                .averageTransfer(averageTransfer)
                .transfersByStatus(byStatus)
                .volumeByCurrency(byCurrency)
                .byAgency(byAgency)
                .byCorridor(byCorridor)
                .build();
    }
}