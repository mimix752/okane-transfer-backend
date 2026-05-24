package com.okanetransfer.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ReportResponseDTO {

    private LocalDate              from;
    private LocalDate              to;
    private BigDecimal             totalVolume;
    private BigDecimal             totalFees;
    private int                    totalTransfers;
    private BigDecimal             averageTransfer;
    private Map<String, Integer>   transfersByStatus;
    private Map<String, BigDecimal> volumeByCurrency;
    private List<AgencyReportLine>  byAgency;
    private List<CorridorReportLine> byCorridor;

    public static class AgencyReportLine {
        private Long       agencyId;
        private String     agencyName;
        private String     country;
        private BigDecimal volume;
        private BigDecimal fees;
        private int        transferCount;

        public AgencyReportLine() {}

        public AgencyReportLine(Long agencyId, String agencyName,
                                String country, BigDecimal volume,
                                BigDecimal fees,
                                int transferCount) {
            this.agencyId      = agencyId;
            this.agencyName    = agencyName;
            this.country       = country;
            this.volume        = volume;
            this.fees          = fees;
            this.transferCount = transferCount;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long       agencyId;
            private String     agencyName;
            private String     country;
            private BigDecimal volume;
            private BigDecimal fees;
            private int        transferCount;

            public Builder agencyId(Long agencyId)
            { this.agencyId = agencyId; return this; }
            public Builder agencyName(String agencyName)
            { this.agencyName = agencyName; return this; }
            public Builder country(String country)
            { this.country = country; return this; }
            public Builder volume(BigDecimal volume)
            { this.volume = volume; return this; }
            public Builder fees(BigDecimal fees)
            { this.fees = fees; return this; }
            public Builder transferCount(int transferCount)
            { this.transferCount = transferCount; return this; }

            public AgencyReportLine build() {
                AgencyReportLine l = new AgencyReportLine();
                l.agencyId      = this.agencyId;
                l.agencyName    = this.agencyName;
                l.country       = this.country;
                l.volume        = this.volume;
                l.fees          = this.fees;
                l.transferCount = this.transferCount;
                return l;
            }
        }

        public Long getAgencyId() { return agencyId; }
        public void setAgencyId(Long agencyId)
        { this.agencyId = agencyId; }
        public String getAgencyName() { return agencyName; }
        public void setAgencyName(String agencyName)
        { this.agencyName = agencyName; }
        public String getCountry() { return country; }
        public void setCountry(String country)
        { this.country = country; }
        public BigDecimal getVolume() { return volume; }
        public void setVolume(BigDecimal volume)
        { this.volume = volume; }
        public BigDecimal getFees() { return fees; }
        public void setFees(BigDecimal fees) { this.fees = fees; }
        public int getTransferCount() { return transferCount; }
        public void setTransferCount(int transferCount)
        { this.transferCount = transferCount; }
    }

    public static class CorridorReportLine {
        private String     sourceCountry;
        private String     destinationCountry;
        private String     label;
        private BigDecimal volume;
        private BigDecimal fees;
        private int        transferCount;

        public CorridorReportLine() {}

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String     sourceCountry;
            private String     destinationCountry;
            private String     label;
            private BigDecimal volume;
            private BigDecimal fees;
            private int        transferCount;

            public Builder sourceCountry(String sourceCountry)
            { this.sourceCountry = sourceCountry; return this; }
            public Builder destinationCountry(
                    String destinationCountry)
            { this.destinationCountry = destinationCountry;
                return this; }
            public Builder label(String label)
            { this.label = label; return this; }
            public Builder volume(BigDecimal volume)
            { this.volume = volume; return this; }
            public Builder fees(BigDecimal fees)
            { this.fees = fees; return this; }
            public Builder transferCount(int transferCount)
            { this.transferCount = transferCount; return this; }

            public CorridorReportLine build() {
                CorridorReportLine l = new CorridorReportLine();
                l.sourceCountry      = this.sourceCountry;
                l.destinationCountry = this.destinationCountry;
                l.label              = this.label;
                l.volume             = this.volume;
                l.fees               = this.fees;
                l.transferCount      = this.transferCount;
                return l;
            }
        }

        public String getSourceCountry() { return sourceCountry; }
        public void setSourceCountry(String sourceCountry)
        { this.sourceCountry = sourceCountry; }
        public String getDestinationCountry()
        { return destinationCountry; }
        public void setDestinationCountry(String destinationCountry)
        { this.destinationCountry = destinationCountry; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public BigDecimal getVolume() { return volume; }
        public void setVolume(BigDecimal volume)
        { this.volume = volume; }
        public BigDecimal getFees() { return fees; }
        public void setFees(BigDecimal fees) { this.fees = fees; }
        public int getTransferCount() { return transferCount; }
        public void setTransferCount(int transferCount)
        { this.transferCount = transferCount; }
    }

    public ReportResponseDTO() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private LocalDate              from;
        private LocalDate              to;
        private BigDecimal             totalVolume;
        private BigDecimal             totalFees;
        private int                    totalTransfers;
        private BigDecimal             averageTransfer;
        private Map<String, Integer>   transfersByStatus;
        private Map<String, BigDecimal> volumeByCurrency;
        private List<AgencyReportLine>  byAgency;
        private List<CorridorReportLine> byCorridor;

        public Builder from(LocalDate from)
        { this.from = from; return this; }
        public Builder to(LocalDate to)
        { this.to = to; return this; }
        public Builder totalVolume(BigDecimal totalVolume)
        { this.totalVolume = totalVolume; return this; }
        public Builder totalFees(BigDecimal totalFees)
        { this.totalFees = totalFees; return this; }
        public Builder totalTransfers(int totalTransfers)
        { this.totalTransfers = totalTransfers; return this; }
        public Builder averageTransfer(BigDecimal averageTransfer)
        { this.averageTransfer = averageTransfer; return this; }
        public Builder transfersByStatus(
                Map<String, Integer> transfersByStatus)
        { this.transfersByStatus = transfersByStatus;
            return this; }
        public Builder volumeByCurrency(
                Map<String, BigDecimal> volumeByCurrency)
        { this.volumeByCurrency = volumeByCurrency;
            return this; }
        public Builder byAgency(List<AgencyReportLine> byAgency)
        { this.byAgency = byAgency; return this; }
        public Builder byCorridor(
                List<CorridorReportLine> byCorridor)
        { this.byCorridor = byCorridor; return this; }

        public ReportResponseDTO build() {
            ReportResponseDTO d = new ReportResponseDTO();
            d.from              = this.from;
            d.to                = this.to;
            d.totalVolume       = this.totalVolume;
            d.totalFees         = this.totalFees;
            d.totalTransfers    = this.totalTransfers;
            d.averageTransfer   = this.averageTransfer;
            d.transfersByStatus = this.transfersByStatus;
            d.volumeByCurrency  = this.volumeByCurrency;
            d.byAgency          = this.byAgency;
            d.byCorridor        = this.byCorridor;
            return d;
        }
    }

    // Getters / Setters
    public LocalDate getFrom() { return from; }
    public void setFrom(LocalDate from) { this.from = from; }

    public LocalDate getTo() { return to; }
    public void setTo(LocalDate to) { this.to = to; }

    public BigDecimal getTotalVolume() { return totalVolume; }
    public void setTotalVolume(BigDecimal totalVolume)
    { this.totalVolume = totalVolume; }

    public BigDecimal getTotalFees() { return totalFees; }
    public void setTotalFees(BigDecimal totalFees)
    { this.totalFees = totalFees; }

    public int getTotalTransfers() { return totalTransfers; }
    public void setTotalTransfers(int totalTransfers)
    { this.totalTransfers = totalTransfers; }

    public BigDecimal getAverageTransfer() { return averageTransfer; }
    public void setAverageTransfer(BigDecimal averageTransfer)
    { this.averageTransfer = averageTransfer; }

    public Map<String, Integer> getTransfersByStatus()
    { return transfersByStatus; }
    public void setTransfersByStatus(
            Map<String, Integer> transfersByStatus)
    { this.transfersByStatus = transfersByStatus; }

    public Map<String, BigDecimal> getVolumeByCurrency()
    { return volumeByCurrency; }
    public void setVolumeByCurrency(
            Map<String, BigDecimal> volumeByCurrency)
    { this.volumeByCurrency = volumeByCurrency; }

    public List<AgencyReportLine> getByAgency()
    { return byAgency; }
    public void setByAgency(List<AgencyReportLine> byAgency)
    { this.byAgency = byAgency; }

    public List<CorridorReportLine> getByCorridor()
    { return byCorridor; }
    public void setByCorridor(List<CorridorReportLine> byCorridor)
    { this.byCorridor = byCorridor; }
}