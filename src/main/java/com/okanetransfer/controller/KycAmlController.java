package com.okanetransfer.controller;

import com.okanetransfer.dto.request.KycProfileRequestDTO;
import com.okanetransfer.entity.AmlAlert;
import com.okanetransfer.entity.KycProfile;
import com.okanetransfer.enums.KycStatus;
import com.okanetransfer.enums.RiskLevel;
import com.okanetransfer.repository.AmlAlertRepository;
import com.okanetransfer.repository.KycProfileRepository;
import com.okanetransfer.service.KycAmlValidationService;
import com.okanetransfer.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kyc-aml")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "KYC/AML Management", description = "KYC and AML validation endpoints")
public class KycAmlController {

    @Autowired
    private KycAmlValidationService kycAmlValidationService;

    @Autowired
    private KycProfileRepository kycProfileRepository;

    @Autowired
    private AmlAlertRepository amlAlertRepository;

    @PostMapping("/kyc-profiles")
    @Operation(summary = "Create KYC profile")
    public ResponseEntity<ApiResponse<KycProfile>> createKycProfile(@Valid @RequestBody KycProfileRequestDTO dto) {
        KycProfile profile = kycAmlValidationService.createKycProfile(
            dto.getDocumentNumber(),
            dto.getDocumentType(),
            dto.getFullName(),
            dto.getNationality(),
            dto.getOccupation(),
            dto.getMonthlyIncome()
        );
        return ResponseEntity.ok(ApiResponse.success("Profil KYC créé avec succès", profile));
    }

    @GetMapping("/kyc-profiles")
    @Operation(summary = "Get all KYC profiles")
    public ResponseEntity<ApiResponse<List<KycProfile>>> getAllKycProfiles(
            @Parameter(description = "Filter by KYC status") @RequestParam(required = false) KycStatus status,
            @Parameter(description = "Filter by risk level") @RequestParam(required = false) RiskLevel riskLevel) {
        
        List<KycProfile> profiles;
        if (status != null) {
            profiles = kycProfileRepository.findByKycStatus(status);
        } else if (riskLevel != null) {
            profiles = kycProfileRepository.findByRiskLevel(riskLevel);
        } else {
            profiles = kycProfileRepository.findAll();
        }
        
        return ResponseEntity.ok(ApiResponse.success("Profils KYC récupérés avec succès", profiles));
    }

    @GetMapping("/kyc-profiles/{documentNumber}")
    @Operation(summary = "Get KYC profile by document number")
    public ResponseEntity<ApiResponse<KycProfile>> getKycProfile(@PathVariable String documentNumber) {
        KycProfile profile = kycProfileRepository.findByDocumentNumber(documentNumber)
            .orElseThrow(() -> new RuntimeException("Profil KYC non trouvé"));
        return ResponseEntity.ok(ApiResponse.success("Profil KYC récupéré avec succès", profile));
    }

    @PostMapping("/kyc-profiles/{documentNumber}/approve")
    @Operation(summary = "Approve KYC profile")
    public ResponseEntity<ApiResponse<Void>> approveKyc(@PathVariable String documentNumber) {
        kycAmlValidationService.approveKyc(documentNumber);
        return ResponseEntity.ok(ApiResponse.success("Profil KYC approuvé avec succès", null));
    }

    @GetMapping("/aml-alerts")
    @Operation(summary = "Get AML alerts")
    public ResponseEntity<ApiResponse<List<AmlAlert>>> getAmlAlerts(
            @Parameter(description = "Show only unresolved alerts") @RequestParam(defaultValue = "false") boolean unresolvedOnly,
            @Parameter(description = "Filter by risk level") @RequestParam(required = false) RiskLevel riskLevel) {
        
        List<AmlAlert> alerts;
        if (unresolvedOnly) {
            alerts = amlAlertRepository.findByResolvedFalse();
        } else if (riskLevel != null) {
            alerts = amlAlertRepository.findByRiskLevel(riskLevel);
        } else {
            alerts = amlAlertRepository.findAll();
        }
        
        return ResponseEntity.ok(ApiResponse.success("Alertes AML récupérées avec succès", alerts));
    }

    @PostMapping("/aml-alerts/{alertId}/resolve")
    @Operation(summary = "Resolve AML alert")
    public ResponseEntity<ApiResponse<Void>> resolveAmlAlert(@PathVariable Long alertId) {
        kycAmlValidationService.resolveAmlAlert(alertId);
        return ResponseEntity.ok(ApiResponse.success("Alerte AML résolue avec succès", null));
    }

    @GetMapping("/aml-alerts/high-risk")
    @Operation(summary = "Get high-risk unresolved alerts")
    public ResponseEntity<ApiResponse<List<AmlAlert>>> getHighRiskAlerts() {
        List<AmlAlert> alerts = amlAlertRepository.findHighRiskUnresolvedAlerts();
        return ResponseEntity.ok(ApiResponse.success("Alertes à haut risque récupérées avec succès", alerts));
    }
}