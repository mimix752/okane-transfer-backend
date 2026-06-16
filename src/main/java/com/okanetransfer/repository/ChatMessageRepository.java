package com.okanetransfer.repository;

import com.okanetransfer.entity.ChatMessage;
import com.okanetransfer.entity.User;
import com.okanetransfer.enums.ChatRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findTop5ByUserAndSessionIdOrderByCreatedAtDesc(User user, String sessionId);
    List<ChatMessage> findTop10ByUserAndSessionIdOrderByCreatedAtDesc(User user, String sessionId);
    List<ChatMessage> findByUserAndSessionIdOrderByCreatedAtAsc(User user, String sessionId);
    long countByUserAndRoleAndCreatedAtAfter(User user, ChatRole role, LocalDateTime since);

    long countByUserAndSessionId(User user, String sessionId);

    List<ChatMessage> findTop2ByUserAndSessionIdAndRoleOrderByCreatedAtDesc(
            User user, String sessionId, ChatRole role);

}