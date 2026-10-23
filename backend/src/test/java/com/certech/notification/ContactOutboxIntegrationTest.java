package com.certech.notification;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.certech.dto.SolicitudContactoRequest;
import com.certech.service.SolicitudContactoService;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Only the disposable loopback database created by tools/mail/local-integration.mjs. */
@EnabledIfEnvironmentVariable(named="CERTECH_SYNTHETIC_DB_ONLY", matches="YES")
@SpringBootTest(properties={"app.notifications.enabled=false", "app.notifications.max-attempts=3", "app.notifications.retry-seconds=1", "app.notifications.max-retry-seconds=60", "app.contact.rate-limit.max-requests=1000"})
@AutoConfigureMockMvc
class ContactOutboxIntegrationTest {
    @DynamicPropertySource static void database(DynamicPropertyRegistry registry) {
        String url = System.getenv("MAIL_TEST_DB_URL");
        if (url == null || !url.matches("jdbc:postgresql://127\\.0\\.0\\.1:[0-9]+/certech_mail_ci")) throw new IllegalStateException("Disposable loopback test DB required");
        registry.add("spring.datasource.url", () -> url);
        registry.add("spring.datasource.username", () -> "certech_mail_ci");
        registry.add("spring.datasource.password", () -> System.getenv("MAIL_TEST_DB_PASSWORD"));
    }
    @Autowired JdbcTemplate jdbc;
    @Autowired SolicitudContactoService contacts;
    @Autowired ContactNotificationOutbox outbox;
    @Autowired PlatformTransactionManager manager;
    @Autowired MockMvc mvc;

    @BeforeEach void cleanSyntheticDatabaseOnly() { jdbc.execute("TRUNCATE solicitudes_contacto CASCADE"); }
    SolicitudContactoRequest request() { return new SolicitudContactoRequest("Synthetic Client", "qa@example.invalid", "Synthetic company", "Desarrollo Web", "Por definir", "Prueba local exclusivamente sintética.", true); }
    int count(String table) { return jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class); }
    String state() { return jdbc.queryForObject("SELECT status FROM contact_notification_outbox", String.class); }
    int attempts() { return jdbc.queryForObject("SELECT attempts FROM contact_notification_outbox", Integer.class); }
    void due() { jdbc.update("UPDATE contact_notification_outbox SET available_at=CURRENT_TIMESTAMP-INTERVAL '1 second'"); }
    ContactNotificationWorker worker(NotificationSender sender) { return new ContactNotificationWorker(outbox, sender, ContactEmailTest.settings(true)); }

    @Test void validHttpContactCreatesBothRecordsWithoutSending() throws Exception {
        mvc.perform(post("/api/contacto").contentType("application/json").content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(request())))
                .andExpect(status().isCreated());
        assertEquals(1, count("solicitudes_contacto")); assertEquals(1, count("contact_notification_outbox"));
        assertEquals("PENDING", state()); assertEquals(0, attempts());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/actuator/health"))
                .andExpect(status().isOk());
    }
    @Test void invalidHttpContactCreatesNeitherRecord() throws Exception {
        mvc.perform(post("/api/contacto").contentType("application/json").content("{\"nombre\":\"x\",\"correo\":\"invalid\",\"aceptaPrivacidad\":false}"))
                .andExpect(status().isBadRequest());
        assertEquals(0, count("solicitudes_contacto")); assertEquals(0, count("contact_notification_outbox"));
    }
    @Test void queueFailureRollsBackTheContactTransaction() {
        jdbc.execute("ALTER TABLE contact_notification_outbox ADD CONSTRAINT synthetic_reject CHECK (false) NOT VALID");
        try { assertThrows(RuntimeException.class, () -> contacts.crear(request())); assertEquals(0, count("solicitudes_contacto")); }
        finally { jdbc.execute("ALTER TABLE contact_notification_outbox DROP CONSTRAINT synthetic_reject"); }
    }
    @Test void uniquenessAndSentStatusPreventOrdinaryDuplicates() {
        var contact = contacts.crear(request());
        new TransactionTemplate(manager).executeWithoutResult(tx -> { outbox.enqueue(contact.id()); outbox.enqueue(contact.id()); });
        assertEquals(1, count("contact_notification_outbox"));
        AtomicInteger sent = new AtomicInteger(); var worker = worker(item -> sent.incrementAndGet());
        worker.poll(); worker.poll(); assertEquals(1, sent.get()); assertEquals("SENT", state()); assertEquals(1, attempts());
    }
    @Test void smtpFailureKeepsContactAndRetriesAfterDueTimeAndWorkerRestart() {
        contacts.crear(request());
        worker(item -> { throw new IllegalStateException("simulated provider unavailable"); }).poll();
        assertEquals(1, count("solicitudes_contacto")); assertEquals("PENDING", state()); assertEquals(1, attempts());
        AtomicInteger sent = new AtomicInteger(); var restarted = worker(item -> sent.incrementAndGet());
        restarted.poll(); assertEquals(0, sent.get());
        due(); restarted.poll(); assertEquals(1, sent.get()); assertEquals("SENT", state()); assertEquals(2, attempts());
    }
    @Test void maximumAttemptsStopAutomaticRetries() {
        contacts.crear(request()); AtomicInteger sent = new AtomicInteger();
        var worker = worker(item -> { sent.incrementAndGet(); throw new IllegalStateException("simulated failure"); });
        for (int i=0;i<5;i++) { due(); worker.poll(); }
        assertEquals(3, sent.get()); assertEquals(3, attempts()); assertEquals("FAILED", state());
        assertEquals(1, count("solicitudes_contacto"));
    }
    @Test void leaseRecoversCrashAndStaleOwnerCannotAcknowledgeNewAttempt() {
        contacts.crear(request()); var first = outbox.claim().orElseThrow();
        assertTrue(outbox.claim().isEmpty()); due();
        var recovered = outbox.claim().orElseThrow();
        assertEquals(first.id(), recovered.id()); assertNotEquals(first.leaseToken(), recovered.leaseToken());
        outbox.accepted(first); assertEquals("PROCESSING", state());
        outbox.accepted(recovered); assertEquals("SENT", state()); assertEquals(2, attempts());
    }
    @Test void crashOnLastAttemptEventuallyBecomesFailed() {
        contacts.crear(request());
        for (int i=0;i<3;i++) { due(); assertTrue(outbox.claim().isPresent()); }
        due(); assertTrue(outbox.claim().isEmpty()); assertEquals("FAILED", state());
    }
    @Test void parallelWorkersClaimOneContactOnlyOnce() throws Exception {
        contacts.crear(request());
        try (var pool = Executors.newFixedThreadPool(2)) {
            var barrier = new CountDownLatch(1);
            var a = pool.submit(() -> { barrier.await(); return outbox.claim().isPresent(); });
            var b = pool.submit(() -> { barrier.await(); return outbox.claim().isPresent(); });
            barrier.countDown(); assertNotEquals(a.get(10, TimeUnit.SECONDS), b.get(10, TimeUnit.SECONDS));
        }
        assertEquals(1, attempts());
    }
}
