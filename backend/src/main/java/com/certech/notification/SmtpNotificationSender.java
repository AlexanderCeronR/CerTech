package com.certech.notification;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.notifications.enabled", havingValue = "true")
public class SmtpNotificationSender implements NotificationSender {
    private final JavaMailSender sender;
    private final NotificationSettings settings;
    private final ContactEmailTemplate template;

    public SmtpNotificationSender(JavaMailSender sender, NotificationSettings settings, ContactEmailTemplate template) {
        this.sender = sender; this.settings = settings; this.template = template;
        EmailAddresses.requireSingle(settings.from());
    }

    @Override
    public void send(ContactNotification item) {
        sendPrepared(item.id(),item.recipient(),item.correo(),ContactEmailTemplate.SUBJECT,template.render(item,settings.zone()));
    }

    public void sendPrepared(java.util.UUID id,String recipient,String replyTo,String subject,ContactEmailTemplate.Content content) {
        EmailAddresses.requireSingle(recipient);
        try {
            // JavaMail normally regenerates Message-ID on every save: retain the same ID across retries.
            MimeMessage message = new StableMessage(sender.createMimeMessage().getSession(), id.toString());
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(settings.from());
            helper.setTo(recipient);
            helper.setSubject(subject);
            if (EmailAddresses.validSingle(replyTo)) helper.setReplyTo(replyTo);
            helper.setText(content.plain(), content.html());
            sender.send(message);
        } catch (MessagingException ex) {
            throw new MailPreparationException("Notification preparation failed");
        }
    }

    private static final class StableMessage extends MimeMessage {
        private final String id;
        StableMessage(Session session, String id) { super(session); this.id = id; }
        @Override protected void updateMessageID() throws MessagingException {
            setHeader("Message-ID", "<" + id + "@notifications.certech.invalid>");
        }
    }
}
