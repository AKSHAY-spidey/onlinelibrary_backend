package com.library.scheduler;

import com.library.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionScheduler.class);
    
    @Autowired
    private SubscriptionService subscriptionService;
    
    /**
     * Process subscription renewals daily at 1:00 AM
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processRenewals() {
        logger.info("Processing subscription renewals...");
        try {
            subscriptionService.processRenewals();
            logger.info("Subscription renewals processed successfully");
        } catch (Exception e) {
            logger.error("Error processing subscription renewals", e);
        }
    }
    
    /**
     * Process expired subscriptions daily at 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void processExpiredSubscriptions() {
        logger.info("Processing expired subscriptions...");
        try {
            subscriptionService.processExpiredSubscriptions();
            logger.info("Expired subscriptions processed successfully");
        } catch (Exception e) {
            logger.error("Error processing expired subscriptions", e);
        }
    }
}
