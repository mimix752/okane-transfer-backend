package com.okanetransfer.service;

import com.okanetransfer.dto.request.ChatMessageRequestDTO;
import com.okanetransfer.dto.response.ChatMessageResponseDTO;

public interface ChatbotService {

    ChatMessageResponseDTO chat(ChatMessageRequestDTO dto);
}