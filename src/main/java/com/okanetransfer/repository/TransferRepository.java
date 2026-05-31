package com.okanetransfer.repository;

import com.okanetransfer.entity.Transfer;
import com.okanetransfer.enums.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {

    // ── Méthodes de base ─────────────────────────────────────

    Optional<Transfer> findByTransferCode(String code);

    List<Transfer> findBySenderId(Long senderId);

    List<Transfer> findByStatus(TransferStatus status);

    // ── Par téléphone bénéficiaire ───────────────────────────

    @Query("SELECT t FROM Transfer t " +
            "WHERE t.recipientPhone = :phone " +
            "AND t.status = 'PENDING' " +
            "ORDER BY t.createdAt DESC")
    Optional<Transfer> findByRecipientPhone(
            @Param("phone") String phone);

    // ── Par code (alias) ─────────────────────────────────────

    @Query("SELECT t FROM Transfer t " +
            "WHERE t.transferCode = :code")
    Optional<Transfer> findByCode(@Param("code") String code);

    // ── Par période ──────────────────────────────────────────

    @Query("SELECT t FROM Transfer t " +
            "WHERE t.createdAt >= :start " +
            "AND t.createdAt <= :end " +
            "ORDER BY t.createdAt DESC")
    List<Transfer> findByCreatedAtBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // ── Par agence et période ────────────────────────────────

    @Query("SELECT t FROM Transfer t " +
            "WHERE t.agency.id = :agencyId " +
            "AND t.createdAt >= :start " +
            "AND t.createdAt <= :end " +
            "ORDER BY t.createdAt DESC")
    List<Transfer> findByAgencyIdAndCreatedAtBetween(
            @Param("agencyId") Long agencyId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // ── Par corridor et période ──────────────────────────────

    @Query("SELECT t FROM Transfer t " +
            "WHERE t.senderCountry = :senderCountry " +
            "AND t.recipientCountry = :recipientCountry " +
            "AND t.createdAt >= :start " +
            "AND t.createdAt <= :end " +
            "ORDER BY t.createdAt DESC")
    List<Transfer> findByCorridorAndCreatedAtBetween(
            @Param("senderCountry") String senderCountry,
            @Param("recipientCountry") String recipientCountry,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // ── Somme montants par période ───────────────────────────

    @Query("SELECT COALESCE(SUM(t.amount), 0) " +
            "FROM Transfer t " +
            "WHERE t.createdAt >= :start " +
            "AND t.createdAt <= :end")
    BigDecimal sumAmountByCreatedAtBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // ── Somme fees par période ───────────────────────────────

    @Query("SELECT COALESCE(SUM(t.fees), 0) " +
            "FROM Transfer t " +
            "WHERE t.createdAt >= :start " +
            "AND t.createdAt <= :end")
    BigDecimal sumFeesByCreatedAtBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // ── KYC/AML ──────────────────────────────────────────────

    @Query("SELECT COUNT(t) FROM Transfer t " +
            "WHERE t.senderCIN = :doc " +
            "AND t.createdAt >= :date")
    long countBySenderDocumentAndCreatedAtAfter(
            @Param("doc") String senderDocument,
            @Param("date") LocalDateTime fromDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) " +
            "FROM Transfer t " +
            "WHERE t.senderCIN = :doc " +
            "AND t.createdAt >= :date")
    BigDecimal sumAmountBySenderDocumentAndCreatedAtAfter(
            @Param("doc") String senderDocument,
            @Param("date") LocalDateTime fromDate);
}