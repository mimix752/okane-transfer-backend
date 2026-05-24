package com.okanetransfer.controller;

import com.okanetransfer.dto.response.TransferResponseDTO;
import com.okanetransfer.enums.TransferStatus;
import com.okanetransfer.service.TransferService;
import com.okanetransfer.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@Slf4j
public class TransferController {

    private final TransferService transferService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN', 'CLIENT')")
    public ResponseEntity<ApiResponse<TransferResponseDTO>> getById(@PathVariable Long id) {
        log.info("GET /api/transfers/{}", id);
        TransferResponseDTO response = transferService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Transfer retrieved", response));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN', 'CLIENT')")
    public ResponseEntity<ApiResponse<TransferResponseDTO>> getByCode(@PathVariable String code) {
        log.info("GET /api/transfers/code/{}", code);
        TransferResponseDTO response = transferService.getByCode(code);
        return ResponseEntity.ok(ApiResponse.success("Transfer retrieved", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN', 'CLIENT')")
    public ResponseEntity<ApiResponse<List<TransferResponseDTO>>> getAll() {
        log.info("GET /api/transfers");
        List<TransferResponseDTO> response = transferService.getAll();
        return ResponseEntity.ok(ApiResponse.success("Transfers retrieved", response));
    }

    @GetMapping("/sender/{senderId}")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN', 'CLIENT')")
    public ResponseEntity<ApiResponse<List<TransferResponseDTO>>> getBySenderId(@PathVariable Long senderId) {
        log.info("GET /api/transfers/sender/{}", senderId);
        List<TransferResponseDTO> response = transferService.getBySenderId(senderId);
        return ResponseEntity.ok(ApiResponse.success("Transfers retrieved", response));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<TransferResponseDTO>>> getByStatus(@PathVariable TransferStatus status) {
        log.info("GET /api/transfers/status/{}", status);
        List<TransferResponseDTO> response = transferService.getByStatus(status);
        return ResponseEntity.ok(ApiResponse.success("Transfers retrieved", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<TransferResponseDTO>> updateStatus(
            @PathVariable Long id,
            @RequestParam TransferStatus status) {
        log.info("PATCH /api/transfers/{}/status to {}", id, status);
        TransferResponseDTO response = transferService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Transfer status updated", response));
    }
}
