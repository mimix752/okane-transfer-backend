package com.okanetransfer.controller;

import com.okanetransfer.dto.response.PageResponse;
import com.okanetransfer.dto.response.TransferResponseDTO;
import com.okanetransfer.enums.TransferStatus;
import com.okanetransfer.service.TransferService;
import com.okanetransfer.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    @Autowired
    private TransferService transferService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN', 'CLIENT')")
    public ResponseEntity<ApiResponse<TransferResponseDTO>> getById(@PathVariable Long id) {
        TransferResponseDTO response = transferService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Transfer retrieved", response));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN', 'CLIENT')")
    public ResponseEntity<ApiResponse<TransferResponseDTO>> getByCode(@PathVariable String code) {
        TransferResponseDTO response = transferService.getByCode(code);
        return ResponseEntity.ok(ApiResponse.success("Transfer retrieved", response));
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN', 'CLIENT')")
    public ResponseEntity<ApiResponse<PageResponse<TransferResponseDTO>>> getAllPaginated(Pageable pageable) {
        PageResponse<TransferResponseDTO> response = transferService.getAllPaginated(pageable);
        return ResponseEntity.ok(ApiResponse.success("Transfers retrieved", response));
    }

    @GetMapping("/sender/{senderId}")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN', 'CLIENT')")
    public ResponseEntity<ApiResponse<List<TransferResponseDTO>>> getBySenderId(@PathVariable Long senderId) {
        List<TransferResponseDTO> response = transferService.getBySenderId(senderId);
        return ResponseEntity.ok(ApiResponse.success("Transfers retrieved", response));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<TransferResponseDTO>>> getByStatus(@PathVariable TransferStatus status) {
        List<TransferResponseDTO> response = transferService.getByStatus(status);
        return ResponseEntity.ok(ApiResponse.success("Transfers retrieved", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<TransferResponseDTO>> updateStatus(
            @PathVariable Long id,
            @RequestParam TransferStatus status) {
        TransferResponseDTO response = transferService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Transfer status updated", response));
    }
}