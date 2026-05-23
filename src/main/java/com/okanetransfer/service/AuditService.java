package com.okanetransfer.service;

import com.okanetransfer.entity.JournalAudit;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditService {

    void logAction(String performedBy, String action,
                   String entityType, Long entityId,
                   String oldValue, String newValue,
                   String ip);

    List<JournalAudit> getByUser(String performedBy);

    List<JournalAudit> getByPeriod(LocalDateTime start, LocalDateTime end);
}