package com.okanetransfer.controller.envoi;

import com.okanetransfer.dto.request.EnvoiRequestDTO;
import com.okanetransfer.dto.response.EnvoiResponseDTO;
import com.okanetransfer.service.envoi.EnvoiService;
import com.okanetransfer.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/envoi")
public class EnvoiController {

    @Autowired
    private EnvoiService envoiService;

    @PostMapping
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ApiResponse<EnvoiResponseDTO>> createTransfer(
            @Valid @RequestBody EnvoiRequestDTO dto,
            Authentication authentication) {

        Long agentId = extractAgentId(authentication);

        EnvoiResponseDTO response = envoiService.createTransfer(dto, agentId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Transfert créé avec succès",
                        response
                ));
    }

    private Long extractAgentId(Authentication authentication) {
        Object details = authentication.getDetails();
        if (details instanceof Long) return (Long) details;
        if (details instanceof Integer) return ((Integer) details).longValue();
        throw new IllegalArgumentException("Unable to extract agent ID from token");
    }
}