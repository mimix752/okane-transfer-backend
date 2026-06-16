package com.okanetransfer.service;

import com.okanetransfer.entity.FaqEmbedding;
import com.okanetransfer.repository.FaqEmbeddingRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FaqDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(FaqDataInitializer.class);

    @Autowired
    private FaqEmbeddingRepository faqEmbeddingRepository;

    @Autowired
    private EmbeddingService embeddingService;

    @PostConstruct
    public void seedIfEmpty() {
        try {
            if (faqEmbeddingRepository.existsBy()) {
                log.info("FAQ table already seeded — skipping");
                return;
            }
        } catch (Exception e) {
            log.warn("FAQ table not available (pgvector not installed?) — skipping seed: {}", e.getMessage());
            return;
        }

        log.info("FAQ table empty — starting seed...");

        try {
            PathMatchingResourcePatternResolver resolver =
                    new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:faq/*.md");

            int total = 0;
            for (Resource resource : resources) {
                List<FaqEntry> entries = parseMd(resource);
                for (FaqEntry entry : entries) {
                    Optional<String> vector = embeddingService.embed(entry.question);
                    if (vector.isPresent()) {
                        faqEmbeddingRepository.insertWithVector(
                                entry.question(), entry.answer(), vector.get(), entry.escalation()
                        );
                        total++;
                        log.debug("Seeded: {}", entry.question);
                    } else {
                        log.warn("Embedding failed for: {}", entry.question);
                    }
                }
                log.info("Processed file: {}", resource.getFilename());
            }

            log.info("FAQ seeding complete — {} entries saved", total);

        } catch (Exception e) {
            log.error("FAQ seeding failed: {}", e.getMessage(), e);
        }
    }

    private List<FaqEntry> parseMd(Resource resource) throws Exception {
        List<FaqEntry> entries = new ArrayList<>();

        String currentQuestion = null;
        StringBuilder currentAnswer = new StringBuilder();
        boolean currentEscalation = false;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("QUESTION:")) {
                    if (currentQuestion != null && !currentAnswer.isEmpty()) {
                        entries.add(new FaqEntry(
                                currentQuestion,
                                currentAnswer.toString().trim(),
                                currentEscalation
                        ));
                    }
                    currentQuestion = line.substring("QUESTION:".length()).trim();
                    currentAnswer = new StringBuilder();
                    currentEscalation = false; // reset for each new question

                } else if (line.startsWith("ANSWER:")) {
                    currentAnswer.append(line.substring("ANSWER:".length()).trim());

                } else if (line.startsWith("ESCALATE:")) {
                    String flag = line.substring("ESCALATE:".length()).trim();
                    currentEscalation = flag.equalsIgnoreCase("true");

                } else if (!line.isBlank() && currentAnswer.length() > 0) {
                    currentAnswer.append(" ").append(line);
                }
            }

            // Save last pair
            if (currentQuestion != null && !currentAnswer.isEmpty()) {
                entries.add(new FaqEntry(
                        currentQuestion,
                        currentAnswer.toString().trim(),
                        currentEscalation
                ));
            }
        }

        return entries;
    }
    private record FaqEntry(String question, String answer, boolean escalation) {}}