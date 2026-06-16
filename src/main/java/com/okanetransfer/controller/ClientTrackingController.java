package com.okanetransfer.controller;

import com.okanetransfer.entity.Transfer;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.TransferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/client")
public class ClientTrackingController {

    @Autowired private TransferRepository transferRepository;

    @GetMapping("/transfert/{code}")
    public ResponseEntity<Map<String, Object>> suivre(@PathVariable String code) {
        Transfer t = transferRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Transfert introuvable: " + code));

        return ResponseEntity.ok(Map.of(
                "code", t.getTransferCode(),
                "statut", t.getStatus(),
                "beneficiaire", t.getRecipientName(),
                "paysDestination", t.getRecipientCountry(),
                "montant", t.getConvertedAmount(),
                "devise", t.getTargetCurrency(),
                "date", t.getCreatedAt()
        ));
    }
}
