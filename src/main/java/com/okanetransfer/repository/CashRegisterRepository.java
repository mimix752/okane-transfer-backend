package com.okanetransfer.repository;

import com.okanetransfer.entity.CashRegister;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class CashRegisterRepository {

    @PersistenceContext
    private EntityManager em;

    public CashRegister save(CashRegister c) { em.persist(c); return c; }

    public Optional<CashRegister> findById(Long id) {
        return Optional.ofNullable(em.find(CashRegister.class, id));
    }

    public List<CashRegister> findByAgencyId(Long agencyId) {
        return em.createQuery("SELECT c FROM CashRegister c WHERE c.agency.id = :id", CashRegister.class)
                .setParameter("id", agencyId).getResultList();
    }

    public Optional<CashRegister> findOpenByAgentId(Long agentId) {
        return em.createQuery(
                "SELECT c FROM CashRegister c WHERE c.agent.id = :id AND c.open = true", CashRegister.class)
                .setParameter("id", agentId).getResultStream().findFirst();
    }
}
