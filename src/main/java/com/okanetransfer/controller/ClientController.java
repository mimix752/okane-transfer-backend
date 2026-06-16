package com.okanetransfer.controller;

import com.okanetransfer.dto.request.ChangePasswordRequestDTO;
import com.okanetransfer.dto.request.NotificationPreferencesDTO;
import com.okanetransfer.dto.request.UpdateProfileRequest;
import com.okanetransfer.dto.response.TransferResponseDTO;
import com.okanetransfer.dto.response.UserResponseDTO;
import com.okanetransfer.entity.User;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.ClientService;
import com.okanetransfer.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/client")
@PreAuthorize("hasRole('CLIENT')")
@Tag(name = "Client", description = "Espace client self-service")
@SecurityRequirement(name = "bearer-key")
public class ClientController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ClientService clientService;

    @GetMapping("/transfers")
    @Operation(summary = "Historique de mes transferts")
    public ResponseEntity<List<TransferResponseDTO>> getMyTransfers() {
        return ResponseEntity.ok(clientService.getMyTransfers());
    }

    @GetMapping("/transfers/recent")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<List<TransferResponseDTO>>> getRecentTransfers() {
        List<TransferResponseDTO> all = clientService.getMyTransfers();
        List<TransferResponseDTO> recent = all.stream().limit(5).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(recent));
    }






    @GetMapping("/transfers/filter")
    @Operation(summary = "Filtrer mes transferts par statut, date, montant et corridor")
    public ResponseEntity<List<TransferResponseDTO>> filterTransfers(
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String dateDebut,
            @RequestParam(required = false) String dateFin,
            @RequestParam(required = false) BigDecimal montantMin,
            @RequestParam(required = false) BigDecimal montantMax,
            @RequestParam(required = false) String paysSource,
            @RequestParam(required = false) String paysDestination) {
        return ResponseEntity.ok(
                clientService.filterTransfers(statut, dateDebut, dateFin, montantMin, montantMax, paysSource, paysDestination));
    }

    @GetMapping("/transfers/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<TransferResponseDTO>> getTransferById(
            @PathVariable Long id) {
        TransferResponseDTO dto = clientService.getTransferById(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/profile")
    @Operation(summary = "Voir mon profil")
    public ResponseEntity<UserResponseDTO> getProfile() {
        return ResponseEntity.ok(clientService.getProfile());
    }

    @PutMapping("/profile")
    @Operation(summary = "Modifier mon profil")
    public ResponseEntity<UserResponseDTO> updateProfile(@RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(
                clientService.updateProfile(
                        request.getFirstName(),
                        request.getLastName(),
                        request.getPhone(),
                        request.getUsername()
                ));
    }

    @DeleteMapping("/account")
    @Operation(summary = "Supprimer mon compte - RGPD")
    public ResponseEntity<String> deleteAccount() {
        clientService.deleteAccount();
        return ResponseEntity.ok("Compte supprimé conformément au RGPD");
    }

    @PutMapping("/password")
    @Operation(summary = "Changer l mdp")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequestDTO request) {
        clientService.changePassword(request);
        return ResponseEntity.ok("Mot de passe modifié");
    }
    @GetMapping("/notifications")
    public ResponseEntity<NotificationPreferencesDTO> getNotificationPreferences(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return ResponseEntity.ok(new NotificationPreferencesDTO(
                user.isNotifyEmail(), user.isNotifySms()));
    }

    @PutMapping("/notifications")
    public ResponseEntity<NotificationPreferencesDTO> updateNotificationPreferences(
            @RequestBody NotificationPreferencesDTO dto, Authentication authentication) {

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        user.setNotifyEmail(dto.isNotifyEmail());
        user.setNotifySms(dto.isNotifySms());
        userRepository.save(user);

        return ResponseEntity.ok(dto);
    }

}