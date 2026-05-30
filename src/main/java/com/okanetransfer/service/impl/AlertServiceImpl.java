package com.okanetransfer.service.impl;

import com.okanetransfer.dto.response.AlertResponseDTO;
import com.okanetransfer.entity.Agency;
import com.okanetransfer.entity.Alert;
import com.okanetransfer.entity.Currency;
import com.okanetransfer.entity.Transfer;
import com.okanetransfer.enums.AlertLevel;
import com.okanetransfer.enums.AlertType;
import com.okanetransfer.enums.RateSource;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.AgencyRepository;
import com.okanetransfer.repository.AlertRepository;
import com.okanetransfer.repository.TransferRepository;
import com.okanetransfer.service.AlertService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AlertServiceImpl implements AlertService {

    // Seuils configurables (idéalement dans application.properties)
    private static final BigDecimal VOLUME_THRESHOLD_1H =
            BigDecimal.valueOf(500_000); // 500k MAD en 1h = CRITIQUE
    private static final BigDecimal BALANCE_LOW_THRESHOLD =
            BigDecimal.valueOf(50_000);  // < 50k MAD = INFO

    // Fenêtre anti-doublon : pas deux alertes identiques en 30 min
    private static final int DEDUP_MINUTES = 30;

    private final AlertRepository    alertRepository;
    private final AgencyRepository   agencyRepository;
    private final TransferRepository transferRepository;

    public AlertServiceImpl(
            AlertRepository alertRepository,
            AgencyRepository agencyRepository,
            TransferRepository transferRepository) {
        this.alertRepository    = alertRepository;
        this.agencyRepository   = agencyRepository;
        this.transferRepository = transferRepository;
    }


    @Override
    @Transactional(readOnly = true)
    public List<AlertResponseDTO> getAllAlerts(int page, int size) {
        return alertRepository
                .findAll(PageRequest.of(page, size))
                .stream()
                .map(AlertResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertResponseDTO> getUnreadAlerts() {
        return alertRepository
                .findByIsReadFalseOrderByCreatedAtDesc()
                .stream()
                .map(AlertResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertResponseDTO> getByLevel(AlertLevel level) {
        return alertRepository
                .findByLevelOrderByCreatedAtDesc(level)
                .stream()
                .map(AlertResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> countUnread() {
        Map<String, Long> result = new HashMap<>();
        result.put("CRITIQUE", 0L);
        result.put("ATTENTION", 0L);
        result.put("INFO", 0L);

        List<Object[]> counts = alertRepository.countUnreadByLevel();
        for (Object[] row : counts) {
            String level = ((AlertLevel) row[0]).name();
            Long   count = (Long) row[1];
            result.put(level, count);
        }
        result.put("TOTAL",
                result.values().stream()
                        .reduce(0L, Long::sum));
        return result;
    }


    @Override
    @Transactional
    public void markAsRead(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alert not found: " + alertId));
        alert.setRead(true);
        alertRepository.save(alert);
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        List<Alert> unread = alertRepository
                .findByIsReadFalseOrderByCreatedAtDesc();
        for (Alert a : unread) {
            a.setRead(true);
        }
        alertRepository.saveAll(unread);
    }

    @Override
    @Transactional
    public void checkVolumeAnomalies() {

        LocalDateTime oneHourAgo =
                LocalDateTime.now().minusHours(1);

        List<Agency> agencies =
                agencyRepository.findByActive(true);

        for (Agency agency : agencies) {

            // Somme des montants des transferts de cette agence
            // dans la dernière heure
            List<Transfer> recentTransfers =
                    transferRepository.findByCreatedAtBetween(
                                    oneHourAgo, LocalDateTime.now())
                            .stream()
                            .filter(t -> t.getAgency() != null
                                    && t.getAgency().getId()
                                    .equals(agency.getId()))
                            .collect(Collectors.toList());

            BigDecimal hourlyVolume = recentTransfers.stream()
                    .map(Transfer::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (hourlyVolume.compareTo(
                    VOLUME_THRESHOLD_1H) > 0) {

                // Vérifier anti-doublon (30 min)
                boolean alreadyAlerted = alertRepository
                        .existsRecentAlert(
                                AlertType.VOLUME_INHABITUEL,
                                agency.getId(),
                                "AGENCY",
                                LocalDateTime.now()
                                        .minusMinutes(DEDUP_MINUTES)
                        );

                if (!alreadyAlerted) {
                    createAlert(
                            AlertLevel.CRITIQUE,
                            AlertType.VOLUME_INHABITUEL,
                            "Dépassement du seuil de "
                                    + VOLUME_THRESHOLD_1H.toPlainString()
                                    + " MAD en 1h par l'agence "
                                    + agency.getName()
                                    + " (volume: "
                                    + hourlyVolume.toPlainString() + " MAD)",
                            agency.getName(),
                            "AGENCY",
                            agency.getId()
                    );
                }
            }
        }
    }

    @Override
    @Transactional
    public void checkLowBalances() {

        List<Agency> agencies =
                agencyRepository.findByActive(true);

        for (Agency agency : agencies) {

            if (agency.getCurrentBalance() == null) continue;

            if (agency.getCurrentBalance().compareTo(
                    BALANCE_LOW_THRESHOLD) < 0) {

                boolean alreadyAlerted = alertRepository
                        .existsRecentAlert(
                                AlertType.SOLDE_AGENCE_BAS,
                                agency.getId(),
                                "AGENCY",
                                LocalDateTime.now()
                                        .minusMinutes(DEDUP_MINUTES)
                        );

                if (!alreadyAlerted) {
                    createAlert(
                            AlertLevel.INFO,
                            AlertType.SOLDE_AGENCE_BAS,
                            "Pré-approvisionnement inférieur à "
                                    + BALANCE_LOW_THRESHOLD.toPlainString()
                                    + " MAD. Solde actuel: "
                                    + agency.getCurrentBalance().toPlainString()
                                    + " MAD",
                            agency.getName(),
                            "AGENCY",
                            agency.getId()
                    );
                }
            }
        }
    }

    @Override
    @Transactional
    public void createApiFailureAlert(String currencyCode) {

        // Compter les échecs récents dans les 30 dernières minutes
        boolean alreadyAlerted = alertRepository
                .existsRecentAlert(
                        AlertType.ECHEC_API_PARTENAIRE,
                        -1L, // pas d'entité spécifique
                        "GATEWAY",
                        LocalDateTime.now()
                                .minusMinutes(DEDUP_MINUTES)
                );

        if (!alreadyAlerted) {
            createAlert(
                    AlertLevel.ATTENTION,
                    AlertType.ECHEC_API_PARTENAIRE,
                    "Échec de récupération du taux pour "
                            + currencyCode
                            + " via l'API externe.",
                    "Gateway-API-" + currencyCode,
                    "GATEWAY",
                    null
            );
        }
    }

    @Override
    @Transactional
    public void createRateAnomalyAlert(
            Currency currency,
            BigDecimal oldRate,
            BigDecimal newRate,
            BigDecimal variationPercent,
            RateSource source) {

        String sign = variationPercent.compareTo(BigDecimal.ZERO) > 0
                ? "+" : "";

        createAlert(
                AlertLevel.CRITIQUE,
                AlertType.TAUX_CHANGE_ANOMALIE,
                "Variation anormale du taux "
                        + currency.getCode() + "/MAD : "
                        + oldRate + " → " + newRate
                        + " (" + sign + variationPercent + "%). "
                        + "Source: " + source.name(),
                currency.getCode() + "/MAD",
                "CURRENCY",
                currency.getId()
        );
    }


    private void createAlert(AlertLevel level,
                             AlertType type,
                             String description,
                             String entityName,
                             String entityType,
                             Long entityId) {
        Alert alert = Alert.builder()
                .level(level)
                .type(type)
                .description(description)
                .entityName(entityName)
                .entityType(entityType)
                .entityId(entityId)
                .isRead(false)
                .build();

        alertRepository.save(alert);
    }
}