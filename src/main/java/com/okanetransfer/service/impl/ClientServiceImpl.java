package com.okanetransfer.service.impl;

import com.okanetransfer.entity.Transfer;
import com.okanetransfer.entity.User;
import com.okanetransfer.enums.TransferStatus;
import com.okanetransfer.repository.TransferRepository;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.AuditService;
import com.okanetransfer.service.ClientService;
import com.okanetransfer.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private AuditService auditService;


    @Transactional(readOnly = true)
    public List<Transfer> getMyTransfers() {
        User client = getConnectedUser();
        return transferRepository.findBySenderId(client.getId());
    }

    @Transactional(readOnly = true)
    public Transfer getByCode(String transferCode) {
        User client = getConnectedUser();
        Transfer transfer = transferRepository.findByCode(transferCode)
                .orElseThrow(() -> new RuntimeException(
                        "Transfert non trouvé"));

        if (!transfer.getSender().getId().equals(client.getId()))
            throw new RuntimeException("Accès non autorisé");

        return transfer;
    }

    @Transactional(readOnly = true)
    public List<Transfer> filterTransfers(String statut) {
        List<Transfer> myTransfers = getMyTransfers();

        if (statut == null || statut.isEmpty())
            return myTransfers;

        TransferStatus status = TransferStatus.valueOf(
                statut.toUpperCase());

        return myTransfers.stream()
                .filter(t -> t.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public User getProfile() {
        return getConnectedUser();
    }

    @Transactional
    public User updateProfile(String firstName,
                              String lastName,
                              String phone) {
        User client = getConnectedUser();

        String oldFirstName = client.getFirstName();
        String oldLastName  = client.getLastName();
        String oldPhone     = client.getPhone();

        if (firstName != null) client.setFirstName(firstName);
        if (lastName  != null) client.setLastName(lastName);
        if (phone     != null) client.setPhone(phone);

        User saved = userRepository.save(client);

        auditService.log(
                SecurityUtils.getCurrentUsername(),
                "UPDATE_PROFILE",
                "User",
                saved.getId(),
                "old=[firstName=" + oldFirstName
                        + ", lastName=" + oldLastName
                        + ", phone=" + oldPhone + "]"
                        + " | new=[firstName=" + saved.getFirstName()
                        + ", lastName=" + saved.getLastName()
                        + ", phone=" + saved.getPhone() + "]"
        );

        return saved;
    }

    @Transactional
    public void deleteAccount() {
        User client = getConnectedUser();

        String oldEmail    = client.getEmail();
        String oldUsername = client.getUsername();

        client.setFirstName("SUPPRIMÉ");
        client.setLastName("SUPPRIMÉ");
        client.setPhone("");
        client.setEmail("deleted_" + client.getId() + "@deleted.com");
        client.setUsername("deleted_" + client.getId());
        client.setEnabled(false);

        userRepository.save(client);

        auditService.log(
                SecurityUtils.getCurrentUsername(),
                "DELETE_ACCOUNT",
                "User",
                client.getId(),
                "old=[email=" + oldEmail
                        + ", username=" + oldUsername + "]"
                        + " | new=[anonymized]"
        );
    }


    private User getConnectedUser() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(
                        "User non trouvé"));
    }
}