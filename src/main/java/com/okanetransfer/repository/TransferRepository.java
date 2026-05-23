package com.okanetransfer.repository;

import com.okanetransfer.entity.Transfer;
import com.okanetransfer.enums.TransferStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
}