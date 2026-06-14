package com.okanetransfer.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.okanetransfer.service.EmbeddingService;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EmbeddingServiceImpl implements EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingServiceImpl.class);

    @Value("${jina.api.key:}")
    private String apiKey;

    @Value("${jina.api.url:https://api.jina.ai/v1/embeddings}")
    private String apiUrl;

    @Value("${jina.model:jina-embeddings-v3}")
    private String model;

    private HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Returns the embedding as a pgvector-compatible string: "[0.1, 0.2, ...]"
     */
    public Optional<String> embed(String text) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Jina API key not configured — skipping embedding");
            return Optional.empty();
        }

        try {
            String requestJson = objectMapper.writeValueAsString(Map.of(
                    "model", model,
                    "input", List.of(text)
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(8))
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                log.warn("Jina returned HTTP {} — body: {}", response.statusCode(), response.body());
                return Optional.empty();
            }

            return extractVector(response.body());

        } catch (Exception e) {
            log.warn("Jina embedding call failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<String> extractVector(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode valuesNode = root.path("data").path(0).path("embedding");

            if (valuesNode.isMissingNode() || !valuesNode.isArray()) {
                log.warn("Jina response missing embedding array");
                return Optional.empty();
            }

            // Build "[v1, v2, v3, ...]" string for pgvector
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < valuesNode.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(valuesNode.get(i).asDouble());
            }
            sb.append("]");

            return Optional.of(sb.toString());

        } catch (Exception e) {
            log.warn("Failed to parse Jina response: {}", e.getMessage());
            return Optional.empty();
        }
    }
}