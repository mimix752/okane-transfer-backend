package com.okanetransfer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class AgencyRequestDTO {

    @NotBlank
    private String name;

    @NotBlank
    private String address;

    @NotBlank
    private String country;


    @NotNull
    @Positive
    private BigDecimal dailyLimit;

    public AgencyRequestDTO() {
    }

    public AgencyRequestDTO(String name, String address, String country, BigDecimal dailyLimit) {
        this.name = name;
        this.address = address;
        this.country = country;
        this.dailyLimit = dailyLimit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public BigDecimal getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(BigDecimal dailyLimit) {
        this.dailyLimit = dailyLimit;
    }
}