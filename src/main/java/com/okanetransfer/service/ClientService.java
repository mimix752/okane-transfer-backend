package com.okanetransfer.service;

import com.okanetransfer.dto.request.ChangePasswordRequestDTO;
import com.okanetransfer.dto.request.NotificationPreferencesDTO;
import com.okanetransfer.dto.response.TransferResponseDTO;
import com.okanetransfer.dto.response.UserResponseDTO;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface ClientService {
    List<TransferResponseDTO> getMyTransfers();
    List<TransferResponseDTO> filterTransfers(String statut, String dateDebut, String dateFin,
                                              BigDecimal montantMin, BigDecimal montantMax,
                                              String paysSource, String paysDestination);
    UserResponseDTO getProfile();
    UserResponseDTO updateProfile(String firstName, String lastName, String phone,String username);
    void deleteAccount();
    void changePassword(ChangePasswordRequestDTO request);
    TransferResponseDTO getTransferById(Long id);
    @Transactional(readOnly = true)
    NotificationPreferencesDTO getNotificationPreferences();

    @Transactional
    NotificationPreferencesDTO updateNotificationPreferences(NotificationPreferencesDTO request);




}