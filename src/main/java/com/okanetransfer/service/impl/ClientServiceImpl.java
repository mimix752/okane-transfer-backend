package com.okanetransfer.service.impl;

import com.okanetransfer.entity.Transfer;
import com.okanetransfer.entity.User;
import com.okanetransfer.enums.TransferStatus;
import com.okanetransfer.repository.TransferRepository;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ClientServiceImpl implements ClientService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TransferRepository transferRepository;
    private User getConnectedUser(){
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User non trouvé"));

    }
    public List<Transfer> getMyTransfers(){
        User client = getConnectedUser();
        return transferRepository.findBySenderId(client.getId());
    }
    public Transfer getByCode(String transferCode){
        User client = getConnectedUser();
        Transfer transfer = transferRepository.findByCode(transferCode)
                .orElseThrow(() -> new RuntimeException("Transfert non trouvé"));
        if (!transfer.getSender().getId().equals(client.getId())) {
            throw new RuntimeException("Accès non autorisé");
        }
        return transfer;
    }
    public List<Transfer> filterTransfers(String statut) {
        List<Transfer> myTransfers = getMyTransfers();
        if( statut == null || statut.isEmpty()) {
            return myTransfers;
        }
        TransferStatus status = TransferStatus.valueOf(statut.toUpperCase());
        return myTransfers.stream()
                .filter(t -> t.getStatus() == status)
                .collect(java.util.stream.Collectors.toList());
    }
    public User getProfile() {
        return getConnectedUser();
    }
    public User updateProfile(String firstName, String lastName, String phone){
        User client = getConnectedUser();
        if (firstName != null) client.setFirstName(firstName);
        if (lastName != null) client.setLastName(lastName);
        if (phone != null) client.setPhone(phone);

        return userRepository.save(client);


    }
    public void deleteAccount() {
        User client = getConnectedUser();
        client.setFirstName("SUPPRIMÉ");
        client.setLastName("SUPPRIMÉ");
        client.setPhone("");
        client.setEmail("deleted_" + client.getId() + "@deleted.com");
        client.setUsername("deleted_" + client.getId());
        client.setEnabled(false);
        userRepository.save(client);
    }
}