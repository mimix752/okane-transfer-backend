package com.okanetransfer.repository;

import com.okanetransfer.entity.JournalAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<JournalAudit, Long> {

    List<JournalAudit> findByPerformedBy(String performedBy);

    List<JournalAudit> findByEntityType(String entityType);

    List<JournalAudit> findByPerformedAtBetween(LocalDateTime start, LocalDateTime end);
}