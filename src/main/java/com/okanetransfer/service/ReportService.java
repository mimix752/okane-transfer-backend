package com.okanetransfer.service;

import com.okanetransfer.dto.response.CorridorStatsResponseDTO;
import com.okanetransfer.dto.response.ReportResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    // Rapport global sur une période
    ReportResponseDTO getGlobalReport(LocalDate from, LocalDate to);

    // Rapport filtré par corridor (nom agence)
    ReportResponseDTO getReportByCorridor(LocalDate from,
                                          LocalDate to,
                                          String corridor);

    // Rapport filtré par statut
    ReportResponseDTO getReportByStatus(LocalDate from,
                                        LocalDate to,
                                        String status);

    // Volume journalier + mensuel pour UN corridor
    CorridorStatsResponseDTO getCorridorStats(Long corridorId);

    // Volume journalier + mensuel pour TOUS les corridors actifs
    List<CorridorStatsResponseDTO> getAllCorridorStats();
}