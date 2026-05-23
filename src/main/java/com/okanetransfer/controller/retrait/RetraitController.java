package com.okanetransfer.controller.retrait;

import com.okanetransfer.dto.request.RetraitRequestDTO;
import com.okanetransfer.dto.response.RetraitResponseDTO;
import com.okanetransfer.service.retrait.RetraitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/retrait")
public class RetraitController {

    @Autowired private RetraitService retraitService;

    @PostMapping
    public ResponseEntity<RetraitResponseDTO> retirer(@Valid @RequestBody RetraitRequestDTO dto) {
        return ResponseEntity.ok(retraitService.retirer(dto));
    }
}
