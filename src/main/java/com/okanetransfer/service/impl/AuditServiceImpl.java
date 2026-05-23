package com.okanetransfer.service.impl;

import com.okanetransfer.entity.JournalAudit;
import com.okanetransfer.repository.AuditRepository;
import com.okanetransfer.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditRepository auditRepository;

    @Transactional
    @Override
    public void logAction(String performedBy, String action,
                          String entityType, Long entityId,
                          String oldValue, String newValue,
                          String ip) {

        String details = String.format(
                "ip=%s | old=%s | new=%s",
                ip,
                oldValue != null ? oldValue : "N/A",
                newValue != null ? newValue : "N/A"
        );

        JournalAudit log = new JournalAudit(
                null,
                performedBy,
                action,
                entityType,
                entityId,
                null, // performedAt set by @PrePersist
                details
        );

        auditRepository.save(log);
    }

    @Transactional(readOnly = true)
    @Override
    public List<JournalAudit> getByUser(String performedBy) {
        return auditRepository.findByPerformedBy(performedBy);
    }

    @Transactional(readOnly = true)
    @Override
    public List<JournalAudit> getByPeriod(LocalDateTime start, LocalDateTime end) {
        return auditRepository.findByPerformedAtBetween(start, end);
    }
}