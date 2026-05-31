package com.okanetransfer.controller.envoi;

import com.okanetransfer.dto.request.EnvoiRequestDTO;
import com.okanetransfer.dto.response.CorridorResponseDTO;
import com.okanetransfer.dto.response.EnvoiResponseDTO;
import com.okanetransfer.service.CorridorService;
import com.okanetransfer.service.envoi.EnvoiService;
import com.okanetransfer.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/envoi")
public class EnvoiController {

    @Autowired private EnvoiService envoiService;
    @Autowired private CorridorService corridorService;

    @GetMapping("/currencies")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<com.okanetransfer.dto.response.CurrencyResponseDTO>>> getCurrencies() {
        return ResponseEntity.ok(ApiResponse.success("Currencies retrieved", corridorService.getActiveCurrencies()));
    }

    @GetMapping("/corridors")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<CorridorResponseDTO>>> getCorridors() {
        return ResponseEntity.ok(ApiResponse.success("Corridors retrieved", corridorService.getActiveCorridors()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<EnvoiResponseDTO>> createTransfer(
            @Valid @RequestBody EnvoiRequestDTO dto,
            Authentication authentication) {

        Long agentId = (Long) authentication.getDetails();
        EnvoiResponseDTO response = envoiService.createTransfer(dto, agentId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transfert créé avec succès", response));
    }
}
