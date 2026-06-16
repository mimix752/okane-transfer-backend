package com.okanetransfer.config;

import com.okanetransfer.entity.*;
import com.okanetransfer.enums.Role;
import com.okanetransfer.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataSeeder implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired private CurrencyRepository currencyRepository;
    @Autowired private CorridorRepository corridorRepository;
    @Autowired private FeeGridRepository feeGridRepository;
    @Autowired private AgencyRepository agencyRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EntityManager entityManager;

    private boolean seeded = false;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (seeded || currencyRepository.count() > 0) return;
        seeded = true;

        // ── Currencies ──────────────────────────────────────────
        Currency mad = currencyRepository.save(Currency.builder()
                .code("MAD").name("Dirham Marocain").symbol("د.م.")
                .exchangeRate(new BigDecimal("1.000000")).active(true).build());

        Currency eur = currencyRepository.save(Currency.builder()
                .code("EUR").name("Euro").symbol("€")
                .exchangeRate(new BigDecimal("10.850000")).active(true).build());

        Currency usd = currencyRepository.save(Currency.builder()
                .code("USD").name("US Dollar").symbol("$")
                .exchangeRate(new BigDecimal("10.020000")).active(true).build());

        Currency xof = currencyRepository.save(Currency.builder()
                .code("XOF").name("Franc CFA").symbol("CFA")
                .exchangeRate(new BigDecimal("0.016500")).active(true).build());

        Currency gnf = currencyRepository.save(Currency.builder()
                .code("GNF").name("Franc Guinéen").symbol("FG")
                .exchangeRate(new BigDecimal("0.001100")).active(true).build());

        // ── Corridors ───────────────────────────────────────────
        Corridor marToSen = corridorRepository.save(Corridor.builder()
                .sourceCountry("MA").destinationCountry("SN")
                .sourceCurrency(mad).destinationCurrency(xof)
                .exchangeRate(new BigDecimal("60.500000")).active(true).build());

        Corridor marToGui = corridorRepository.save(Corridor.builder()
                .sourceCountry("MA").destinationCountry("GN")
                .sourceCurrency(mad).destinationCurrency(gnf)
                .exchangeRate(new BigDecimal("909.000000")).active(true).build());

        Corridor marToEur = corridorRepository.save(Corridor.builder()
                .sourceCountry("MA").destinationCountry("FR")
                .sourceCurrency(mad).destinationCurrency(eur)
                .exchangeRate(new BigDecimal("0.092000")).active(true).build());

        Corridor marToUsd = corridorRepository.save(Corridor.builder()
                .sourceCountry("MA").destinationCountry("US")
                .sourceCurrency(mad).destinationCurrency(usd)
                .exchangeRate(new BigDecimal("0.099800")).active(true).build());

        // ── Fee Grids ───────────────────────────────────────────
        // MA → SN
        feeGridRepository.save(FeeGrid.builder().corridor(marToSen)
                .minAmount(bd("0")).maxAmount(bd("500"))
                .fixedFee(bd("15")).percentageFee(bd("1.5"))
                .agencyShare(bd("60")).centralShare(bd("40")).active(true).build());

        feeGridRepository.save(FeeGrid.builder().corridor(marToSen)
                .minAmount(bd("500.01")).maxAmount(bd("2000"))
                .fixedFee(bd("25")).percentageFee(bd("1.2"))
                .agencyShare(bd("60")).centralShare(bd("40")).active(true).build());

        feeGridRepository.save(FeeGrid.builder().corridor(marToSen)
                .minAmount(bd("2000.01")).maxAmount(null)
                .fixedFee(bd("40")).percentageFee(bd("1.0"))
                .agencyShare(bd("60")).centralShare(bd("40")).active(true).build());

        // MA → GN
        feeGridRepository.save(FeeGrid.builder().corridor(marToGui)
                .minAmount(bd("0")).maxAmount(bd("500"))
                .fixedFee(bd("20")).percentageFee(bd("2.0"))
                .agencyShare(bd("55")).centralShare(bd("45")).active(true).build());

        feeGridRepository.save(FeeGrid.builder().corridor(marToGui)
                .minAmount(bd("500.01")).maxAmount(null)
                .fixedFee(bd("35")).percentageFee(bd("1.5"))
                .agencyShare(bd("55")).centralShare(bd("45")).active(true).build());

        // MA → FR
        feeGridRepository.save(FeeGrid.builder().corridor(marToEur)
                .minAmount(bd("0")).maxAmount(bd("1000"))
                .fixedFee(bd("10")).percentageFee(bd("0.8"))
                .agencyShare(bd("50")).centralShare(bd("50")).active(true).build());

        feeGridRepository.save(FeeGrid.builder().corridor(marToEur)
                .minAmount(bd("1000.01")).maxAmount(null)
                .fixedFee(bd("20")).percentageFee(bd("0.5"))
                .agencyShare(bd("50")).centralShare(bd("50")).active(true).build());

        // MA → US
        feeGridRepository.save(FeeGrid.builder().corridor(marToUsd)
                .minAmount(bd("0")).maxAmount(null)
                .fixedFee(bd("30")).percentageFee(bd("1.0"))
                .agencyShare(bd("50")).centralShare(bd("50")).active(true).build());

        // ── Agencies ────────────────────────────────────────────
        Agency agencyCasa = agencyRepository.save(new Agency(
                null, "Agence Casablanca Centre", "123 Bd Mohammed V", "MA",
                new BigDecimal("500000"), new BigDecimal("150000"),
                true, null, null));

        Agency agencyRabat = agencyRepository.save(new Agency(
                null, "Agence Rabat Hassan", "45 Av Allal Ben Abdellah", "MA",
                new BigDecimal("300000"), new BigDecimal("80000"),
                true, null, null));

        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@admin.com");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            admin.setNotifyEmail(true);
            admin.setNotifySms(true);
            admin.setPhone("+212600000000");
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());
            userRepository.save(admin);
            System.out.println("✅ Admin user created: admin/admin");
        }
        entityManager.flush();


        entityManager.flush();
        System.out.println("✅ DataSeeder: seed completed successfully.");
    }

    private BigDecimal bd(String val) {
        return new BigDecimal(val);
    }
}