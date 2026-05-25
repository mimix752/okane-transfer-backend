package com.okanetransfer.repository;

import com.okanetransfer.entity.JournalAudit;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class JournalAuditRepository {

    private final EntityManager em;

    public JournalAuditRepository(EntityManager em) {
        this.em = em;
    }

    public JournalAudit save(JournalAudit audit) {
        if (audit.getId() == null) {
            em.persist(audit);
            return audit;
        }
        return em.merge(audit);
    }

    public Optional<JournalAudit> findById(Long id) {
        return Optional.ofNullable(em.find(JournalAudit.class, id));
    }

    /**
     * Requête dynamique avec tous les filtres optionnels.
     */
    public List<JournalAudit> findWithFilters(String performedBy,
                                              String action,
                                              String entityType,
                                              Long entityId,
                                              LocalDateTime from,
                                              LocalDateTime to,
                                              int offset,
                                              int limit) {

        StringBuilder jpql = new StringBuilder(
                "SELECT j FROM JournalAudit j WHERE 1=1");

        if (performedBy != null && !performedBy.isBlank())
            jpql.append(" AND LOWER(j.performedBy) LIKE :performedBy");
        if (action != null && !action.isBlank())
            jpql.append(" AND LOWER(j.action) LIKE :action");
        if (entityType != null && !entityType.isBlank())
            jpql.append(" AND LOWER(j.entityType) = :entityType");
        if (entityId != null)
            jpql.append(" AND j.entityId = :entityId");
        if (from != null)
            jpql.append(" AND j.performedAt >= :from");
        if (to != null)
            jpql.append(" AND j.performedAt <= :to");

        jpql.append(" ORDER BY j.performedAt DESC");

        TypedQuery<JournalAudit> query =
                em.createQuery(jpql.toString(), JournalAudit.class);

        if (performedBy != null && !performedBy.isBlank())
            query.setParameter("performedBy",
                    "%" + performedBy.toLowerCase() + "%");
        if (action != null && !action.isBlank())
            query.setParameter("action",
                    "%" + action.toLowerCase() + "%");
        if (entityType != null && !entityType.isBlank())
            query.setParameter("entityType", entityType.toLowerCase());
        if (entityId != null)
            query.setParameter("entityId", entityId);
        if (from != null)
            query.setParameter("from", from);
        if (to != null)
            query.setParameter("to", to);

        return query
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public long countWithFilters(String performedBy,
                                 String action,
                                 String entityType,
                                 Long entityId,
                                 LocalDateTime from,
                                 LocalDateTime to) {

        StringBuilder jpql = new StringBuilder(
                "SELECT COUNT(j) FROM JournalAudit j WHERE 1=1");

        if (performedBy != null && !performedBy.isBlank())
            jpql.append(" AND LOWER(j.performedBy) LIKE :performedBy");
        if (action != null && !action.isBlank())
            jpql.append(" AND LOWER(j.action) LIKE :action");
        if (entityType != null && !entityType.isBlank())
            jpql.append(" AND LOWER(j.entityType) = :entityType");
        if (entityId != null)
            jpql.append(" AND j.entityId = :entityId");
        if (from != null)
            jpql.append(" AND j.performedAt >= :from");
        if (to != null)
            jpql.append(" AND j.performedAt <= :to");

        TypedQuery<Long> query =
                em.createQuery(jpql.toString(), Long.class);

        if (performedBy != null && !performedBy.isBlank())
            query.setParameter("performedBy",
                    "%" + performedBy.toLowerCase() + "%");
        if (action != null && !action.isBlank())
            query.setParameter("action",
                    "%" + action.toLowerCase() + "%");
        if (entityType != null && !entityType.isBlank())
            query.setParameter("entityType", entityType.toLowerCase());
        if (entityId != null)
            query.setParameter("entityId", entityId);
        if (from != null)
            query.setParameter("from", from);
        if (to != null)
            query.setParameter("to", to);

        return query.getSingleResult();
    }

    /** Tous les logs d'une entité précise, toutes actions confondues. */
    public List<JournalAudit> findByEntityTypeAndEntityId(
            String entityType, Long entityId) {

        return em.createQuery(
                        "SELECT j FROM JournalAudit j " +
                                "WHERE LOWER(j.entityType) = :type " +
                                "AND j.entityId = :eid " +
                                "ORDER BY j.performedAt DESC",
                        JournalAudit.class)
                .setParameter("type", entityType.toLowerCase())
                .setParameter("eid", entityId)
                .getResultList();
    }
}