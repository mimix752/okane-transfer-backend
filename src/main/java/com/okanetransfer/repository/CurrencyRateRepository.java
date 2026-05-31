package com.okanetransfer.repository;

import com.okanetransfer.entity.CurrencyRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, Long> {

    Optional<CurrencyRate> findByFromCurrencyAndToCurrencyAndActiveTrueOrderByAppliedAtDesc(
            String fromCurrency, String toCurrency);

    List<CurrencyRate> findByFromCurrencyAndToCurrencyOrderByAppliedAtDesc(
            String fromCurrency, String toCurrency);

    @Query("""
        SELECT r FROM CurrencyRate r
        WHERE r.fromCurrency = :from
          AND r.toCurrency = :to
          AND r.active = true
          AND r.appliedAt <= :date
        ORDER BY r.appliedAt DESC
    """)
    List<CurrencyRate> findLatestByPairAndDate(
            @Param("from") String fromCurrency,
            @Param("to") String toCurrency,
            @Param("date") LocalDateTime date
    );

    List<CurrencyRate> findByActiveTrue();

    @Query("SELECT DISTINCT r.fromCurrency FROM CurrencyRate r WHERE r.active = true")
    List<String> findAllActiveCurrencies();
}