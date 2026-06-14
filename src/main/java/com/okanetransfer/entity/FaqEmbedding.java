package com.okanetransfer.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "faq_embeddings")
public class FaqEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_text", nullable = false, length = 500)
    private String questionText;

    @Column(name = "answer_text", nullable = false, length = 1000)
    private String answerText;

    @Column(name = "embedding", columnDefinition = "vector(1024)")
    private String embedding;

    @Column(name = "is_escalation", nullable = false)
    private boolean escalation = false;


    public boolean isEscalation() { return escalation; }
    public void setEscalation(boolean escalation) { this.escalation = escalation; }

    public FaqEmbedding() {}

    public FaqEmbedding(String questionText, String answerText, String embedding, boolean escalation) {
        this.questionText = questionText;
        this.answerText = answerText;
        this.embedding = embedding;
    }

    public Long getId() { return id; }
    public String getQuestionText() { return questionText; }
    public String getAnswerText() { return answerText; }
    public String getEmbedding() { return embedding; }
    public void setEmbedding(String embedding) { this.embedding = embedding; }
}