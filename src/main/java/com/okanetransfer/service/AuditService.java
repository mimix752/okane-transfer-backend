package com.okanetransfer.service;

import com.okanetransfer.dto.response.JournalAuditResponseDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AuditService {


    void log(String performedBy,
             String action,
             String entityType,
             Long entityId,
             String details);


    JournalAuditResponseDTO getById(Long id);

    Map<String, Object> search(String performedBy,
                               String action,
                               String entityType,
                               Long entityId,
                               LocalDateTime from,
                               LocalDateTime to,
                               int page,
                               int size);

    List<JournalAuditResponseDTO> getHistoryForEntity(
            String entityType, Long entityId);
}