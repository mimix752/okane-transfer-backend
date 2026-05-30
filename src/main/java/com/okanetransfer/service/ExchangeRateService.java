package com.okanetransfer.service;

import com.okanetransfer.dto.request.RateUpdateRequestDTO;
import com.okanetransfer.dto.response.ApiSyncResponseDTO;
import com.okanetransfer.dto.response.CurrencyRateHistoryResponseDTO;

import java.util.List;

public interface ExchangeRateService {

    CurrencyRateHistoryResponseDTO updateManually(
            RateUpdateRequestDTO dto,
            String adminIp);

    ApiSyncResponseDTO syncFromApi(String adminIp);

    List<CurrencyRateHistoryResponseDTO> getHistory(
            int page, int size);

    List<CurrencyRateHistoryResponseDTO> getHistoryByCurrency(
            Long currencyId);
}