package com.okanetransfer.repository;

import com.okanetransfer.entity.KycProfile;
import com.okanetransfer.enums.KycStatus;
import com.okanetransfer.enums.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface KycProfileRepository extends JpaRepository<KycProfile, Long> {

    Optional<KycProfile> findByDocumentNumber(String documentNumber);

    List<KycProfile> findByKycStatus(KycStatus status);

    List<KycProfile> findByRiskLevel(RiskLevel riskLevel);

    @Query("SELECT k FROM KycProfile k WHERE k.lastReviewDate < :date OR k.lastReviewDate IS NULL")
    List<KycProfile> findProfilesRequiringReview(@Param("date") LocalDateTime date);

    @Query("SELECT k FROM KycProfile k WHERE k.fullName LIKE %:name% OR k.documentNumber LIKE %:docNumber%")
    List<KycProfile> searchByNameOrDocument(@Param("name") String name, @Param("docNumber") String docNumber);
}