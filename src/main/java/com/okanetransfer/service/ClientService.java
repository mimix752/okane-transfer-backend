package com.okanetransfer.service;

import com.okanetransfer.entity.Transfer;
import com.okanetransfer.entity.User;
import java.util.List;

public interface ClientService {
    List<Transfer> getMyTransfers();
    Transfer getByCode(String transferCode);
    List<Transfer> filterTransfers(String statut);
    User getProfile();
    User updateProfile(String firstName, String lastName, String phone);
    void deleteAccount();
}