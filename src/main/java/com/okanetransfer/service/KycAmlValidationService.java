package com.okanetransfer.service;

import com.okanetransfer.entity.AmlAlert;
import com.okanetransfer.entity.KycProfile;
import com.okanetransfer.enums.KycStatus;
import com.okanetransfer.enums.RiskLevel;
import com.okanetransfer.exception.KycAmlValidationException;
import com.okanetransfer.repository.AmlAlertRepository;
import com.okanetransfer.repository.KycProfileRepository;
import com.okanetransfer.repository.TransferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class KycAmlValidationService {

    @Autowired
    private KycProfileRepository kycProfileRepository;

    @Autowired
    private AmlAlertRepository amlAlertRepository;

    @Autowired
    private TransferRepository transferRepository;

    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("10000");
    private static final BigDecimal CRITICAL_AMOUNT_THRESHOLD = new BigDecimal("50000");
    private static final List<String> HIGH_RISK_COUNTRIES = Arrays.asList("AF", "IR", "KP", "SY");

    @Transactional
    public void validateTransfer(String senderDocNumber, String recipientDocNumber, 
                               BigDecimal amount, String senderCountry, String recipientCountry) {
        
        // 1. Validation KYC de l'expéditeur
        validateSenderKyc(senderDocNumber);
        
        // 2. Validation du bénéficiaire
        validateRecipientKyc(recipientDocNumber);
        
        // 3. Contrôles AML
        performAmlChecks(senderDocNumber, amount, senderCountry, recipientCountry);
        
        // 4. Vérification des alertes existantes
        checkExistingAlerts(senderDocNumber);
    }

    private void validateSenderKyc(String documentNumber) {
        Optional<KycProfile> profile = kycProfileRepository.findByDocumentNumber(documentNumber);

        if (profile.isEmpty()) {
            // Créer automatiquement un profil KYC approuvé
            KycProfile newProfile = new KycProfile();
            newProfile.setDocumentNumber(documentNumber);
            newProfile.setDocumentType("CIN");
            newProfile.setFullName(documentNumber);
            newProfile.setNationality("MA");
            newProfile.setKycStatus(KycStatus.APPROVED);
            newProfile.setRiskLevel(RiskLevel.LOW);
            newProfile.setVerificationDate(LocalDateTime.now());
            newProfile.setLastReviewDate(LocalDateTime.now());
            kycProfileRepository.save(newProfile);
            return;
        }

        KycProfile kycProfile = profile.get();

        if (kycProfile.getKycStatus() == KycStatus.REJECTED) {
            throw new KycAmlValidationException(
                "Client sur liste noire. Transfert interdit."
            );
        }

        // Renouveler automatiquement si expiré
        if (kycProfile.getLastReviewDate() != null &&
            kycProfile.getLastReviewDate().isBefore(LocalDateTime.now().minusMonths(12))) {
            kycProfile.setKycStatus(KycStatus.APPROVED);
            kycProfile.setLastReviewDate(LocalDateTime.now());
            kycProfileRepository.save(kycProfile);
        }
    }

    private void validateRecipientKyc(String documentNumber) {
        // Validation basique du bénéficiaire (peut être moins stricte)
        Optional<KycProfile> profile = kycProfileRepository.findByDocumentNumber(documentNumber);
        
        if (profile.isPresent() && profile.get().getKycStatus() == KycStatus.REJECTED) {
            throw new KycAmlValidationException(
                "Bénéficiaire sur liste noire. Transfert interdit."
            );
        }
    }

    private void performAmlChecks(String senderDocNumber, BigDecimal amount, 
                                String senderCountry, String recipientCountry) {
        
        // 1. Contrôle des montants élevés
        if (amount.compareTo(HIGH_AMOUNT_THRESHOLD) >= 0) {
            createAmlAlert(senderDocNumber, "HIGH_AMOUNT", 
                "Transfert de montant élevé: " + amount, 
                amount.compareTo(CRITICAL_AMOUNT_THRESHOLD) >= 0 ? RiskLevel.CRITICAL : RiskLevel.HIGH,
                amount);
        }
        
        // 2. Contrôle des pays à haut risque
        if (HIGH_RISK_COUNTRIES.contains(senderCountry) || HIGH_RISK_COUNTRIES.contains(recipientCountry)) {
            createAmlAlert(senderDocNumber, "HIGH_RISK_COUNTRY", 
                "Transfert vers/depuis un pays à haut risque: " + senderCountry + " -> " + recipientCountry,
                RiskLevel.HIGH, amount);
        }
        
        // 3. Contrôle de fréquence des transferts
        checkTransferFrequency(senderDocNumber, amount);
        
        // 4. Contrôle des montants cumulés
        checkCumulativeAmounts(senderDocNumber, amount);
    }

    private void checkTransferFrequency(String documentNumber, BigDecimal amount) {
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        long recentTransfers = transferRepository.countBySenderDocumentAndCreatedAtAfter(documentNumber, last24Hours);
        
        if (recentTransfers >= 5) {
            createAmlAlert(documentNumber, "HIGH_FREQUENCY", 
                "Fréquence élevée de transferts: " + recentTransfers + " transferts en 24h",
                RiskLevel.MEDIUM, amount);
        }
    }

    private void checkCumulativeAmounts(String documentNumber, BigDecimal currentAmount) {
        LocalDateTime last30Days = LocalDateTime.now().minusDays(30);
        BigDecimal cumulativeAmount = transferRepository.sumAmountBySenderDocumentAndCreatedAtAfter(
            documentNumber, last30Days);
        
        if (cumulativeAmount != null) {
            BigDecimal totalWithCurrent = cumulativeAmount.add(currentAmount);
            
            if (totalWithCurrent.compareTo(new BigDecimal("100000")) >= 0) {
                createAmlAlert(documentNumber, "HIGH_CUMULATIVE_AMOUNT", 
                    "Montant cumulé élevé sur 30 jours: " + totalWithCurrent,
                    RiskLevel.HIGH, totalWithCurrent);
            }
        }
    }

    private void checkExistingAlerts(String documentNumber) {
        List<AmlAlert> unresolvedAlerts = amlAlertRepository.findByDocumentNumber(documentNumber)
            .stream()
            .filter(alert -> !alert.isResolved())
            .toList();
        
        boolean hasCriticalAlert = unresolvedAlerts.stream()
            .anyMatch(alert -> alert.getRiskLevel() == RiskLevel.CRITICAL);
        
        if (hasCriticalAlert) {
            throw new KycAmlValidationException(
                "Transfert bloqué: alertes AML critiques non résolues pour ce client."
            );
        }
        
        long highRiskAlerts = unresolvedAlerts.stream()
            .filter(alert -> alert.getRiskLevel() == RiskLevel.HIGH)
            .count();
        
        if (highRiskAlerts >= 3) {
            throw new KycAmlValidationException(
                "Transfert bloqué: trop d'alertes AML à haut risque non résolues."
            );
        }
    }

    @Transactional
    public void createAmlAlert(String documentNumber, String alertType, String description, 
                             RiskLevel riskLevel, BigDecimal amountInvolved) {
        
        AmlAlert alert = new AmlAlert();
        alert.setDocumentNumber(documentNumber);
        alert.setAlertType(alertType);
        alert.setDescription(description);
        alert.setRiskLevel(riskLevel);
        alert.setAmountInvolved(amountInvolved);
        
        amlAlertRepository.save(alert);
    }

    @Transactional
    public KycProfile createKycProfile(String documentNumber, String documentType, String fullName,
                                     String nationality, String occupation, BigDecimal monthlyIncome) {
        
        KycProfile profile = new KycProfile();
        profile.setDocumentNumber(documentNumber);
        profile.setDocumentType(documentType);
        profile.setFullName(fullName);
        profile.setNationality(nationality);
        profile.setOccupation(occupation);
        profile.setMonthlyIncome(monthlyIncome);
        
        // Évaluation automatique du risque
        RiskLevel riskLevel = assessRiskLevel(monthlyIncome, nationality);
        profile.setRiskLevel(riskLevel);
        
        return kycProfileRepository.save(profile);
    }

    private RiskLevel assessRiskLevel(BigDecimal monthlyIncome, String nationality) {
        if (HIGH_RISK_COUNTRIES.contains(nationality)) {
            return RiskLevel.HIGH;
        }
        
        if (monthlyIncome != null && monthlyIncome.compareTo(new BigDecimal("50000")) >= 0) {
            return RiskLevel.MEDIUM;
        }
        
        return RiskLevel.LOW;
    }

    @Transactional
    public void approveKyc(String documentNumber) {
        KycProfile profile = kycProfileRepository.findByDocumentNumber(documentNumber)
            .orElseThrow(() -> new KycAmlValidationException("Profil KYC non trouvé"));
        
        profile.setKycStatus(KycStatus.APPROVED);
        profile.setVerificationDate(LocalDateTime.now());
        profile.setLastReviewDate(LocalDateTime.now());
        
        kycProfileRepository.save(profile);
    }

    @Transactional
    public void resolveAmlAlert(Long alertId) {
        AmlAlert alert = amlAlertRepository.findById(alertId)
            .orElseThrow(() -> new KycAmlValidationException("Alerte AML non trouvée"));
        
        alert.setResolved(true);
        alert.setResolvedAt(LocalDateTime.now());
        
        amlAlertRepository.save(alert);
    }
}