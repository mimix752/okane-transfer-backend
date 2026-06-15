package com.okanetransfer.controller.envoi;

import com.okanetransfer.dto.request.EnvoiRequestDTO;
import com.okanetransfer.dto.response.CorridorResponseDTO;
import com.okanetransfer.dto.response.EnvoiResponseDTO;
import com.okanetransfer.dto.response.PageResponse;
import com.okanetransfer.repository.AgentRepository;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.CorridorService;
import com.okanetransfer.service.envoi.EnvoiService;
import com.okanetransfer.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @Autowired private UserRepository userRepository;
    @Autowired private AgentRepository agentRepository;

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

    @GetMapping("/corridors/paginated")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<CorridorResponseDTO>>> getCorridorsPaginated(Pageable pageable) {
        Page<CorridorResponseDTO> page = corridorService.getActiveCorridorsPaginated(pageable);
        PageResponse<CorridorResponseDTO> response = new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
        return ResponseEntity.ok(ApiResponse.success("Corridors retrieved", response));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<EnvoiResponseDTO>>> getRecentTransfers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Long agentId = resolveAgentId(authentication);
        PageResponse<EnvoiResponseDTO> response = envoiService.getRecentTransfersPaginated(agentId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Recent transfers retrieved", response));
    }

    @GetMapping("/search/{code}")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<EnvoiResponseDTO>> searchTransferByCode(@PathVariable String code) {
        EnvoiResponseDTO response = envoiService.searchTransferByCode(code);
        return ResponseEntity.ok(ApiResponse.success("Transfer found", response));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<EnvoiResponseDTO>> createTransfer(
            @Valid @RequestBody EnvoiRequestDTO dto,
            Authentication authentication) {

        Long agentId = resolveAgentId(authentication);
        EnvoiResponseDTO response = envoiService.createTransfer(dto, agentId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transfert créé avec succès", response));
    }

    private Long resolveAgentId(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username))
                .getId();
    }

    @GetMapping("/search/phone/{phone}")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<EnvoiResponseDTO>>> searchTransfersByPhone(
            @PathVariable String phone,
            Authentication authentication) {
        Long agentId = resolveAgentId(authentication);
        List<EnvoiResponseDTO> response = envoiService.searchTransfersByPhone(phone, agentId);
        return ResponseEntity.ok(ApiResponse.success("Transfers found", response));
    }

    @GetMapping("/corridor-by-countries")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<CorridorResponseDTO>> getCorridorByCountries(
            @RequestParam String to,
            Authentication authentication) {
        Long agentId = resolveAgentId(authentication);
        com.okanetransfer.entity.Agent agent = agentRepository
                .findByUserId(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        String from = agent.getAgency().getCountry();
        CorridorResponseDTO corridor = corridorService.findByCountries(from, to);
        return ResponseEntity.ok(ApiResponse.success("Corridor found", corridor));
    }
}
