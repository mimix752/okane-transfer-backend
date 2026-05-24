package com.okanetransfer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CurrencyRateRepository
        extends JpaRepository<CurrencyRate, Long> {

    List<CurrencyRate> findByPairOrderByAppliedAtDesc(String pair);

    @Query("""
        SELECT r FROM CurrencyRate r
        WHERE r.pair      = :pair
          AND r.appliedAt <= :date
        ORDER BY r.appliedAt DESC
    """)
    List<CurrencyRate> findLatestByPairAndDate(
            @Param("pair") String pair,
            @Param("date") LocalDateTime date
    );

    List<CurrencyRate> findByPairAndAppliedAtBetween(
            String pair,
            LocalDateTime from,
            LocalDateTime to
    );

    @Query("""
        SELECT r FROM CurrencyRate r
        WHERE r.appliedAt = (
            SELECT MAX(r2.appliedAt)
            FROM CurrencyRate r2
            WHERE r2.pair = r.pair
        )
    """)
    List<CurrencyRate> findLatestForAllPairs();

    boolean existsByPairAndAppliedAt(
            String pair,
            LocalDateTime appliedAt
    );
}