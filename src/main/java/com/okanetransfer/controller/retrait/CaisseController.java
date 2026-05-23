package com.okanetransfer.controller.retrait;

import com.okanetransfer.entity.CashRegister;
import com.okanetransfer.service.caisse.CashClosingService;
import com.okanetransfer.service.caisse.CashRegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/caisse")
public class CaisseController {

    @Autowired private CashRegisterService cashRegisterService;
    @Autowired private CashClosingService cashClosingService;

    @PostMapping("/ouvrir/{agentId}")
    public ResponseEntity<CashRegister> ouvrir(@PathVariable Long agentId) {
        return ResponseEntity.ok(cashRegisterService.ouvrirCaisse(agentId));
    }

    @PostMapping("/fermer/{agentId}")
    public ResponseEntity<CashRegister> fermer(@PathVariable Long agentId) {
        return ResponseEntity.ok(cashClosingService.fermerCaisse(agentId));
    }
}
