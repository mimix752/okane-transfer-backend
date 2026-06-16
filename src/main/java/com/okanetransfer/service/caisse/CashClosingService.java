package com.okanetransfer.service.caisse;

import com.okanetransfer.entity.Agency;
import com.okanetransfer.entity.CashRegister;
import com.okanetransfer.repository.AgencyRepository;
import com.okanetransfer.repository.CashRegisterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class CashClosingService {

    @Autowired private CashRegisterRepository cashRegisterRepository;
    @Autowired private AgencyRepository agencyRepository;

    @Transactional
    public Map<String, Object> fermerCaisse(Long agentId, BigDecimal soldeReel) {
        CashRegister caisse = cashRegisterRepository.findOpenByAgentId(agentId)
                .orElseThrow(() -> new RuntimeException("Aucune caisse ouverte pour cet agent"));

        BigDecimal soldeSysteme = caisse.getBalance();
        BigDecimal ecart = soldeReel.subtract(soldeSysteme);

        caisse.setOpen(false);
        caisse.setClosedAt(LocalDateTime.now());



        Map<String, Object> result = new HashMap<>();
        result.put("caisse", caisse);
        result.put("soldeSysteme", soldeSysteme);
        result.put("soldeReel", soldeReel);
        result.put("ecart", ecart);

        if (ecart.compareTo(BigDecimal.ZERO) < 0) {
            result.put("statut", "MANQUE");
            result.put("message", "Manque de " + ecart.abs() + " en caisse");
        } else if (ecart.compareTo(BigDecimal.ZERO) > 0) {
            result.put("statut", "SURPLUS");
            result.put("message", "Surplus de " + ecart + " en caisse");
        } else {
            result.put("statut", "OK");
            result.put("message", "Caisse équilibrée");
        }

        return result;
    }
}