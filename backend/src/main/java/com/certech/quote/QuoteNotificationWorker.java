package com.certech.quote;
import com.certech.notification.NotificationSettings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.notifications.enabled", havingValue = "true")
public class QuoteNotificationWorker {
    private static final Logger log = LoggerFactory.getLogger(QuoteNotificationWorker.class);
    private final QuoteNotificationOutbox outbox;
    private final QuoteNotificationSender sender;
    private final NotificationSettings settings;

    public QuoteNotificationWorker(QuoteNotificationOutbox outbox, QuoteNotificationSender sender, NotificationSettings settings) {
        this.outbox = outbox; this.sender = sender; this.settings = settings;
    }

    @Scheduled(fixedDelayString = "${app.notifications.poll-ms:60000}", initialDelayString = "${app.notifications.poll-ms:60000}")
    public void poll() {
        if (!settings.enabled()) return;
        try {
            for (int i = 0; i < settings.batchSize(); i++) {
                var claimed = outbox.claim();
                if (claimed.isEmpty()) return;
                QuoteNotification item = claimed.get();
                try {
                    sender.send(item);
                } catch (RuntimeException ex) {
                    outbox.failed(item);
                    // Never log SMTP exceptions: they can contain message bodies, addresses or credentials.
                    log.warn("Quote notification attempt failed (notificationId={}, attempt={})", item.id(), item.attempts());
                    continue;
                }
                // If acknowledgement persistence fails after SMTP acceptance, keep the lease for recovery.
                outbox.accepted(item);
            }
        } catch (RuntimeException ex) {
            log.error("Quote notification worker unavailable; pending records remain in database");
        }
    }
}
