package com.okanetransfer.service;

import com.okanetransfer.dto.response.AlertResponseDTO;
import com.okanetransfer.entity.Currency;
import com.okanetransfer.enums.AlertLevel;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface AlertService {

    // Récupérer toutes les alertes (paginées)
    List<AlertResponseDTO> getAllAlerts(int page, int size);

    // Alertes non lues uniquement
    List<AlertResponseDTO> getUnreadAlerts();

    // Alertes par niveau : CRITIQUE / ATTENTION / INFO
    List<AlertResponseDTO> getByLevel(AlertLevel level);

    // Marquer une alerte comme lue
    void markAsRead(Long alertId);

    // Marquer toutes comme lues
    void markAllAsRead();

    // Compter les alertes non lues (pour badge)
    Map<String, Long> countUnread();

    // ── Méthodes appelées automatiquement ───────────────────

    // Vérifier les volumes anormaux sur toutes les agences
    void checkVolumeAnomalies();

    // Vérifier les soldes bas sur toutes les agences
    void checkLowBalances();

    // Créer une alerte pour échec API partenaire
    void createApiFailureAlert(String currencyCode);

    // Créer une alerte pour variation de taux anormale
    void createRateAnomalyAlert(
            Currency currency,
            BigDecimal oldRate,
            BigDecimal newRate,
            BigDecimal variationPercent,
            com.okanetransfer.enums.RateSource source
    );
}