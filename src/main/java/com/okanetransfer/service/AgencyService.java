package com.okanetransfer.service;

import com.okanetransfer.dto.request.AgencyRequestDTO;
import com.okanetransfer.dto.response.AgencyPerformanceResponseDTO;
import com.okanetransfer.dto.response.AgencyResponseDTO;

import java.util.List;

public interface AgencyService {

    List<AgencyResponseDTO> getAllAgencies(String country, Boolean active);

    AgencyResponseDTO getById(Long id);

    AgencyResponseDTO create(AgencyRequestDTO dto, String adminIp);

    AgencyResponseDTO update(Long id, AgencyRequestDTO dto, String adminIp);

    void toggle(Long id, String adminIp);

    void addAgent(Long agencyId, Long userId, String adminIp);

    void removeAgent(Long agencyId, Long userId, String adminIp);

    AgencyPerformanceResponseDTO getPerformance(Long id);
}