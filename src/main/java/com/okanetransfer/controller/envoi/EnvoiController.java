package com.okanetransfer.controller.envoi;

import com.okanetransfer.dto.request.EnvoiRequestDTO;
import com.okanetransfer.dto.response.EnvoiResponseDTO;
import com.okanetransfer.service.envoi.EnvoiService;
import com.okanetransfer.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/envoi")
@RequiredArgsConstructor
@Slf4j
public class EnvoiController {

    private final EnvoiService envoiService;

    @PostMapping
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ApiResponse<EnvoiResponseDTO>> createTransfer(
            @Valid @RequestBody EnvoiRequestDTO dto,
            Authentication authentication) {

        log.info("Requête de création d'envoi par l'agent: {}", authentication.getName());

        // Récupérer l'ID de l'agent depuis le token JWT
        Long agentId = extractAgentId(authentication);

        // Créer le transfert
        EnvoiResponseDTO response = envoiService.createTransfer(dto, agentId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Transfert créé avec succès",
                        response
                ));
    }

    private Long extractAgentId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof Map) {
            Map<String, Object> claims = (Map<String, Object>) principal;
            Object userId = claims.get("userId");
            if (userId != null) {
                return Long.parseLong(userId.toString());
            }
        }
        throw new IllegalArgumentException("Unable to extract agent ID from token");
    }
}
