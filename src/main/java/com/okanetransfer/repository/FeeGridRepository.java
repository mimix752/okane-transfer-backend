package com.okanetransfer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface FeeGridRepository
        extends JpaRepository<FeeGrid, Long> {

    List<FeeGrid> findByCorridor_Id(Long corridorId);

    List<FeeGrid> findByCorridor_IdAndActive(
            Long corridorId,
            boolean active
    );

    List<FeeGrid> findByActive(boolean active);
    @Query("""
        SELECT f FROM FeeGrid f
        WHERE f.corridor.id  = :corridorId
          AND f.active        = true
          AND f.minAmount    <= :amount
          AND f.maxAmount    >= :amount
        ORDER BY f.minAmount ASC
    """)
    Optional<FeeGrid> findApplicable(
            @Param("corridorId") Long corridorId,
            @Param("amount")     BigDecimal amount
    );

    @Query("""
        SELECT COUNT(f) > 0 FROM FeeGrid f
        WHERE f.corridor.id = :corridorId
          AND f.active       = true
          AND f.id          != :excludeId
          AND f.minAmount   <  :maxAmount
          AND f.maxAmount   >  :minAmount
    """)
    boolean existsOverlap(
            @Param("corridorId") Long corridorId,
            @Param("minAmount")  BigDecimal minAmount,
            @Param("maxAmount")  BigDecimal maxAmount,
            @Param("excludeId")  Long excludeId
    );
}