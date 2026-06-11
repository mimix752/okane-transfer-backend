package com.okanetransfer.service.impl;

import com.okanetransfer.dto.response.TransferResponseDTO;
import com.okanetransfer.dto.response.UserResponseDTO;
import com.okanetransfer.entity.Transfer;
import com.okanetransfer.entity.User;
import com.okanetransfer.enums.TransferStatus;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.TransferRepository;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.AuditService;
import com.okanetransfer.service.ClientService;
import com.okanetransfer.service.NotificationService;
import com.okanetransfer.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Autowired
    private NotificationService notificationService;

    // ─── GET MY TRANSFERS ───
    @Override
    @Transactional(readOnly = true)
    public List<TransferResponseDTO> getMyTransfers() {
        User client = getConnectedUser();
        return transferRepository.findBySenderId(client.getId())
                .stream()
                .map(TransferResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ─── GET BY CODE ───
    @Override
    @Transactional(readOnly = true)
    public TransferResponseDTO getByCode(String transferCode) {
        User client = getConnectedUser();
        Transfer transfer = transferRepository.findByCode(transferCode)
                .orElseThrow(() -> new ResourceNotFoundException("Transfert non trouvé : " + transferCode));
        if (!transfer.getSender().getId().equals(client.getId()))
            throw new AccessDeniedException("Accès non autorisé à ce transfert");
        return TransferResponseDTO.fromEntity(transfer);
    }

    // ─── FILTER TRANSFERS ───
    @Override
    @Transactional(readOnly = true)
    public List<TransferResponseDTO> filterTransfers(String statut, String dateDebut, String dateFin,
                                                     BigDecimal montantMin, BigDecimal montantMax,
                                                     String paysSource, String paysDestination) {
        User client = getConnectedUser();
        List<Transfer> myTransfers = transferRepository.findBySenderId(client.getId());

        return myTransfers.stream()
                .filter(t -> {

                    // Filtre statut
                    if (statut != null && !statut.isEmpty()) {
                        try {
                            TransferStatus status = TransferStatus.valueOf(statut.toUpperCase());
                            if (t.getStatus() != status) return false;
                        } catch (IllegalArgumentException e) {
                            throw new IllegalArgumentException("Statut invalide : " + statut);
                        }
                    }

                    // Filtre date début — exclure si le transfert est AVANT la date de début
                    if (dateDebut != null && !dateDebut.isEmpty()) {
                        try {
                            LocalDate debut = LocalDate.parse(dateDebut);
                            if (t.getCreatedAt() == null) return false;
                            if (t.getCreatedAt().toLocalDate().isBefore(debut)) return false;
                        } catch (Exception e) {
                            throw new IllegalArgumentException("Format dateDebut invalide. Utilisez yyyy-MM-dd");
                        }
                    }

                    // Filtre date fin — exclure si le transfert est APRÈS la date de fin
                    if (dateFin != null && !dateFin.isEmpty()) {
                        try {
                            LocalDate fin = LocalDate.parse(dateFin);
                            if (t.getCreatedAt() == null) return false;
                            if (t.getCreatedAt().toLocalDate().isAfter(fin)) return false;
                        } catch (Exception e) {
                            throw new IllegalArgumentException("Format dateFin invalide. Utilisez yyyy-MM-dd");
                        }
                    }

                    // Filtre montant minimum
                    if (montantMin != null) {
                        if (t.getAmount() == null) return false;
                        if (t.getAmount().compareTo(montantMin) < 0) return false;
                    }

                    // Filtre montant maximum
                    if (montantMax != null) {
                        if (t.getAmount() == null) return false;
                        if (t.getAmount().compareTo(montantMax) > 0) return false;
                    }

                    // Filtre pays source (corridor)
                    if (paysSource != null && !paysSource.isEmpty()) {
                        if (t.getSenderCountry() == null) return false;
                        if (!paysSource.equalsIgnoreCase(t.getSenderCountry())) return false;
                    }

                    // Filtre pays destination (corridor)
                    if (paysDestination != null && !paysDestination.isEmpty()) {
                        if (t.getRecipientCountry() == null) return false;
                        if (!paysDestination.equalsIgnoreCase(t.getRecipientCountry())) return false;
                    }

                    return true;
                })
                .map(TransferResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ─── GET PROFILE ───
    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getProfile() {
        return UserResponseDTO.fromEntity(getConnectedUser());
    }

    // ─── UPDATE PROFILE ───
    @Override
    @Transactional
    public UserResponseDTO updateProfile(String firstName, String lastName, String phone) {
        User client = getConnectedUser();

        String oldFirstName = client.getFirstName();
        String oldLastName = client.getLastName();
        String oldPhone = client.getPhone();

        // Validation et mise à jour — ignorer les valeurs vides
        if (firstName != null && !firstName.trim().isEmpty())
            client.setFirstName(firstName.trim());
        if (lastName != null && !lastName.trim().isEmpty())
            client.setLastName(lastName.trim());
        if (phone != null && !phone.trim().isEmpty())
            client.setPhone(phone.trim());

        User saved = userRepository.save(client);

        // Audit log
        auditService.log(
                SecurityUtils.getCurrentUsername(),
                "UPDATE_PROFILE",
                "User",
                saved.getId(),
                LocalDateTime.now() + " - L'utilisateur a modifier ses infos de firstName=" + oldFirstName + ", lastName=" + oldLastName + ", phone=" + oldPhone + " à "
                        + " -> firstName=" + saved.getFirstName() + ", lastName=" + saved.getLastName()
                        + ", phone=" + saved.getPhone()
        );

        // Notification
        notificationService.sendProfileUpdateNotification(saved.getEmail(), saved.getUsername());

        return UserResponseDTO.fromEntity(saved);
    }

    // ─── DELETE ACCOUNT (RGPD) ───
    @Override
    @Transactional
    public void deleteAccount() {
        User client = getConnectedUser();

        String oldEmail = client.getEmail();
        String oldUsername = client.getUsername();

        // Pseudonymisation RGPD
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
                LocalDateTime.now() +  " - Compte supprimé email=" + oldEmail + ", username=" + oldUsername + "] | new=[anonymized]"
        );

        notificationService.sendAccountDeletionNotification(oldEmail, oldUsername);
    }
    
    private User getConnectedUser() {
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur connecté non trouvé : " + username));
    }
}