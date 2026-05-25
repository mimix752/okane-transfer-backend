package com.okanetransfer.service;

import com.okanetransfer.dto.response.TransferResponseDTO;
import com.okanetransfer.entity.Transfer;
import com.okanetransfer.enums.TransferStatus;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.TransferRepository;
import com.okanetransfer.service.AgencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransferService {

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private AgencyService agencyService;

    @Transactional(readOnly = true)
    public TransferResponseDTO getById(Long id) {
        Transfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer not found"));
        return TransferResponseDTO.fromEntity(transfer);
    }

    @Transactional(readOnly = true)
    public TransferResponseDTO getByCode(String code) {
        Transfer transfer = transferRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer not found"));
        return TransferResponseDTO.fromEntity(transfer);
    }

    @Transactional(readOnly = true)
    public List<TransferResponseDTO> getBySenderId(Long senderId) {
        return transferRepository.findBySenderId(senderId)
                .stream()
                .map(TransferResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransferResponseDTO> getAll() {
        return transferRepository.findAll()
                .stream()
                .map(TransferResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransferResponseDTO> getByStatus(TransferStatus status) {
        return transferRepository.findByStatus(status)
                .stream()
                .map(TransferResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public TransferResponseDTO updateStatus(Long id, TransferStatus newStatus) {
        Transfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer not found"));

        TransferStatus oldStatus = transfer.getStatus();
        
        // Restaurer le solde si le transfert est annulé
        if (newStatus == TransferStatus.CANCELLED && oldStatus == TransferStatus.PENDING) {
            agencyService.addBalance(transfer.getAgency().getId(), transfer.getAmount());
        }

        transfer.setStatus(newStatus);
        Transfer updated = transferRepository.save(transfer);

        return TransferResponseDTO.fromEntity(updated);
    }
}