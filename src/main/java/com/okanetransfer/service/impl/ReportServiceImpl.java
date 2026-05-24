package com.okanetransfer.service.impl;

import com.okanetransfer.dto.response.ReportResponseDTO;
import com.okanetransfer.entity.Transfer;
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

    public ReportServiceImpl(TransferRepository transferRepository) {
        this.transferRepository = transferRepository;
    }

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

        BigDecimal totalVolume = transfers.stream()
                .map(Transfer::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalFees = BigDecimal.ZERO;

        int totalTransfers = transfers.size();

        BigDecimal averageTransfer = totalVolume.divide(
                BigDecimal.valueOf(totalTransfers), 2,
                RoundingMode.HALF_UP);

        Map<String, Integer> byStatus = new HashMap<>();
        for (Transfer t : transfers) {
            String key = t.getStatus().name();
            byStatus.put(key, byStatus.getOrDefault(key, 0) + 1);
        }

        Map<String, BigDecimal> byCurrency = new HashMap<>();
        for (Transfer t : transfers) {
            if (t.getCurrency() != null) {
                String key = t.getCurrency().name();
                byCurrency.put(key,
                        byCurrency.getOrDefault(key, BigDecimal.ZERO)
                                .add(t.getAmount()));
            }
        }

        Map<Long, List<Transfer>> groupedByAgency = new HashMap<>();
        for (Transfer t : transfers) {
            if (t.getAgency() != null) {
                Long agencyId = t.getAgency().getId();
                groupedByAgency
                        .computeIfAbsent(agencyId,
                                k -> new ArrayList<>())
                        .add(t);
            }
        }

        List<ReportResponseDTO.AgencyReportLine> byAgency =
                new ArrayList<>();
        for (Map.Entry<Long, List<Transfer>> entry
                : groupedByAgency.entrySet()) {
            List<Transfer> ag = entry.getValue();
            Transfer first    = ag.get(0);

            BigDecimal vol = ag.stream()
                    .map(Transfer::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            byAgency.add(
                    ReportResponseDTO.AgencyReportLine.builder()
                            .agencyId(entry.getKey())
                            .agencyName(first.getAgency().getName())
                            .country(first.getAgency().getCountry())
                            .volume(vol)
                            .fees(BigDecimal.ZERO)
                            .transferCount(ag.size())
                            .build()
            );
        }

        byAgency.sort((a, b) ->
                b.getVolume().compareTo(a.getVolume()));

        List<ReportResponseDTO.CorridorReportLine> byCorridor =
                new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry
                : byCurrency.entrySet()) {

            String currencyCode = entry.getKey();

            List<Transfer> ct = transfers.stream()
                    .filter(t -> t.getCurrency() != null
                            && t.getCurrency().name()
                            .equals(currencyCode))
                    .collect(Collectors.toList());

            String targetCurrencyName = "N/A";
            if (!ct.isEmpty()
                    && ct.get(0).getTargetCurrency() != null) {
                targetCurrencyName =
                        ct.get(0).getTargetCurrency().name();
            }

            byCorridor.add(
                    ReportResponseDTO.CorridorReportLine.builder()
                            .sourceCountry(currencyCode)
                            .destinationCountry(targetCurrencyName)
                            .label(currencyCode + " → " + targetCurrencyName)
                            .volume(entry.getValue())
                            .fees(BigDecimal.ZERO)
                            .transferCount(ct.size())
                            .build()
            );
        }

        return ReportResponseDTO.builder()
                .from(from).to(to)
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

    private boolean matchesCorridor(Transfer t, String corridor) {
        if (t.getAgency() == null) return false;
        return t.getAgency().getName()
                .equalsIgnoreCase(corridor);
    }
}