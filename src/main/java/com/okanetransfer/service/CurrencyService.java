package com.okanetransfer.service;

import java.util.List;

public interface CurrencyService {

    List<CurrencyResponseDTO> getAllCurrencies();

    List<CurrencyResponseDTO> getActiveCurrencies();

    CurrencyResponseDTO getById(Long id);

    CurrencyResponseDTO getByCode(String code);

    CurrencyResponseDTO create(CurrencyRequestDTO dto,
                               String adminIp);

    CurrencyResponseDTO update(Long id,
                               CurrencyRequestDTO dto,
                               String adminIp);

    void toggle(Long id, String adminIp);
}