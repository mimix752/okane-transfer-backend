package com.okanetransfer.service.retrait;

import com.okanetransfer.dto.request.RetraitRequestDTO;
import com.okanetransfer.dto.response.RetraitResponseDTO;
import com.okanetransfer.entity.Transfer;
import com.okanetransfer.enums.TransferStatus;
import com.okanetransfer.exception.TransferNotFoundException;
import com.okanetransfer.repository.TransferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RetraitService {

    @Autowired private TransferRepository transferRepository;
    @Autowired private VerificationRetraitService verificationService;

    @Transactional
    public RetraitResponseDTO retirer(RetraitRequestDTO dto) {
        Transfer t = transferRepository.findByCode(dto.getTransferCode())
                .orElseThrow(() -> new TransferNotFoundException("Transfert introuvable: " + dto.getTransferCode()));

        verificationService.verifier(t, dto.getRecipientPhone());

        t.setStatus(TransferStatus.PAID);
        return toDTO(t);
    }

    private RetraitResponseDTO toDTO(Transfer t) {
        RetraitResponseDTO dto = new RetraitResponseDTO();
        dto.setId(t.getId());
        dto.setTransferCode(t.getTransferCode());
        dto.setRecipientName(t.getRecipientName());
        dto.setPaidAmount(t.getConvertedAmount());
        dto.setStatus(t.getStatus());
        dto.setPaidAt(LocalDateTime.now());
        return dto;
    }
}
