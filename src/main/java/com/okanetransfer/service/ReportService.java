package com.okanetransfer.service;

import com.okanetransfer.dto.response.ReportResponseDTO;

import java.time.LocalDate;

public interface ReportService {

    ReportResponseDTO getGlobalReport(LocalDate from,
                                      LocalDate to);

    ReportResponseDTO getReportByCorridor(LocalDate from,
                                          LocalDate to,
                                          String corridor);

    ReportResponseDTO getReportByStatus(LocalDate from,
                                        LocalDate to,
                                        String status);
}