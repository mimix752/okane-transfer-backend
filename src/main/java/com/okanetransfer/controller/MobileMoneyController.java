package com.okanetransfer.controller;

import com.okanetransfer.dto.request.MobileMoneyRequestDTO;
import com.okanetransfer.dto.response.MobileMoneyResponseDTO;
import com.okanetransfer.enums.MobileMoneyOperator;
import com.okanetransfer.service.MobileMoneyService;
import com.okanetransfer.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mobile-money")
public class MobileMoneyController {

    @Autowired private MobileMoneyService mobileMoneyService;

    // Agent — envoyer vers compte mobile money
    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<MobileMoneyResponseDTO>> send(
            @Valid @RequestBody MobileMoneyRequestDTO dto) {
        return ResponseEntity.status(201)
                .body(ApiResponse.success("Envoi mobile money effectué", mobileMoneyService.send(dto)));
    }

    // Agent — dashboard ses propres transferts mobile money
    @GetMapping("/agent/{agentId}")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<MobileMoneyResponseDTO>>> getByAgent(
            @PathVariable Long agentId) {
        return ResponseEntity.ok(ApiResponse.success("Transferts récupérés", mobileMoneyService.getByAgent(agentId)));
    }

    // Admin — dashboard global
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<MobileMoneyResponseDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Tous les transferts mobile money", mobileMoneyService.getAll()));
    }

    // Admin — filtrer par opérateur
    @GetMapping("/operator/{operator}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<MobileMoneyResponseDTO>>> getByOperator(
            @PathVariable MobileMoneyOperator operator) {
        return ResponseEntity.ok(ApiResponse.success("Transferts par opérateur", mobileMoneyService.getByOperator(operator)));
    }

    // Admin — transferts en attente de réconciliation
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<MobileMoneyResponseDTO>>> getPending() {
        return ResponseEntity.ok(ApiResponse.success("Transferts en attente", mobileMoneyService.getPending()));
    }

    // Admin — réconcilier un transfert
    @PostMapping("/{id}/reconcile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MobileMoneyResponseDTO>> reconcile(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Réconciliation effectuée", mobileMoneyService.reconcile(id)));
    }
}
