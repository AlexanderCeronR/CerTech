package com.certech.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.notifications.enabled", havingValue = "true")
public class ContactNotificationWorker {
    private static final Logger log = LoggerFactory.getLogger(ContactNotificationWorker.class);
    private final ContactNotificationOutbox outbox;
    private final NotificationSender sender;
    private final NotificationSettings settings;

    public ContactNotificationWorker(ContactNotificationOutbox outbox, NotificationSender sender, NotificationSettings settings) {
        this.outbox = outbox; this.sender = sender; this.settings = settings;
    }

    @Scheduled(fixedDelayString = "${app.notifications.poll-ms:60000}", initialDelayString = "${app.notifications.poll-ms:60000}")
    public void poll() {
        if (!settings.enabled()) return;
        try {
            for (int i = 0; i < settings.batchSize(); i++) {
                var claimed = outbox.claim();
                if (claimed.isEmpty()) return;
                ContactNotification item = claimed.get();
                try {
                    sender.send(item);
                } catch (RuntimeException ex) {
                    outbox.failed(item);
                    // Never log SMTP exceptions: they can contain message bodies, addresses or credentials.
                    log.warn("Contact notification attempt failed (notificationId={}, attempt={})", item.id(), item.attempts());
                    continue;
                }
                // If acknowledgement persistence fails after SMTP acceptance, keep the lease for recovery.
                outbox.accepted(item);
            }
        } catch (RuntimeException ex) {
            log.error("Contact notification worker unavailable; pending records remain in database");
        }
    }
}
