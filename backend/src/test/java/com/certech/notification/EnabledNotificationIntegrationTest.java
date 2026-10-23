package com.certech.notification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.certech.dto.SolicitudContactoRequest;
import com.certech.service.SolicitudContactoService;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@EnabledIfEnvironmentVariable(named="CERTECH_SYNTHETIC_DB_ONLY", matches="YES")
@SpringBootTest(properties={"app.notifications.enabled=true", "app.notifications.recipient=owner@example.invalid",
    "app.notifications.from=sender@example.invalid", "app.notifications.poll-ms=86400000",
    "spring.mail.host=smtp.example.invalid", "spring.mail.username=synthetic", "spring.mail.password=test-only"})
class EnabledNotificationIntegrationTest {
    @DynamicPropertySource static void database(DynamicPropertyRegistry registry) { ContactOutboxIntegrationTest.database(registry); }
    @MockitoBean JavaMailSender mail;
    @Autowired SolicitudContactoService contacts;
    @Autowired ContactNotificationWorker worker;
    @Autowired JdbcTemplate jdbc;

    @Test void enabledSpringWiringPersistsThenBuildsAndSendsMimeThroughSimulatedSmtp() throws Exception {
        jdbc.execute("TRUNCATE solicitudes_contacto CASCADE");
        when(mail.createMimeMessage()).thenAnswer(call -> new MimeMessage(Session.getInstance(new Properties())));
        var captured = new AtomicReference<MimeMessage>();
        doAnswer(call -> { captured.set(call.getArgument(0)); return null; }).when(mail).send(any(MimeMessage.class));
        contacts.crear(new SolicitudContactoRequest("Synthetic Client", "qa@example.invalid", null,
                "Desarrollo Web", null, "Prueba exclusivamente sintética del flujo habilitado.", true));
        verify(mail, never()).send(any(MimeMessage.class));
        worker.poll(); worker.poll();
        verify(mail, times(1)).send(any(MimeMessage.class));
        assertEquals("SENT", jdbc.queryForObject("SELECT status FROM contact_notification_outbox", String.class));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM solicitudes_contacto", Integer.class));
        assertEquals(ContactEmailTemplate.SUBJECT, captured.get().getSubject());
        assertEquals("sender@example.invalid", captured.get().getFrom()[0].toString());
        assertEquals("owner@example.invalid", captured.get().getAllRecipients()[0].toString());
    }
}
