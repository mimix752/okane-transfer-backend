package com.okanetransfer.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.okanetransfer.entity.ChatMessage;
import com.okanetransfer.enums.ChatRole;
import com.okanetransfer.service.GroqChatService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class GroqChatServiceImpl implements GroqChatService {

    private static final Logger log = LoggerFactory.getLogger(GroqChatServiceImpl.class);


    private String buildSystemPrompt(String lang) {
        String langInstruction = switch (lang) {
            case "ar" -> "ALWAYS reply in Arabic (العربية). Never switch to French or English.";
            case "en" -> "ALWAYS reply in English. Never switch to French or Arabic.";
            default   -> "Réponds TOUJOURS en français.";
        };

        return "Tu es un assistant de support client pour OkaneTransfer, une plateforme de transfert d'argent international.\n" +
                langInstruction + "\n" +
                "Tu peux aider avec :\n" +
                "- Suivi de transfert (le client a besoin de son code de transfert, rubrique Historique)\n" +
                "- Frais et commissions (calculés automatiquement avant confirmation du transfert)\n" +
                "- Connexion et code OTP (reçu par SMS ou email, saisi pour valider l'authentification)\n" +
                "- Annulation ou remboursement (possible si le transfert n'a pas encore été payé)\n" +
                "- Modification du mot de passe (Paramètres > Sécurité)\n" +
                "- Gestion des notifications (Paramètres > Notifications)\n" +
                "Si la demande est urgente, complexe ou nécessite un accès aux données du compte, " +
                "recommande au client d'écrire le mot \"agent\" pour être mis en relation avec un conseiller humain.\n" +
                "Limite tes réponses à 3 à 5 phrases. Ne fournis jamais d'informations sensibles.";
    }

    @Value("${groq.api.key:}")
    private String apiKey;

    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String apiUrl;

    @Value("${groq.model:llama-3.1-8b-instant}")
    private String model;

    @Value("${groq.timeout.seconds:8}")
    private int timeoutSeconds;

    private HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public Optional<String> ask(String userMessage, List<ChatMessage> history,String lang) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Groq API key not configured — skipping LLM call");
            return Optional.empty();
        }

        try {
            String requestJson = buildRequestJson(history,lang);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                log.warn("Groq returned HTTP {} — falling back to FAQ. Body: {}",
                        response.statusCode(), response.body());
                return Optional.empty();
            }

            return extractContent(response.body());

        } catch (Exception e) {
            log.warn("Groq call failed ({}) — falling back to FAQ", e.getMessage());
            return Optional.empty();
        }
    }

    private String buildRequestJson(List<ChatMessage> history,String lang  ) throws Exception {
        List<Map<String, String>> messages = new ArrayList<>();

        messages.add(Map.of("role", "system", "content", buildSystemPrompt(lang)));

        for (ChatMessage msg : history) {
            String role = msg.getRole() == ChatRole.USER ? "user" : "assistant";
            messages.add(Map.of("role", role, "content", msg.getContent()));
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", 0.5);
        body.put("max_tokens", 350);

        return objectMapper.writeValueAsString(body);
    }

    private Optional<String> extractContent(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String content = root
                    .path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText(null);

            if (content == null || content.isBlank()) {
                log.warn("Groq returned empty content");
                return Optional.empty();
            }

            return Optional.of(content.trim());

        } catch (Exception e) {
            log.warn("Failed to parse Groq response: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public String translate(String text, String targetLang) {
        if ("fr".equals(targetLang)) return text; // already French, skip the API call

        try {
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content",
                    "You are a translator. Translate the text to the language with ISO code '" + targetLang + "'. " +
                            "Reply with ONLY the translated text, no explanation, no preamble."));
            messages.add(Map.of("role", "user", "content", text));

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", model);
            body.put("messages", messages);
            body.put("temperature", 0.2); // low temp for accurate translation
            body.put("max_tokens", 350);

            String requestJson = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return extractContent(response.body()).orElse(text); // fallback: original
            }

        } catch (Exception e) {
            log.warn("Translation failed ({}) — returning original", e.getMessage());
        }

        return text; // safe fallback: return French if anything goes wrong
    }
}