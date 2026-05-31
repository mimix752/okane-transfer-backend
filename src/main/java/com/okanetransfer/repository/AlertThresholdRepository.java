package com.okanetransfer.repository;

import com.okanetransfer.entity.AlertThreshold;
import com.okanetransfer.enums.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface AlertThresholdRepository
        extends JpaRepository<AlertThreshold, Long> {

    // Trouver le seuil d'un type d'alerte
    Optional<AlertThreshold> findByAlertType(AlertType alertType);

    // Tous les seuils actifs
    List<AlertThreshold> findByEnabled(boolean enabled);

    // Vérifier existence
    boolean existsByAlertType(AlertType alertType);
}