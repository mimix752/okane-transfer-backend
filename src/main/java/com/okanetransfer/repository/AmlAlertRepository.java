package com.okanetransfer.repository;

import com.okanetransfer.entity.AmlAlert;
import com.okanetransfer.enums.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AmlAlertRepository extends JpaRepository<AmlAlert, Long> {

    List<AmlAlert> findByDocumentNumber(String documentNumber);

    List<AmlAlert> findByResolvedFalse();

    List<AmlAlert> findByRiskLevel(RiskLevel riskLevel);

    @Query("SELECT COUNT(a) FROM AmlAlert a WHERE a.documentNumber = :docNumber AND a.createdAt >= :fromDate")
    long countRecentAlertsByDocument(@Param("docNumber") String documentNumber, @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT a FROM AmlAlert a WHERE a.resolved = false AND a.riskLevel IN ('HIGH', 'CRITICAL')")
    List<AmlAlert> findHighRiskUnresolvedAlerts();
}