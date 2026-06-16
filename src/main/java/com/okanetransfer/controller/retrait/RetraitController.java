package com.okanetransfer.controller.retrait;

import com.okanetransfer.dto.request.RetraitRequestDTO;
import com.okanetransfer.dto.response.RetraitResponseDTO;
import com.okanetransfer.service.retrait.RetraitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;


import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/retrait")
public class RetraitController {

    @Autowired private com.okanetransfer.repository.UserRepository userRepository;
    @Autowired private com.okanetransfer.repository.AgentRepository agentRepository;
    @Autowired private RetraitService retraitService;

    @PostMapping
    public ResponseEntity<RetraitResponseDTO> retirer(@Valid @RequestBody RetraitRequestDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long agentId = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
        return ResponseEntity.ok(retraitService.retirer(dto, agentId));
    }

    @GetMapping("/rechercher")
    public ResponseEntity<RetraitResponseDTO> rechercher(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String telephone) {
        return ResponseEntity.ok(retraitService.rechercher(code, telephone));
    }
}



