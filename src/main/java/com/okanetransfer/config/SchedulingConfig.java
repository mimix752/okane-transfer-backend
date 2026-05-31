package com.okanetransfer.config;

import com.okanetransfer.service.AlertService;
import com.okanetransfer.service.ExchangeRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class SchedulingConfig {

    private static final Logger logger = LoggerFactory.getLogger(SchedulingConfig.class);

    @Autowired(required = false)
    private ExchangeRateService exchangeRateService;

    @Autowired(required = false)
    private AlertService alertService;

    @Scheduled(cron = "${scheduling.exchange-rate.cron:0 0 * * * *}", zone = "UTC")
    public void syncExchangeRatesScheduled() {
        if (exchangeRateService == null) return;
        try {
            logger.info("Scheduled: syncing exchange rates...");
            exchangeRateService.syncFromApi("SCHEDULER");
            logger.info("Exchange rate sync completed");
        } catch (Exception e) {
            logger.error("Exchange rate sync error", e);
        }
    }

    @Scheduled(cron = "${scheduling.volume-check.cron:0 */15 * * * *}", zone = "UTC")
    public void checkVolumeAnomaliesScheduled() {
        if (alertService == null) return;
        try {
            logger.info("Scheduled: checking volume anomalies...");
            alertService.checkVolumeAnomalies();
            logger.info("Volume check completed");
        } catch (Exception e) {
            logger.error("Volume check error", e);
        }
    }

    @Scheduled(cron = "${scheduling.balance-check.cron:0 */30 * * * *}", zone = "UTC")
    public void checkLowBalancesScheduled() {
        if (alertService == null) return;
        try {
            logger.info("Scheduled: checking low balances...");
            alertService.checkLowBalances();
            logger.info("Balance check completed");
        } catch (Exception e) {
            logger.error("Balance check error", e);
        }
    }
}

