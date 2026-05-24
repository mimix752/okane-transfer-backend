package com.okanetransfer.dto.response;

import com.okanetransfer.entity.Corridor;
import java.time.LocalDateTime;

public class CorridorResponseDTO {

    private Long          id;
    private String        sourceCountry;
    private String        destinationCountry;
    private CurrencyInfo  sourceCurrency;
    private CurrencyInfo  destinationCurrency;
    private boolean       active;
    private LocalDateTime createdAt;

    public static class CurrencyInfo {
        private Long   id;
        private String code;
        private String symbol;
        private String name;

        public CurrencyInfo() {}

        public CurrencyInfo(Long id, String code,
                            String symbol, String name) {
            this.id     = id;
            this.code   = code;
            this.symbol = symbol;
            this.name   = name;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long   id;
            private String code;
            private String symbol;
            private String name;

            public Builder id(Long id)
            { this.id = id; return this; }
            public Builder code(String code)
            { this.code = code; return this; }
            public Builder symbol(String symbol)
            { this.symbol = symbol; return this; }
            public Builder name(String name)
            { this.name = name; return this; }

            public CurrencyInfo build() {
                CurrencyInfo i = new CurrencyInfo();
                i.id     = this.id;
                i.code   = this.code;
                i.symbol = this.symbol;
                i.name   = this.name;
                return i;
            }
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol)
        { this.symbol = symbol; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }


    public CorridorResponseDTO() {}

    public static CorridorResponseDTO fromEntity(Corridor corridor) {
        CorridorResponseDTO d = new CorridorResponseDTO();
        d.id                 = corridor.getId();
        d.sourceCountry      = corridor.getSourceCountry();
        d.destinationCountry = corridor.getDestinationCountry();
        d.active             = corridor.isActive();
        d.createdAt          = corridor.getCreatedAt();

        d.sourceCurrency = new CurrencyInfo(
                corridor.getSourceCurrency().getId(),
                corridor.getSourceCurrency().getCode(),
                corridor.getSourceCurrency().getSymbol(),
                corridor.getSourceCurrency().getName()
        );

        d.destinationCurrency = new CurrencyInfo(
                corridor.getDestinationCurrency().getId(),
                corridor.getDestinationCurrency().getCode(),
                corridor.getDestinationCurrency().getSymbol(),
                corridor.getDestinationCurrency().getName()
        );

        return d;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long          id;
        private String        sourceCountry;
        private String        destinationCountry;
        private CurrencyInfo  sourceCurrency;
        private CurrencyInfo  destinationCurrency;
        private boolean       active;
        private LocalDateTime createdAt;

        public Builder id(Long id)
        { this.id = id; return this; }
        public Builder sourceCountry(String sourceCountry)
        { this.sourceCountry = sourceCountry; return this; }
        public Builder destinationCountry(String destinationCountry)
        { this.destinationCountry = destinationCountry; return this; }
        public Builder sourceCurrency(CurrencyInfo sourceCurrency)
        { this.sourceCurrency = sourceCurrency; return this; }
        public Builder destinationCurrency(
                CurrencyInfo destinationCurrency)
        { this.destinationCurrency = destinationCurrency;
            return this; }
        public Builder active(boolean active)
        { this.active = active; return this; }
        public Builder createdAt(LocalDateTime createdAt)
        { this.createdAt = createdAt; return this; }

        public CorridorResponseDTO build() {
            CorridorResponseDTO d = new CorridorResponseDTO();
            d.id                  = this.id;
            d.sourceCountry       = this.sourceCountry;
            d.destinationCountry  = this.destinationCountry;
            d.sourceCurrency      = this.sourceCurrency;
            d.destinationCurrency = this.destinationCurrency;
            d.active              = this.active;
            d.createdAt           = this.createdAt;
            return d;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSourceCountry() { return sourceCountry; }
    public void setSourceCountry(String sourceCountry)
    { this.sourceCountry = sourceCountry; }

    public String getDestinationCountry()
    { return destinationCountry; }
    public void setDestinationCountry(String destinationCountry)
    { this.destinationCountry = destinationCountry; }

    public CurrencyInfo getSourceCurrency()
    { return sourceCurrency; }
    public void setSourceCurrency(CurrencyInfo sourceCurrency)
    { this.sourceCurrency = sourceCurrency; }

    public CurrencyInfo getDestinationCurrency()
    { return destinationCurrency; }
    public void setDestinationCurrency(
            CurrencyInfo destinationCurrency)
    { this.destinationCurrency = destinationCurrency; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)
    { this.createdAt = createdAt; }
}