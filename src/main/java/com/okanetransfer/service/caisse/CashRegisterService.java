package com.okanetransfer.service.caisse;

import com.okanetransfer.entity.Agency;
import com.okanetransfer.entity.Agent;
import com.okanetransfer.entity.CashOperation;
import com.okanetransfer.entity.CashRegister;
import com.okanetransfer.repository.AgentRepository;
import com.okanetransfer.repository.CashOperationRepository;
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
    @Autowired private CashOperationRepository cashOperationRepository;

    @Transactional
    public CashRegister ouvrirCaisse(Long agentId) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent introuvable: " + agentId));

        Agency agency = agent.getAgency();
        if (agency == null) {
            throw new IllegalStateException("Agent has no agency");
        }

        if (!agency.isActive()) {
            throw new IllegalStateException("Agency is suspended");
        }

        cashRegisterRepository.findOpenByAgentId(agentId).ifPresent(c -> {
            throw new RuntimeException("Une caisse est déjà ouverte pour cet agent");
        });

        BigDecimal openingBalance = agency.getCurrentBalance() != null
                ? agency.getCurrentBalance()
                : BigDecimal.ZERO;

        CashRegister caisse = new CashRegister();
        caisse.setAgent(agent);
        caisse.setAgency(agency);
        caisse.setOpeningBalance(openingBalance);
        caisse.setBalance(openingBalance);
        caisse.setOpenedAt(LocalDateTime.now());
        caisse.setOpen(true);
        caisse.setCurrencyCode(
                agency.getCurrencyCode() != null
                        ? agency.getCurrencyCode()
                        : "MAD"
        );

        return cashRegisterRepository.save(caisse);
    }

    @Transactional
    public void debiter(Long agentId, BigDecimal amount, String type, String transferCode) {
        CashRegister caisse = cashRegisterRepository.findOpenByAgentId(agentId)
                .orElseThrow(() -> new RuntimeException("Aucune caisse ouverte pour cet agent"));

        BigDecimal newBalance = caisse.getBalance().subtract(amount);
        caisse.setBalance(newBalance);

        CashOperation op = new CashOperation();
        op.setCashRegister(caisse);
        op.setType(type);
        op.setAmount(amount);
        op.setBalanceAfter(newBalance);
        op.setTransferCode(transferCode);
        cashOperationRepository.save(op);
    }

    @Transactional
    public void crediter(Long agentId, BigDecimal amount, String type, String transferCode) {
        CashRegister caisse = cashRegisterRepository.findOpenByAgentId(agentId)
                .orElseThrow(() -> new RuntimeException("Aucune caisse ouverte pour cet agent"));

        BigDecimal newBalance = caisse.getBalance().add(amount);
        caisse.setBalance(newBalance);

        CashOperation op = new CashOperation();
        op.setCashRegister(caisse);
        op.setType(type);
        op.setAmount(amount);
        op.setBalanceAfter(newBalance);
        op.setTransferCode(transferCode);
        cashOperationRepository.save(op);
    }
}