package com.okanetransfer.controller;

import com.okanetransfer.dto.response.TransferResponseDTO;
import com.okanetransfer.dto.response.UserResponseDTO;
import com.okanetransfer.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/client")
@PreAuthorize("hasRole('CLIENT')")
@Tag(name = "Client", description = "Espace client self-service")
@SecurityRequirement(name = "bearer-key")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @GetMapping("/transfers")
    @Operation(summary = "Historique de mes transferts")
    public ResponseEntity<List<TransferResponseDTO>> getMyTransfers() {
        return ResponseEntity.ok(clientService.getMyTransfers());
    }

    @GetMapping("/transfers/{code}")
    @Operation(summary = "Rechercher un transfert par code")
    public ResponseEntity<TransferResponseDTO> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(clientService.getByCode(code));
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

    @GetMapping("/profile")
    @Operation(summary = "Voir mon profil")
    public ResponseEntity<UserResponseDTO> getProfile() {
        return ResponseEntity.ok(clientService.getProfile());
    }

    @PutMapping("/profile")
    @Operation(summary = "Modifier mon profil")
    public ResponseEntity<UserResponseDTO> updateProfile(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String phone) {
        return ResponseEntity.ok(
                clientService.updateProfile(firstName, lastName, phone));
    }

    @DeleteMapping("/account")
    @Operation(summary = "Supprimer mon compte - RGPD")
    public ResponseEntity<String> deleteAccount() {
        clientService.deleteAccount();
        return ResponseEntity.ok("Compte supprimé conformément au RGPD");
    }
}