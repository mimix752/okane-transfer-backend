package com.okanetransfer.service;

import com.okanetransfer.dto.request.FeeGridRequestDTO;
import com.okanetransfer.dto.response.FeeGridResponseDTO;

import java.math.BigDecimal;
import java.util.List;

public interface FeeGridService {

    List<FeeGridResponseDTO> getByCorridor(Long corridorId);

    FeeGridResponseDTO getById(Long id);

    FeeGridResponseDTO create(FeeGridRequestDTO dto,
                              String adminIp);

    FeeGridResponseDTO update(Long id,
                              FeeGridRequestDTO dto,
                              String adminIp);

    void toggle(Long id, String adminIp);

    FeeGridResponseDTO simulate(Long corridorId,
                                BigDecimal amount);

    BigDecimal calculateFee(Long corridorId,
                            BigDecimal amount);
}