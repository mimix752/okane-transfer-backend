package com.okanetransfer.service.impl;

import com.okanetransfer.repository.FaqEmbeddingRepository;
import com.okanetransfer.service.EmbeddingService;
import com.okanetransfer.service.SemanticFaqService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SemanticFaqServiceImpl implements SemanticFaqService {

    private static final Logger log = LoggerFactory.getLogger(SemanticFaqService.class);

    public record FaqResult(String answer, boolean escalation) {}

    @Value("${faq.similarity.threshold:0.78}")
    private double similarityThreshold;

    @Autowired
    private FaqEmbeddingRepository faqEmbeddingRepository;

    @Autowired
    private EmbeddingService embeddingService;

    public Optional<FaqResult> findAnswer(String userMessage) {
        Optional<String> vectorOpt = embeddingService.embed(userMessage);

        if (vectorOpt.isEmpty()) {
            log.warn("Embedding failed — skipping semantic FAQ lookup");
            return Optional.empty();
        }

        List<Object[]> results = faqEmbeddingRepository.findClosestMatch(vectorOpt.get());

        if (results == null || results.isEmpty()) {
            return Optional.empty();
        }

        Object[] row = results.get(0);
        String answer      = (String) row[2];
        boolean escalation = (Boolean) row[4];
        double similarity  = ((Number) row[5]).doubleValue();

        log.debug("Semantic FAQ — similarity: {}, escalation: {}", similarity, escalation);

        if (similarity >= similarityThreshold) {
            log.info("FAQ hit (similarity={}, escalation={}) — skipping Groq", similarity, escalation);
            return Optional.of(new FaqResult(answer, escalation));
        }

        return Optional.empty();
}}

