package com.okanetransfer.repository;

import com.okanetransfer.entity.FaqEmbedding;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FaqEmbeddingRepository extends JpaRepository<FaqEmbedding, Long> {

    boolean existsBy();

    @Query(value = """
        SELECT id, question_text, answer_text, embedding,
               is_escalation,
               1 - (embedding <=> CAST(:queryVector AS vector)) AS similarity
        FROM faq_embeddings
        ORDER BY embedding <=> CAST(:queryVector AS vector)
        LIMIT 1
        """, nativeQuery = true)
    List<Object[]> findClosestMatch(@Param("queryVector") String queryVector);

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO faq_embeddings (question_text, answer_text, embedding, is_escalation)
        VALUES (:question, :answer, CAST(:embedding AS vector), :escalation)
        """, nativeQuery = true)
    void insertWithVector(
            @Param("question") String question,
            @Param("answer") String answer,
            @Param("embedding") String embedding,
            @Param("escalation") boolean escalation
    );}

