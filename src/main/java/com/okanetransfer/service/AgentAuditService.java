package com.okanetransfer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.okanetransfer.entity.AgentAuditTrail;
import com.okanetransfer.entity.Currency;
import com.okanetransfer.repository.AgentAuditTrailRepository;
import com.okanetransfer.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AgentAuditService {

    @Autowired
    private AgentAuditTrailRepository auditTrailRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void logAgentAction(String actionType, String entityType, Long entityId, 
                              String description, Object oldValues, Object newValues) {
        
        String currentUsername = SecurityUtils.getCurrentUsername();
        
        if ("anonymous".equals(currentUsername)) return; // Pas d'utilisateur connecté
        
        AgentAuditTrail audit = new AgentAuditTrail();
        audit.setAgentUsername(currentUsername);
        audit.setActionType(actionType);
        audit.setEntityType(entityType);
        audit.setEntityId(entityId);
        audit.setDescription(description);
        
        // Capturer les informations de la requête HTTP
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            audit.setIpAddress(getClientIpAddress(request));
            audit.setUserAgent(request.getHeader("User-Agent"));
            audit.setSessionId(request.getSession().getId());
        }
        
        // Sérialiser les valeurs en JSON
        if (oldValues != null) {
            audit.setOldValues(toJson(oldValues));
        }
        if (newValues != null) {
            audit.setNewValues(toJson(newValues));
        }
        
        auditTrailRepository.save(audit);
    }

    @Transactional
    public void logTransferCreation(Long transferId, String transferCode, String recipientName,
                                    String amount, Currency currency) {
        logAgentAction(
            "CREATE_TRANSFER",
            "Transfer",
            transferId,
            "Création d'un transfert: " + transferCode + " pour " + recipientName + 
            " - Montant: " + amount + " " + currency,
            null,
            "transferCode=" + transferCode + ", recipient=" + recipientName + 
            ", amount=" + amount + ", currency=" + currency
        );
    }

    @Transactional
    public void logTransferWithdrawal(Long transferId, String transferCode, String recipientPhone) {
        logAgentAction(
            "WITHDRAW_TRANSFER",
            "Transfer",
            transferId,
            "Retrait du transfert: " + transferCode + " par " + recipientPhone,
            null,
            "transferCode=" + transferCode + ", recipientPhone=" + recipientPhone
        );
    }

    @Transactional
    public void logLogin(String ipAddress, String userAgent) {
        String currentUsername = SecurityUtils.getCurrentUsername();
        
        AgentAuditTrail audit = new AgentAuditTrail();
        audit.setAgentUsername(currentUsername);
        audit.setActionType("LOGIN");
        audit.setDescription("Connexion de l'agent");
        audit.setIpAddress(ipAddress);
        audit.setUserAgent(userAgent);
        
        auditTrailRepository.save(audit);
    }

    @Transactional
    public void logLogout() {
        logAgentAction(
            "LOGOUT",
            null,
            null,
            "Déconnexion de l'agent",
            null,
            null
        );
    }

    @Transactional
    public void logCashOperation(String operationType, String amount, String description) {
        logAgentAction(
            "CASH_" + operationType.toUpperCase(),
            "CashRegister",
            null,
            description + " - Montant: " + amount,
            null,
            "amount=" + amount + ", operation=" + operationType
        );
    }

    @Transactional(readOnly = true)
    public List<AgentAuditTrail> getAgentAuditTrail(Long agentId, LocalDateTime from, LocalDateTime to) {
        String username = SecurityUtils.getCurrentUsername();
        
        if (from != null && to != null) {
            return auditTrailRepository.findByAgentAndDateRange(username, from, to);
        } else {
            return auditTrailRepository.findByAgentUsernameOrderByCreatedAtDesc(username);
        }
    }

    @Transactional(readOnly = true)
    public List<AgentAuditTrail> getEntityAuditTrail(String entityType, Long entityId) {
        return auditTrailRepository.findByEntityTypeAndId(entityType, entityId);
    }

    @Transactional(readOnly = true)
    public long getAgentActivityCount(Long agentId, LocalDateTime since) {
        String username = SecurityUtils.getCurrentUsername();
        return auditTrailRepository.countAgentActionsSince(username, since);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return object.toString();
        }
    }
}