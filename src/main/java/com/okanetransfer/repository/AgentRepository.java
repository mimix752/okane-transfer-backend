package com.okanetransfer.repository;

import com.okanetransfer.entity.Agent;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class AgentRepository {

    @PersistenceContext
    private EntityManager em;

    public Agent save(Agent a) { em.persist(a); return a; }

    public Optional<Agent> findById(Long id) {
        return Optional.ofNullable(em.find(Agent.class, id));
    }

    public List<Agent> findByAgencyId(Long agencyId) {
        return em.createQuery("SELECT a FROM Agent a WHERE a.agency.id = :id", Agent.class)
                .setParameter("id", agencyId).getResultList();
    }
}
