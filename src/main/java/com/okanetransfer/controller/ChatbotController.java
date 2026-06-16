package com.okanetransfer.controller;

import com.okanetransfer.dto.request.ChatMessageRequestDTO;
import com.okanetransfer.dto.response.ChatMessageResponseDTO;
import com.okanetransfer.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chatbot", description = "Assistant conversationnel client")
@SecurityRequirement(name = "bearer-key")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @PostMapping("/message")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Envoyer un message au chatbot")
    public ResponseEntity<ChatMessageResponseDTO> sendMessage(
            @Valid @RequestBody ChatMessageRequestDTO dto) {
        return ResponseEntity.ok(chatbotService.chat(dto));
    }
}