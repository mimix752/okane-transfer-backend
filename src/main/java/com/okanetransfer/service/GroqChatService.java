package com.okanetransfer.service;

import com.okanetransfer.entity.ChatMessage;

import java.util.List;
import java.util.Optional;

public interface GroqChatService {


    Optional<String> ask(String userMessage, List<ChatMessage> history,String lang);
    String translate(String text, String targetLang);
}