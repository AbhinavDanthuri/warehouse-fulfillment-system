package com.abhinav.warehouse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "app.fulfillment")
@Getter @Setter
public class FulfillmentConfig {

    public enum LockingStrategy { PESSIMISTIC, OPTIMISTIC }

    /** Which strategy the engine uses. Flip it in application.yml and re-run the test. */
    private LockingStrategy lockingStrategy = LockingStrategy.PESSIMISTIC;

    /** Only meaningful for OPTIMISTIC — how many times to retry a version clash. */
    private int maxRetries = 5;

    /** Base backoff in ms between optimistic retries (multiplied by attempt number). */
    private long retryBackoffMs = 20;
}
