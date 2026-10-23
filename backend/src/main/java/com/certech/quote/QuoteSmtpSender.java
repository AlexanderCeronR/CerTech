package com.certech.quote;
import com.certech.notification.*;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
@Component
@ConditionalOnProperty(name="app.notifications.enabled",havingValue="true")
public class QuoteSmtpSender implements QuoteNotificationSender {
 private final SmtpNotificationSender transport; private final QuoteEmailTemplate template; private final NotificationSettings settings;
 public QuoteSmtpSender(SmtpNotificationSender transport,QuoteEmailTemplate template,NotificationSettings settings){this.transport=transport;this.template=template;this.settings=settings;}
 public void send(QuoteNotification item){transport.sendPrepared(item.id(),item.recipient(),item.correo(),QuoteEmailTemplate.SUBJECT,template.render(item,settings.zone()));}
}
