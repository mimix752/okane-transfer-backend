package com.okanetransfer.controller.retrait;

import com.okanetransfer.entity.CashOperation;
import com.okanetransfer.entity.CashRegister;
import com.okanetransfer.repository.CashOperationRepository;
import com.okanetransfer.repository.CashRegisterRepository;
import com.okanetransfer.service.caisse.CashClosingService;
import com.okanetransfer.service.caisse.CashRegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/caisse")
public class CaisseController {

    @Autowired private CashRegisterService cashRegisterService;
    @Autowired private CashClosingService cashClosingService;
    @Autowired private CashRegisterRepository cashRegisterRepository;
    @Autowired private CashOperationRepository cashOperationRepository;

    @PostMapping("/ouvrir/{agentId}")
    public ResponseEntity<CashRegister> ouvrir(@PathVariable Long agentId) {
        return ResponseEntity.ok(cashRegisterService.ouvrirCaisse(agentId));
    }

    // Solde en temps réel
    @GetMapping("/solde/{agentId}")
    public ResponseEntity<Map<String, Object>> solde(@PathVariable Long agentId) {
        CashRegister caisse = cashRegisterRepository.findOpenByAgentId(agentId)
                .orElseThrow(() -> new RuntimeException("Aucune caisse ouverte pour cet agent"));
        return ResponseEntity.ok(Map.of(
                "solde", caisse.getBalance(),
                "ouverteLe", caisse.getOpenedAt(),
                "caisseId", caisse.getId()
        ));
    }

    // Historique des opérations du jour
    @GetMapping("/historique/{agentId}")
    public ResponseEntity<List<CashOperation>> historique(@PathVariable Long agentId) {
        CashRegister caisse = cashRegisterRepository.findOpenByAgentId(agentId)
                .orElseThrow(() -> new RuntimeException("Aucune caisse ouverte pour cet agent"));
        LocalDateTime debut = LocalDate.now().atStartOfDay();
        LocalDateTime fin = debut.plusDays(1);
        return ResponseEntity.ok(cashOperationRepository.findByCashRegisterAndDay(caisse.getId(), debut, fin));
    }

    // Clôture avec réconciliation
    @PostMapping("/fermer/{agentId}")
    public ResponseEntity<Map<String, Object>> fermer(
            @PathVariable Long agentId,
            @RequestParam BigDecimal soldeReel) {
        return ResponseEntity.ok(cashClosingService.fermerCaisse(agentId, soldeReel));
    }
}
