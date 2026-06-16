package com.okanetransfer.service;

import com.okanetransfer.dto.request.AgencyRequestDTO;
import com.okanetransfer.dto.response.AgencyPerformanceResponseDTO;
import com.okanetransfer.dto.response.AgencyResponseDTO;
import com.okanetransfer.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface AgencyService {

    List<AgencyResponseDTO> getAllAgencies(String country, Boolean active);

    PageResponse<AgencyResponseDTO> getAllAgenciesPaginated(Pageable pageable);

    AgencyResponseDTO getById(Long id);

    AgencyResponseDTO create(AgencyRequestDTO dto, String adminIp);

    AgencyResponseDTO update(Long id, AgencyRequestDTO dto, String adminIp);

    void toggle(Long id, String adminIp);

    void addAgent(Long agencyId, Long userId, String adminIp);

    void removeAgent(Long agencyId, Long userId, String adminIp);

    AgencyPerformanceResponseDTO getPerformance(Long id);
    
    List<AgencyPerformanceResponseDTO> getAllPerformances();

    void checkAndDeductBalance(Long agencyId, BigDecimal amount);

    void addBalance(Long agencyId, BigDecimal amount);
}