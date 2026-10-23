package com.certech.notification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.time.OffsetDateTime;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

class ContactEmailTest {
    static NotificationSettings settings(boolean enabled) {
        return new NotificationSettings(enabled, "owner@example.invalid", "sender@example.invalid", "America/Bogota", 3, 1, 60, 300, 1);
    }
    static ContactNotification item(String correo, String untrusted) {
        return new ContactNotification(UUID.randomUUID(), UUID.randomUUID(), 1L, 1, "owner@example.invalid", untrusted,
                correo, untrusted, "Desarrollo Web", untrusted, untrusted, OffsetDateTime.parse("2026-10-21T17:30:00Z"));
    }

    @Test void rendersAllVisitorFieldsAsEscapedTextAndIdentifiesTheTimezone() throws Exception {
        var content = new ContactEmailTemplate().render(item("qa@example.invalid", "<img src=x onerror=alert(1)> & \" '"), "America/Bogota");
        java.nio.file.Files.createDirectories(java.nio.file.Path.of("target"));
        java.nio.file.Files.writeString(java.nio.file.Path.of("target/email-preview.html"), content.html());
        assertFalse(content.html().contains("<img"));
        assertFalse(content.html().contains("<script"));
        assertTrue(content.html().contains("&lt;img src=x onerror=alert(1)&gt; &amp; &quot; &#39;"));
        assertTrue(content.html().contains("2026-10-21 12:30:00 -05:00 [America/Bogota]"));
        assertTrue(content.plain().contains("<img src=x"));
        for (String label : new String[]{"Nombre", "Correo", "Empresa", "Servicio", "Presupuesto", "Descripción", "Fecha de recepción"}) {
            assertTrue(content.plain().contains(label + ":"));
        }
    }

    @Test void rejectsHeaderInjectionMultipleAddressesAndDisplayNames() {
        for (String value : new String[]{"qa@example.invalid\r\nBcc: other@example.invalid", "a@example.invalid,b@example.invalid", "Client <qa@example.invalid>", "bad", ""}) {
            assertFalse(EmailAddresses.validSingle(value));
        }
        assertTrue(EmailAddresses.validSingle("qa@example.invalid"));
    }

    @Test void smtpHasAuthorizedFromValidatedReplyToAlternativesAndStableMessageId() throws Exception {
        JavaMailSender mail = mock(JavaMailSender.class);
        when(mail.createMimeMessage()).thenAnswer(call -> new MimeMessage(Session.getInstance(new Properties())));
        AtomicReference<MimeMessage> captured = new AtomicReference<>();
        doAnswer(call -> { captured.set(call.getArgument(0)); return null; }).when(mail).send(any(MimeMessage.class));
        var sender = new SmtpNotificationSender(mail, settings(true), new ContactEmailTemplate());
        var notification = item("qa@example.invalid", "<script>alert(1)</script>");
        sender.send(notification);
        MimeMessage first = captured.get(); first.saveChanges();
        assertEquals("sender@example.invalid", first.getFrom()[0].toString());
        assertEquals("owner@example.invalid", first.getAllRecipients()[0].toString());
        assertEquals("qa@example.invalid", first.getReplyTo()[0].toString());
        assertEquals(ContactEmailTemplate.SUBJECT, first.getSubject());
        String id = first.getMessageID();
        first.saveChanges(); assertEquals(id, first.getMessageID());
        var output = new java.io.ByteArrayOutputStream(); first.writeTo(output);
        String mime = output.toString(java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(mime.contains("multipart/alternative"));
        assertTrue(mime.contains("text/plain")); assertTrue(mime.contains("text/html"));
        sender.send(notification); captured.get().saveChanges();
        assertEquals(id, captured.get().getMessageID());
        assertNull(first.getHeader("Bcc"));
    }

    @Test void invalidClientMailboxCannotInjectReplyTo() throws Exception {
        JavaMailSender mail = mock(JavaMailSender.class);
        when(mail.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        AtomicReference<MimeMessage> captured = new AtomicReference<>();
        doAnswer(call -> { captured.set(call.getArgument(0)); return null; }).when(mail).send(any(MimeMessage.class));
        new SmtpNotificationSender(mail, settings(true), new ContactEmailTemplate()).send(item("bad\r\nBcc: victim@example.invalid", "Test"));
        assertNull(captured.get().getHeader("Reply-To"));
    }

    @Test void retriesAreBoundedAndConfigurationFailsClosed() {
        var settings = settings(true);
        assertEquals(1, settings.retryDelay(1)); assertEquals(2, settings.retryDelay(2));
        assertEquals(60, settings.retryDelay(10));
        assertThrows(IllegalArgumentException.class, () -> new NotificationSettings(true, "owner@example.invalid", "", "America/Bogota", 3, 1, 60, 300, 1));
        assertThrows(IllegalArgumentException.class, () -> new NotificationSettings(false, "owner@example.invalid", "", "America/Bogota", 50, 1, 60, 300, 1));
    }

    @Test void disabledWorkerNeverClaimsOrSends() {
        var outbox = mock(ContactNotificationOutbox.class); var sender = mock(NotificationSender.class);
        new ContactNotificationWorker(outbox, sender, settings(false)).poll();
        verifyNoInteractions(outbox, sender);
    }
}
