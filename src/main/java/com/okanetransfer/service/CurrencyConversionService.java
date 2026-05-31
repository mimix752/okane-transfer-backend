package com.okanetransfer.service;

import com.okanetransfer.entity.Currency;
import com.okanetransfer.entity.CurrencyRate;
import com.okanetransfer.repository.CurrencyRateRepository;
import com.okanetransfer.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CurrencyConversionService {

    @Autowired private CurrencyRateRepository currencyRateRepository;
    @Autowired private CurrencyRepository currencyRepository;

    public Currency findByCode(String code) {
        return currencyRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Currency not found: " + code));
    }

    @Transactional(readOnly = true)
    public BigDecimal convertAmount(BigDecimal amount, String fromCode, String toCode) {
        if (fromCode.equals(toCode)) return amount;
        BigDecimal rate = getExchangeRate(fromCode, toCode);
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public BigDecimal getExchangeRate(String fromCode, String toCode) {
        Optional<CurrencyRate> rateOpt = currencyRateRepository
                .findByFromCurrencyAndToCurrencyAndActiveTrueOrderByAppliedAtDesc(fromCode, toCode);
        if (rateOpt.isPresent()) return rateOpt.get().getRate();

        Optional<CurrencyRate> inverseOpt = currencyRateRepository
                .findByFromCurrencyAndToCurrencyAndActiveTrueOrderByAppliedAtDesc(toCode, fromCode);
        if (inverseOpt.isPresent())
            return BigDecimal.ONE.divide(inverseOpt.get().getRate(), 6, RoundingMode.HALF_UP);

        throw new RuntimeException("Taux de change non disponible pour " + fromCode + " -> " + toCode);
    }

    @Transactional
    public CurrencyRate updateExchangeRate(Currency fromCurrency, Currency toCurrency, BigDecimal rate) {
        currencyRateRepository
                .findByFromCurrencyAndToCurrencyAndActiveTrueOrderByAppliedAtDesc(
                        fromCurrency.getCode(), toCurrency.getCode())
                .ifPresent(old -> { old.setActive(false); currencyRateRepository.save(old); });

        CurrencyRate newRate = new CurrencyRate();
        newRate.setFromCurrency(fromCurrency.getCode());
        newRate.setToCurrency(toCurrency.getCode());
        newRate.setPair(fromCurrency.getCode() + "_" + toCurrency.getCode());
        newRate.setRate(rate);
        newRate.setActive(true);
        newRate.setAppliedAt(LocalDateTime.now());
        return currencyRateRepository.save(newRate);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateConversionWithFees(BigDecimal amount, String fromCode, String toCode,
                                                  BigDecimal conversionFeePercent) {
        BigDecimal converted = convertAmount(amount, fromCode, toCode);
        BigDecimal fee = converted.multiply(conversionFeePercent.divide(BigDecimal.valueOf(100)));
        return converted.subtract(fee);
    }
}
