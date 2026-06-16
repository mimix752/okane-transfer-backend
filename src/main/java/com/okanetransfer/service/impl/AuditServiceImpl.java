package com.okanetransfer.service.impl;

import com.okanetransfer.dto.response.JournalAuditResponseDTO;
import com.okanetransfer.entity.JournalAudit;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.JournalAuditRepository;
import com.okanetransfer.service.AuditService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuditServiceImpl implements AuditService {

    private final JournalAuditRepository repository;

    public AuditServiceImpl(JournalAuditRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void log(String performedBy,
                    String action,
                    String entityType,
                    Long entityId,
                    String details) {

        JournalAudit audit = new JournalAudit();
        audit.setPerformedBy(performedBy);
        audit.setAction(action);
        audit.setEntityType(entityType);
        audit.setEntityId(entityId);
        audit.setDetails(details);

        repository.save(audit);
    }

    @Override
    public JournalAuditResponseDTO getById(Long id) {
        JournalAudit audit = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Audit log not found with id: " + id));
        return toDTO(audit);
    }

    @Override
    public Map<String, Object> search(String performedBy,
                                      String action,
                                      String entityType,
                                      Long entityId,
                                      LocalDateTime from,
                                      LocalDateTime to,
                                      int page,
                                      int size) {

        if (page < 0) page = 0;
        if (size < 1 || size > 200) size = 50;

        int offset = page * size;

        List<JournalAudit> rows = repository.findWithFilters(
                performedBy, action, entityType,
                entityId, from, to, offset, size);

        long total = repository.countWithFilters(
                performedBy, action, entityType,
                entityId, from, to);

        Map<String, Object> result = new HashMap<>();
        result.put("data",  toDTOList(rows));
        result.put("total", total);
        result.put("page",  page);
        result.put("size",  size);
        result.put("pages", (long) Math.ceil((double) total / size));
        return result;
    }

    @Override
    public List<JournalAuditResponseDTO> getHistoryForEntity(
            String entityType, Long entityId) {

        return toDTOList(
                repository.findByEntityTypeAndEntityId(
                        entityType, entityId));
    }

    public JournalAuditResponseDTO toDTO(JournalAudit entity) {
        if (entity == null) return null;
        return new JournalAuditResponseDTO(
                entity.getId(),
                entity.getPerformedBy(),
                entity.getAction(),
                entity.getEntityType(),
                entity.getEntityId(),
                entity.getPerformedAt(),
                entity.getDetails()
        );
    }

        public List<JournalAuditResponseDTO> toDTOList(List<JournalAudit> list) {
            return list.stream().map(this::toDTO).toList();
        }
}
