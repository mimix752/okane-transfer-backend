package com.okanetransfer.service;

import com.okanetransfer.dto.request.CorridorRequestDTO;
import com.okanetransfer.dto.response.CorridorResponseDTO;

import java.util.List;

public interface CorridorService {

    List<CorridorResponseDTO> getAllCorridors();

    List<CorridorResponseDTO> getActiveCorridors();

    List<CorridorResponseDTO> getBySourceCountry(
            String sourceCountry);

    CorridorResponseDTO getById(Long id);

    CorridorResponseDTO create(CorridorRequestDTO dto,
                               String adminIp);

    CorridorResponseDTO update(Long id,
                               CorridorRequestDTO dto,
                               String adminIp);

    void toggle(Long id, String adminIp);
}