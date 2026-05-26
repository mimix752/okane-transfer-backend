package com.okanetransfer.repository;

import com.okanetransfer.entity.Transfer;
import com.okanetransfer.enums.TransferStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class TransferRepository {

    @PersistenceContext
    private EntityManager em;

    public Transfer save(Transfer t) {
        em.persist(t);
        return t;
    }

    public Optional<Transfer> findById(Long id) {
        return Optional.ofNullable(em.find(Transfer.class, id));
    }

    public Optional<Transfer> findByRecipientPhone(String phone) {
        return em.createQuery(
                        "SELECT t FROM Transfer t WHERE t.recipientPhone = :phone AND t.status = 'PENDING' ORDER BY t.createdAt DESC",
                        Transfer.class)
                .setParameter("phone", phone)
                .getResultStream()
                .findFirst();
    }

    public Optional<Transfer> findByCode(String code) {
        return em.createQuery(
                        "SELECT t FROM Transfer t WHERE t.transferCode = :code",
                        Transfer.class)
                .setParameter("code", code)
                .getResultStream()
                .findFirst();
    }

    public List<Transfer> findBySenderId(Long senderId) {
        return em.createQuery(
                        "SELECT t FROM Transfer t WHERE t.sender.id = :id",
                        Transfer.class)
                .setParameter("id", senderId)
                .getResultList();
    }

    public List<Transfer> findByStatus(TransferStatus status) {
        return em.createQuery(
                        "SELECT t FROM Transfer t WHERE t.status = :s",
                        Transfer.class)
                .setParameter("s", status)
                .getResultList();
    }

    public List<Transfer> findAll() {
        return em.createQuery(
                        "SELECT t FROM Transfer t",
                        Transfer.class)
                .getResultList();
    }

    // ── Méthode ajoutée pour ReportService ───────────────────

    @Transactional(readOnly = true)
    public List<Transfer> findByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end) {

        return em.createQuery(
                        """
                        SELECT t FROM Transfer t
                        WHERE t.createdAt >= :start
                          AND t.createdAt <= :end
                        ORDER BY t.createdAt DESC
                        """,
                        Transfer.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
    }

    // ── Méthodes pour KYC/AML ───────────────────

    @Transactional(readOnly = true)
    public long countBySenderDocumentAndCreatedAtAfter(String senderDocument, LocalDateTime fromDate) {
        return em.createQuery(
                        "SELECT COUNT(t) FROM Transfer t WHERE t.senderCIN = :doc AND t.createdAt >= :date",
                        Long.class)
                .setParameter("doc", senderDocument)
                .setParameter("date", fromDate)
                .getSingleResult();
    }

    @Transactional(readOnly = true)
    public BigDecimal sumAmountBySenderDocumentAndCreatedAtAfter(String senderDocument, LocalDateTime fromDate) {
        return em.createQuery(
                        "SELECT COALESCE(SUM(t.amount), 0) FROM Transfer t WHERE t.senderCIN = :doc AND t.createdAt >= :date",
                        BigDecimal.class)
                .setParameter("doc", senderDocument)
                .setParameter("date", fromDate)
                .getSingleResult();
    }
}