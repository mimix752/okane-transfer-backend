package com.okanetransfer.controller;

import com.okanetransfer.entity.Transfer;
import com.okanetransfer.entity.User;
import com.okanetransfer.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

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
    public ResponseEntity<List<Transfer>> getMyTransfers() {
        return ResponseEntity.ok(clientService.getMyTransfers());
    }

    @GetMapping("/transfers/{code}")
    @Operation(summary = "Rechercher un transfert par code")
    public ResponseEntity<Transfer> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(clientService.getByCode(code));
    }

    @GetMapping("/transfers/filter")
    @Operation(summary = "Filtrer mes transferts par statut")
    public ResponseEntity<List<Transfer>> filterTransfers(
            @RequestParam(required = false) String statut) {
        return ResponseEntity.ok(clientService.filterTransfers(statut));
    }

    @GetMapping("/profile")
    @Operation(summary = "Voir mon profil")
    public ResponseEntity<User> getProfile() {
        return ResponseEntity.ok(clientService.getProfile());
    }

    @PutMapping("/profile")
    @Operation(summary = "Modifier mon profil")
    public ResponseEntity<User> updateProfile(
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