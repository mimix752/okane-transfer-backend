package com.okanetransfer.service.caisse;

import com.okanetransfer.entity.CashRegister;
import com.okanetransfer.repository.CashRegisterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CashClosingService {

    @Autowired private CashRegisterRepository cashRegisterRepository;

    @Transactional
    public CashRegister fermerCaisse(Long agentId) {
        CashRegister caisse = cashRegisterRepository.findOpenByAgentId(agentId)
                .orElseThrow(() -> new RuntimeException("Aucune caisse ouverte pour cet agent"));
        caisse.setOpen(false);
        caisse.setClosedAt(LocalDateTime.now());
        return caisse;
    }
}
