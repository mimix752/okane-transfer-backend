package com.okanetransfer.service;

import com.okanetransfer.entity.Currency;
import com.okanetransfer.entity.CurrencyRate;
import com.okanetransfer.repository.CurrencyRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CurrencyConversionService {

    @Autowired
    private CurrencyRateRepository currencyRateRepository;

    @Transactional(readOnly = true)
    public BigDecimal convertAmount(BigDecimal amount, Currency fromCurrency, Currency toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }

        BigDecimal rate = getExchangeRate(fromCurrency, toCurrency);
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public BigDecimal getExchangeRate(Currency fromCurrency, Currency toCurrency) {
        Optional<CurrencyRate> rateOpt = currencyRateRepository
            .findByFromCurrencyAndToCurrencyAndActiveTrueOrderByCreatedAtDesc(fromCurrency, toCurrency);
        
        if (rateOpt.isPresent()) {
            return rateOpt.get().getRate();
        }

        // Essayer la conversion inverse
        Optional<CurrencyRate> inverseRateOpt = currencyRateRepository
            .findByFromCurrencyAndToCurrencyAndActiveTrueOrderByCreatedAtDesc(toCurrency, fromCurrency);
        
        if (inverseRateOpt.isPresent()) {
            return BigDecimal.ONE.divide(inverseRateOpt.get().getRate(), 6, RoundingMode.HALF_UP);
        }

        throw new RuntimeException("Taux de change non disponible pour " + fromCurrency + " -> " + toCurrency);
    }

    @Transactional
    public CurrencyRate updateExchangeRate(Currency fromCurrency, Currency toCurrency, BigDecimal rate) {
        // Désactiver l'ancien taux
        currencyRateRepository.findByFromCurrencyAndToCurrencyAndActiveTrueOrderByCreatedAtDesc(fromCurrency, toCurrency)
            .ifPresent(oldRate -> {
                oldRate.setActive(false);
                currencyRateRepository.save(oldRate);
            });

        // Créer le nouveau taux
        CurrencyRate newRate = new CurrencyRate();
        newRate.setFromCurrency(fromCurrency);
        newRate.setToCurrency(toCurrency);
        newRate.setRate(rate);
        newRate.setActive(true);
        newRate.setCreatedAt(LocalDateTime.now());

        return currencyRateRepository.save(newRate);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateConversionWithFees(BigDecimal amount, Currency fromCurrency, Currency toCurrency,
                                                BigDecimal conversionFeePercent) {
        BigDecimal convertedAmount = convertAmount(amount, fromCurrency, toCurrency);
        BigDecimal conversionFee = convertedAmount.multiply(conversionFeePercent.divide(BigDecimal.valueOf(100)));
        return convertedAmount.subtract(conversionFee);
    }
}