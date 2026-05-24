package com.okanetransfer.dto.response;

import java.math.BigDecimal;

public class AgencyResponseDTO {

    private Long id;
    private String name;
    private String address;
    private String country;
    private int agentCount;
    private BigDecimal dailyLimit;
    private boolean active;

    public AgencyResponseDTO() {
    }

    public AgencyResponseDTO(Long id, String name, String address, String country, int agentCount, BigDecimal dailyLimit, boolean active) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.country = country;
        this.agentCount = agentCount;
        this.dailyLimit = dailyLimit;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public int getAgentCount() {
        return agentCount;
    }

    public void setAgentCount(int agentCount) {
        this.agentCount = agentCount;
    }

    public BigDecimal getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(BigDecimal dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}