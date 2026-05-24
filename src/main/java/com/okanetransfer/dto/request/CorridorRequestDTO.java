package com.okanetransfer.dto.request;

import jakarta.validation.constraints.*;

public class CorridorRequestDTO {

    @NotBlank(message = "Source country is required")
    @Size(min = 2, max = 3,
            message = "Source country code must be 2 or 3 characters")
    @Pattern(regexp = "^[A-Z]{2,3}$",
            message = "Source country must be uppercase letters only")
    private String sourceCountry;

    @NotBlank(message = "Destination country is required")
    @Size(min = 2, max = 3,
            message = "Destination country code must be 2 or 3 characters")
    @Pattern(regexp = "^[A-Z]{2,3}$",
            message = "Destination country must be uppercase letters only")
    private String destinationCountry;

    @NotNull(message = "Source currency ID is required")
    @Positive(message = "Source currency ID must be positive")
    private Long sourceCurrencyId;

    @NotNull(message = "Destination currency ID is required")
    @Positive(message = "Destination currency ID must be positive")
    private Long destinationCurrencyId;

    private Boolean active;

    public CorridorRequestDTO() {}

    public CorridorRequestDTO(String sourceCountry,
                              String destinationCountry,
                              Long sourceCurrencyId,
                              Long destinationCurrencyId,
                              Boolean active) {
        this.sourceCountry       = sourceCountry;
        this.destinationCountry  = destinationCountry;
        this.sourceCurrencyId    = sourceCurrencyId;
        this.destinationCurrencyId = destinationCurrencyId;
        this.active              = active;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String  sourceCountry;
        private String  destinationCountry;
        private Long    sourceCurrencyId;
        private Long    destinationCurrencyId;
        private Boolean active;

        public Builder sourceCountry(String sourceCountry)
        { this.sourceCountry = sourceCountry; return this; }
        public Builder destinationCountry(String destinationCountry)
        { this.destinationCountry = destinationCountry; return this; }
        public Builder sourceCurrencyId(Long sourceCurrencyId)
        { this.sourceCurrencyId = sourceCurrencyId; return this; }
        public Builder destinationCurrencyId(Long destinationCurrencyId)
        { this.destinationCurrencyId = destinationCurrencyId; return this; }
        public Builder active(Boolean active)
        { this.active = active; return this; }

        public CorridorRequestDTO build() {
            CorridorRequestDTO d = new CorridorRequestDTO();
            d.sourceCountry         = this.sourceCountry;
            d.destinationCountry    = this.destinationCountry;
            d.sourceCurrencyId      = this.sourceCurrencyId;
            d.destinationCurrencyId = this.destinationCurrencyId;
            d.active                = this.active;
            return d;
        }
    }

    public String getSourceCountry() { return sourceCountry; }
    public void setSourceCountry(String sourceCountry)
    { this.sourceCountry = sourceCountry; }

    public String getDestinationCountry() { return destinationCountry; }
    public void setDestinationCountry(String destinationCountry)
    { this.destinationCountry = destinationCountry; }

    public Long getSourceCurrencyId() { return sourceCurrencyId; }
    public void setSourceCurrencyId(Long sourceCurrencyId)
    { this.sourceCurrencyId = sourceCurrencyId; }

    public Long getDestinationCurrencyId()
    { return destinationCurrencyId; }
    public void setDestinationCurrencyId(Long destinationCurrencyId)
    { this.destinationCurrencyId = destinationCurrencyId; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    @Override
    public String toString() {
        return "CorridorRequestDTO{" + sourceCountry
                + "→" + destinationCountry + "}";
    }
}