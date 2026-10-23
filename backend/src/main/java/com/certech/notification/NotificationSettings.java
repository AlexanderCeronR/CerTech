package com.certech.notification;

import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("app.notifications")
public record NotificationSettings(
        @DefaultValue("false") boolean enabled,
        @DefaultValue("prodbydsceron@gmail.com") String recipient,
        @DefaultValue("") String from,
        @DefaultValue("America/Bogota") String zone,
        @DefaultValue("5") int maxAttempts,
        @DefaultValue("60") int retrySeconds,
        @DefaultValue("3600") int maxRetrySeconds,
        @DefaultValue("300") int leaseSeconds,
        @DefaultValue("1") int batchSize) {
    public NotificationSettings {
        EmailAddresses.requireSingle(recipient);
        ZoneId.of(zone);
        if (enabled) EmailAddresses.requireSingle(from);
        if (maxAttempts < 1 || maxAttempts > 10 || retrySeconds < 1 || maxRetrySeconds < retrySeconds
                || maxRetrySeconds > 86400 || leaseSeconds < 60 || leaseSeconds > 3600
                || batchSize < 1 || batchSize > 20) {
            throw new IllegalArgumentException("Invalid notification retry/lease/batch settings");
        }
    }
    public long retryDelay(int attempts) {
        return Math.min(maxRetrySeconds, (long) retrySeconds * (1L << Math.min(attempts - 1, 10)));
    }
}
