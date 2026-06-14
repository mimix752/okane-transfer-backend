package com.okanetransfer.service.impl;

import com.okanetransfer.dto.response.AlertResponseDTO;
import com.okanetransfer.entity.*;
import com.okanetransfer.entity.Currency;
import com.okanetransfer.enums.*;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.*;
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

    private static final BigDecimal DEFAULT_VOLUME_THRESHOLD =
            BigDecimal.valueOf(500_000);
    private static final BigDecimal DEFAULT_BALANCE_THRESHOLD =
            BigDecimal.valueOf(50_000);
    private static final BigDecimal DEFAULT_RATE_THRESHOLD =
            BigDecimal.valueOf(5.0);
    private static final int DEFAULT_DEDUP_MINUTES = 30;

    private final AlertRepository          alertRepository;
    private final AlertThresholdRepository thresholdRepository;
    private final AgencyRepository         agencyRepository;
    private final TransferRepository       transferRepository;

    public AlertServiceImpl(
            AlertRepository alertRepository,
            AlertThresholdRepository thresholdRepository,
            AgencyRepository agencyRepository,
            TransferRepository transferRepository) {
        this.alertRepository     = alertRepository;
        this.thresholdRepository = thresholdRepository;
        this.agencyRepository    = agencyRepository;
        this.transferRepository  = transferRepository;
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

        List<Object[]> counts =
                alertRepository.countUnreadByLevel();
        for (Object[] row : counts) {
            String key   = ((AlertLevel) row[0]).name();
            Long   count = (Long) row[1];
            result.put(key, count);
        }
        long total = result.values().stream()
                .reduce(0L, Long::sum);
        result.put("TOTAL", total);
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
        for (Alert a : unread) a.setRead(true);
        alertRepository.saveAll(unread);
    }


    @Override
    @Transactional
    public void checkVolumeAnomalies() {

        // Lire le seuil depuis la base (ou défaut)
        BigDecimal threshold = getThreshold(
                AlertType.VOLUME_INHABITUEL,
                DEFAULT_VOLUME_THRESHOLD);

        int dedupMin = getDedupMinutes(
                AlertType.VOLUME_INHABITUEL);

        // Vérifier si ce type d'alerte est activé
        if (!isAlertEnabled(AlertType.VOLUME_INHABITUEL)) return;

        LocalDateTime oneHourAgo =
                LocalDateTime.now().minusHours(1);

        for (Agency agency : agencyRepository.findByActive(true)) {

            BigDecimal hourlyVolume =
                    transferRepository.findByCreatedAtBetween(
                            oneHourAgo, LocalDateTime.now())
                    .stream()
                    .filter(t -> t.getAgency() != null
                            && agency.getId().equals(
                                    t.getAgency().getId()))
                    .map(Transfer::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (hourlyVolume.compareTo(threshold) > 0) {

                boolean alreadyAlerted =
                        alertRepository.existsRecentAlert(
                            AlertType.VOLUME_INHABITUEL,
                            agency.getId(),
                            "AGENCY",
                            LocalDateTime.now()
                                .minusMinutes(dedupMin));

                if (!alreadyAlerted) {
                    createAlert(
                        AlertLevel.CRITIQUE,
                        AlertType.VOLUME_INHABITUEL,
                        "Dépassement du seuil de "
                        + threshold.toPlainString()
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

        BigDecimal threshold = getThreshold(
                AlertType.SOLDE_AGENCE_BAS,
                DEFAULT_BALANCE_THRESHOLD);

        int dedupMin = getDedupMinutes(
                AlertType.SOLDE_AGENCE_BAS);

        if (!isAlertEnabled(AlertType.SOLDE_AGENCE_BAS)) return;

        for (Agency agency : agencyRepository.findByActive(true)) {

            if (agency.getCurrentBalance() == null) continue;

            if (agency.getCurrentBalance()
                      .compareTo(threshold) < 0) {

                boolean alreadyAlerted =
                        alertRepository.existsRecentAlert(
                            AlertType.SOLDE_AGENCE_BAS,
                            agency.getId(),
                            "AGENCY",
                            LocalDateTime.now()
                                .minusMinutes(dedupMin));

                if (!alreadyAlerted) {
                    createAlert(
                        AlertLevel.INFO,
                        AlertType.SOLDE_AGENCE_BAS,
                        "Pré-approvisionnement inférieur à "
                        + threshold.toPlainString()
                        + " MAD. Solde actuel: "
                        + agency.getCurrentBalance()
                                .toPlainString() + " MAD",
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

        int dedupMin = getDedupMinutes(
                AlertType.ECHEC_API_PARTENAIRE);

        if (!isAlertEnabled(AlertType.ECHEC_API_PARTENAIRE))
            return;

        boolean alreadyAlerted = alertRepository.existsRecentAlert(
                AlertType.ECHEC_API_PARTENAIRE,
                -1L, "GATEWAY",
                LocalDateTime.now().minusMinutes(dedupMin));

        if (!alreadyAlerted) {
            createAlert(
                AlertLevel.ATTENTION,
                AlertType.ECHEC_API_PARTENAIRE,
                "Échec de récupération du taux pour "
                + currencyCode + " via l'API externe.",
                "Gateway-API-" + currencyCode,
                "GATEWAY", null
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

        BigDecimal threshold = getThreshold(
                AlertType.TAUX_CHANGE_ANOMALIE,
                DEFAULT_RATE_THRESHOLD);

        if (variationPercent.abs().compareTo(threshold) < 0)
            return;

        if (!isAlertEnabled(AlertType.TAUX_CHANGE_ANOMALIE))
            return;

        String sign = newRate.compareTo(oldRate) > 0 ? "+" : "";

        createAlert(
            AlertLevel.CRITIQUE,
            AlertType.TAUX_CHANGE_ANOMALIE,
            "Variation anormale du taux "
            + currency.getCode() + "/USD : "
            + oldRate + " → " + newRate
            + " (" + sign + variationPercent + "%). "
            + "Source: " + source.name(),
            currency.getCode() + "/USD",
            "CURRENCY",
            currency.getId()
        );
    }

    private BigDecimal getThreshold(AlertType type,
                                     BigDecimal defaultValue) {
        return thresholdRepository
                .findByAlertType(type)
                .map(AlertThreshold::getThresholdValue)
                .orElse(defaultValue);
    }

    private int getDedupMinutes(AlertType type) {
        return thresholdRepository
                .findByAlertType(type)
                .map(AlertThreshold::getDedupMinutes)
                .orElse(DEFAULT_DEDUP_MINUTES);
    }

    private boolean isAlertEnabled(AlertType type) {
        return thresholdRepository
                .findByAlertType(type)
                .map(AlertThreshold::isEnabled)
                .orElse(true);
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
