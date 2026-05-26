package com.okanetransfer.repository;

import com.okanetransfer.entity.CashOperation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@Transactional
public class CashOperationRepository {

    @PersistenceContext
    private EntityManager em;

    public CashOperation save(CashOperation op) {
        em.persist(op);
        return op;
    }

    public List<CashOperation> findByCashRegisterAndDay(Long cashRegisterId, LocalDateTime start, LocalDateTime end) {
        return em.createQuery(
                "SELECT o FROM CashOperation o WHERE o.cashRegister.id = :id AND o.createdAt >= :start AND o.createdAt <= :end ORDER BY o.createdAt ASC",
                CashOperation.class)
                .setParameter("id", cashRegisterId)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
    }
}
