package com.okanetransfer.service;

import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import org.springframework.stereotype.Component;

@Component
public class LanguageDetectorService {

    private final LanguageDetector detector;

    public LanguageDetectorService() {
        this.detector = LanguageDetectorBuilder
                .fromLanguages(
                        Language.FRENCH,
                        Language.ENGLISH,
                        Language.ARABIC,
                        Language.SPANISH


                )
                .build();
    }

    public String detect(String text) {
        if (text == null || text.isBlank()) return "fr";
        return detector.detectLanguageOf(text)
                .getIsoCode639_1()
                .toString();
    }
}