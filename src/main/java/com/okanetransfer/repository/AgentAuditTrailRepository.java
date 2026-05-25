package com.okanetransfer.repository;

import com.okanetransfer.entity.AgentAuditTrail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgentAuditTrailRepository extends JpaRepository<AgentAuditTrail, Long> {

    @Query("SELECT a FROM AgentAuditTrail a WHERE a.agentUsername = :username ORDER BY a.createdAt DESC")
    List<AgentAuditTrail> findByAgentUsernameOrderByCreatedAtDesc(@Param("username") String agentUsername);

    List<AgentAuditTrail> findByActionTypeOrderByCreatedAtDesc(String actionType);

    @Query("SELECT a FROM AgentAuditTrail a WHERE a.createdAt BETWEEN :from AND :to ORDER BY a.createdAt DESC")
    List<AgentAuditTrail> findByDateRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT a FROM AgentAuditTrail a WHERE a.agentUsername = :username AND a.createdAt BETWEEN :from AND :to ORDER BY a.createdAt DESC")
    List<AgentAuditTrail> findByAgentAndDateRange(@Param("username") String agentUsername, 
                                                 @Param("from") LocalDateTime from, 
                                                 @Param("to") LocalDateTime to);

    @Query("SELECT a FROM AgentAuditTrail a WHERE a.entityType = :entityType AND a.entityId = :entityId ORDER BY a.createdAt DESC")
    List<AgentAuditTrail> findByEntityTypeAndId(@Param("entityType") String entityType, @Param("entityId") Long entityId);

    @Query("SELECT COUNT(a) FROM AgentAuditTrail a WHERE a.agentUsername = :username AND a.createdAt >= :fromDate")
    long countAgentActionsSince(@Param("username") String agentUsername, @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT a FROM AgentAuditTrail a ORDER BY a.createdAt DESC")
    List<AgentAuditTrail> findAll();
}