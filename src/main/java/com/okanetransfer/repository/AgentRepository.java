package com.okanetransfer.repository;

import com.okanetransfer.entity.Agency;
import com.okanetransfer.entity.Agent;
import com.okanetransfer.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {

    List<Agent> findByAgency(Agency agency);

    List<Agent> findByActive(boolean active);

    List<Agent> findByAgencyAndRole(Agency agency, Role role);

    List<Agent> findByAgency_Id(Long agencyId);
}