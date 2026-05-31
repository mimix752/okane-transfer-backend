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
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/caisse")
public class CaisseController {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Autowired private CashRegisterService cashRegisterService;
    @Autowired private CashClosingService cashClosingService;
    @Autowired private CashRegisterRepository cashRegisterRepository;
    @Autowired private CashOperationRepository cashOperationRepository;

    @PostMapping("/ouvrir/{agentId}")
    public ResponseEntity<Map<String, Object>> ouvrir(@PathVariable Long agentId) {
        CashRegister c = cashRegisterService.ouvrirCaisse(agentId);
        return ResponseEntity.ok(toCaisseMap(c));
    }

    @GetMapping("/solde/{agentId}")
    public ResponseEntity<Map<String, Object>> solde(@PathVariable Long agentId) {
        CashRegister c = cashRegisterRepository.findOpenByAgentId(agentId)
                .orElseThrow(() -> new RuntimeException("Aucune caisse ouverte pour cet agent"));
        Map<String, Object> result = new HashMap<>();
        result.put("solde", c.getBalance());
        result.put("ouverteLe", c.getOpenedAt() != null ? c.getOpenedAt().format(ISO) : null);
        result.put("caisseId", c.getId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/historique/{agentId}")
    public ResponseEntity<List<Map<String, Object>>> historique(@PathVariable Long agentId) {
        CashRegister c = cashRegisterRepository.findOpenByAgentId(agentId)
                .orElseThrow(() -> new RuntimeException("Aucune caisse ouverte pour cet agent"));
        LocalDateTime debut = LocalDate.now().atStartOfDay();
        LocalDateTime fin = debut.plusDays(1);
        List<Map<String, Object>> ops = cashOperationRepository
                .findByCashRegisterAndDay(c.getId(), debut, fin)
                .stream()
                .map(this::toOperationMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ops);
    }

    @PostMapping("/fermer/{agentId}")
    public ResponseEntity<Map<String, Object>> fermer(
            @PathVariable Long agentId,
            @RequestParam BigDecimal soldeReel) {
        Map<String, Object> result = cashClosingService.fermerCaisse(agentId, soldeReel);
        // Convertir les dates dans le résultat
        if (result.get("caisse") instanceof CashRegister c) {
            result.put("caisse", toCaisseMap(c));
        }
        return ResponseEntity.ok(result);
    }

    private Map<String, Object> toCaisseMap(CashRegister c) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", c.getId());
        m.put("balance", c.getBalance());
        m.put("openedAt", c.getOpenedAt() != null ? c.getOpenedAt().format(ISO) : null);
        m.put("closedAt", c.getClosedAt() != null ? c.getClosedAt().format(ISO) : null);
        m.put("open", c.isOpen());
        return m;
    }

    private Map<String, Object> toOperationMap(CashOperation op) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", op.getId());
        m.put("type", op.getType());
        m.put("amount", op.getAmount());
        m.put("balanceAfter", op.getBalanceAfter());
        m.put("transferCode", op.getTransferCode());
        m.put("createdAt", op.getCreatedAt() != null ? op.getCreatedAt().format(ISO) : null);
        return m;
    }
}
