package com.okanetransfer.service.caisse;

import com.okanetransfer.entity.Agent;
import com.okanetransfer.entity.CashRegister;
import com.okanetransfer.repository.AgentRepository;
import com.okanetransfer.repository.CashRegisterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class CashRegisterService {

    @Autowired private CashRegisterRepository cashRegisterRepository;
    @Autowired private AgentRepository agentRepository;

    @Transactional
    public CashRegister ouvrirCaisse(Long agentId) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent introuvable: " + agentId));

        cashRegisterRepository.findOpenByAgentId(agentId).ifPresent(c -> {
            throw new RuntimeException("Une caisse est déjà ouverte pour cet agent");
        });

        CashRegister caisse = new CashRegister();
        caisse.setAgent(agent);
        caisse.setAgency(agent.getAgency());
        caisse.setBalance(BigDecimal.ZERO);
        caisse.setOpenedAt(LocalDateTime.now());
        caisse.setOpen(true);
        return cashRegisterRepository.save(caisse);
    }

    @Transactional
    public void crediter(Long agentId, BigDecimal montant) {
        CashRegister caisse = cashRegisterRepository.findOpenByAgentId(agentId)
                .orElseThrow(() -> new RuntimeException("Aucune caisse ouverte pour cet agent"));
        caisse.setBalance(caisse.getBalance().add(montant));
    }
}
