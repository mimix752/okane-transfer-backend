package com.okanetransfer.repository;

import com.okanetransfer.entity.Corridor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CorridorRepository
        extends JpaRepository<Corridor, Long> {

    List<Corridor> findBySourceCountry(String sourceCountry);

    List<Corridor> findByDestinationCountry(String destinationCountry);

    List<Corridor> findByActive(boolean active);

    Page<Corridor> findByActive(boolean active, Pageable pageable);

    Optional<Corridor> findBySourceCountryAndDestinationCountry(
            String sourceCountry,
            String destinationCountry
    );

    boolean existsBySourceCountryAndDestinationCountry(
            String sourceCountry,
            String destinationCountry
    );

    boolean existsBySourceCountryAndDestinationCountryAndIdNot(
            String sourceCountry,
            String destinationCountry,
            Long id
    );

    @Query("""
        SELECT c FROM Corridor c
        WHERE c.sourceCurrency.id = :currencyId
           OR c.destinationCurrency.id = :currencyId
    """)
    List<Corridor> findByCurrencyId(@Param("currencyId") Long currencyId);

    @Query("SELECT c FROM Corridor c JOIN FETCH c.sourceCurrency JOIN FETCH c.destinationCurrency")
    List<Corridor> findAllWithCurrencies();

    Optional<Corridor> findBySourceCountryAndDestinationCountryAndActiveTrue(
            String sourceCountry, String destinationCountry);



}