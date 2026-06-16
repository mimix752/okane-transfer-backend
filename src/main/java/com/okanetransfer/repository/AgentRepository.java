package com.okanetransfer.repository;

import com.okanetransfer.entity.Agency;
import com.okanetransfer.entity.Agent;
import com.okanetransfer.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {

    List<Agent> findByAgency(Agency agency);


    List<Agent> findByActive(boolean active);


    List<Agent> findByAgencyAndRole(Agency agency, Role role);


    List<Agent> findByAgency_Id(Long agencyId);


    @Query("""
    SELECT a
    FROM Agent a
    JOIN FETCH a.agency
    WHERE a.id = :userId
    """)
    Optional<Agent> findByUserId(@Param("userId") Long userId);
}