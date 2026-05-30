package com.okanetransfer.repository;

import com.okanetransfer.entity.Alert;
import com.okanetransfer.enums.AlertLevel;
import com.okanetransfer.enums.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository
        extends JpaRepository<Alert, Long> {

    // Toutes les alertes non lues (pour badge notification)
    List<Alert> findByIsReadFalseOrderByCreatedAtDesc();

    // Alertes par niveau
    List<Alert> findByLevelOrderByCreatedAtDesc(AlertLevel level);

    // Alertes par type
    List<Alert> findByTypeOrderByCreatedAtDesc(AlertType type);

    // Alertes d'une entité précise  ex: agenceId=5
    List<Alert> findByEntityIdAndEntityTypeOrderByCreatedAtDesc(
            Long entityId, String entityType);

    // Alertes entre deux dates
    List<Alert> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime start, LocalDateTime end);

    // Compter les alertes non lues par niveau
    @Query("""
        SELECT a.level, COUNT(a)
        FROM Alert a
        WHERE a.isRead = false
        GROUP BY a.level
    """)
    List<Object[]> countUnreadByLevel();

    // Vérifier si une alerte similaire existe déjà récemment
    // (éviter les doublons dans les 30 dernières minutes)
    @Query("""
        SELECT COUNT(a) > 0 FROM Alert a
        WHERE a.type       = :type
          AND a.entityId   = :entityId
          AND a.entityType = :entityType
          AND a.createdAt >= :since
    """)
    boolean existsRecentAlert(
            @Param("type")       AlertType type,
            @Param("entityId")   Long entityId,
            @Param("entityType") String entityType,
            @Param("since")      LocalDateTime since
    );
}