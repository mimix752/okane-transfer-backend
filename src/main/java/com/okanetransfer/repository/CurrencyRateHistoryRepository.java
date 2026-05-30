package com.okanetransfer.repository;

import com.okanetransfer.entity.Currency;
import com.okanetransfer.entity.CurrencyRateHistory;
import com.okanetransfer.enums.RateSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CurrencyRateHistoryRepository
        extends JpaRepository<CurrencyRateHistory, Long> {

    List<CurrencyRateHistory> findByCurrencyOrderByChangedAtDesc(
            Currency currency);

    List<CurrencyRateHistory> findByChangedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    List<CurrencyRateHistory> findByCurrencyAndChangedAtBetween(
            Currency currency,
            LocalDateTime start,
            LocalDateTime end
    );

    List<CurrencyRateHistory> findBySourceOrderByChangedAtDesc(
            RateSource source);

    @Query("""
        SELECT h FROM CurrencyRateHistory h
        ORDER BY h.changedAt DESC
    """)
    List<CurrencyRateHistory> findRecentHistory(
            org.springframework.data.domain.Pageable pageable);

    @Query("""
        SELECT h.source, COUNT(h)
        FROM CurrencyRateHistory h
        WHERE h.changedAt >= :startOfDay
        GROUP BY h.source
    """)
    List<Object[]> countBySourceToday(
            @Param("startOfDay") LocalDateTime startOfDay);
}