package com.okanetransfer.service;

import com.okanetransfer.dto.response.TransferResponseDTO;
import com.okanetransfer.entity.Transfer;
import com.okanetransfer.enums.TransferStatus;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final TransferRepository transferRepository;

    @Transactional(readOnly = true)
    public TransferResponseDTO getById(Long id) {
        log.info("Fetching transfer with id: {}", id);
        Transfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer not found"));
        return TransferResponseDTO.fromEntity(transfer);
    }

    @Transactional(readOnly = true)
    public TransferResponseDTO getByCode(String code) {
        log.info("Fetching transfer with code: {}", code);
        Transfer transfer = transferRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer not found"));
        return TransferResponseDTO.fromEntity(transfer);
    }

    @Transactional(readOnly = true)
    public List<TransferResponseDTO> getBySenderId(Long senderId) {
        log.info("Fetching transfers for sender: {}", senderId);
        return transferRepository.findBySenderId(senderId)
                .stream()
                .map(TransferResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransferResponseDTO> getAll() {
        log.info("Fetching all transfers");
        return transferRepository.findAll()
                .stream()
                .map(TransferResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransferResponseDTO> getByStatus(TransferStatus status) {
        log.info("Fetching transfers with status: {}", status);
        return transferRepository.findByStatus(status)
                .stream()
                .map(TransferResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public TransferResponseDTO updateStatus(Long id, TransferStatus newStatus) {
        log.info("Updating transfer {} status to {}", id, newStatus);
        Transfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer not found"));
        
        transfer.setStatus(newStatus);
        Transfer updated = transferRepository.save(transfer);
        
        return TransferResponseDTO.fromEntity(updated);
    }
}
