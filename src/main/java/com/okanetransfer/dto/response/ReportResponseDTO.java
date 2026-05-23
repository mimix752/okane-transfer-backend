package com.okanetransfer.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDTO {
    private LocalDate from;
    private LocalDate to;
    private BigDecimal totalVolume;
    private BigDecimal totalFees;
    private int        totalTransfers;
    private BigDecimal averageTransfer;
    private Map<String, Integer> transfersByStatus;
    private Map<String, BigDecimal> volumeByCurrency;
    private List<AgencyReportLine> byAgency;
    private List<CorridorReportLine> byCorridor;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgencyReportLine {
        private Long       agencyId;
        private String     agencyName;
        private String     country;
        private BigDecimal volume;
        private BigDecimal fees;
        private int        transferCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CorridorReportLine {
        private String     sourceCountry;
        private String     destinationCountry;
        private String     label;
        private BigDecimal volume;
        private BigDecimal fees;
        private int        transferCount;
    }
}