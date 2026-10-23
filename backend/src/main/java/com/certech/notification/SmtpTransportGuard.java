package com.certech.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.stereotype.Component;

/** Fail at startup rather than silently using plaintext SMTP or infinite timeouts. */
@Component
@ConditionalOnProperty(name="app.notifications.enabled", havingValue="true")
public class SmtpTransportGuard {
    public SmtpTransportGuard(MailProperties mail, NotificationSettings settings,
            @Value("${app.notifications.poll-ms:60000}") long pollMs) {
        if (mail.getHost() == null || mail.getHost().isBlank() || mail.getUsername() == null
                || mail.getUsername().isBlank() || mail.getPassword() == null || mail.getPassword().isBlank()
                || !"smtp".equals(mail.getProtocol()) || mail.getPort() == null || mail.getPort() < 1 || mail.getPort() > 65535
                || pollMs < 1000 || pollMs > 86400000) fail();
        for (String property : new String[]{"mail.smtp.auth", "mail.smtp.starttls.enable", "mail.smtp.starttls.required", "mail.smtp.ssl.checkserveridentity"}) {
            if (!"true".equalsIgnoreCase(mail.getProperties().get(property))) fail();
        }
        if ("true".equalsIgnoreCase(mail.getProperties().get("mail.debug"))) fail();
        long timeouts=0;
        for (String property : new String[]{"mail.smtp.connectiontimeout", "mail.smtp.timeout", "mail.smtp.writetimeout"}) {
            try {
                int value=Integer.parseInt(mail.getProperties().getOrDefault(property,"0"));
                if (value < 1 || value > 30000) fail();
                timeouts+=value;
            } catch (NumberFormatException ex) { fail(); }
        }
        if (settings.leaseSeconds()*1000L <= timeouts*4+60000) fail();
    }
    private static void fail() {
        throw new IllegalArgumentException("SMTP requires credentials, mandatory verified STARTTLS, bounded timeouts, sufficient lease and valid polling settings");
    }
}
