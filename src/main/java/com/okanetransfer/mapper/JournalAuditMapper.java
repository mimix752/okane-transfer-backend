package com.okanetransfer.mapper;

import com.okanetransfer.dto.response.JournalAuditResponseDTO;
import com.okanetransfer.entity.JournalAudit;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JournalAuditMapper {

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