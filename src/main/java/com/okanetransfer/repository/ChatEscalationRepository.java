package com.okanetransfer.repository;

import com.okanetransfer.entity.ChatEscalation;
import com.okanetransfer.enums.EscalationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatEscalationRepository extends JpaRepository<ChatEscalation, Long> {

    List<ChatEscalation> findByStatusOrderByCreatedAtDesc(EscalationStatus status);
}