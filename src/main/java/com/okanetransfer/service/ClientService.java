package com.okanetransfer.service;

import com.okanetransfer.dto.response.TransferResponseDTO;
import com.okanetransfer.dto.response.UserResponseDTO;
import java.math.BigDecimal;
import java.util.List;

public interface ClientService {
    List<TransferResponseDTO> getMyTransfers();
    TransferResponseDTO getByCode(String transferCode);
    List<TransferResponseDTO> filterTransfers(String statut, String dateDebut, String dateFin,
                                              BigDecimal montantMin, BigDecimal montantMax,
                                              String paysSource, String paysDestination);
    UserResponseDTO getProfile();
    UserResponseDTO updateProfile(String firstName, String lastName, String phone);
    void deleteAccount();
}