package com.certech.notification;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Repository
public class ContactNotificationOutbox {
    private final JdbcTemplate jdbc;
    private final NotificationSettings settings;
    private final TransactionTemplate transactions;

    public ContactNotificationOutbox(JdbcTemplate jdbc, NotificationSettings settings, PlatformTransactionManager manager) {
        this.jdbc = jdbc; this.settings = settings; this.transactions = new TransactionTemplate(manager);
        this.transactions.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    // Must participate in the contact transaction: a queue failure rolls back both writes.
    @Transactional(propagation = Propagation.MANDATORY)
    public void enqueue(Long contactId) {
        jdbc.update("INSERT INTO contact_notification_outbox(id, contact_id, recipient) VALUES (?, ?, ?) ON CONFLICT (contact_id) DO NOTHING",
                UUID.randomUUID(), contactId, settings.recipient());
    }

    public Optional<ContactNotification> claim() {
        return transactions.execute(tx -> {
            jdbc.update("""
                    UPDATE contact_notification_outbox SET status='FAILED', lease_token=NULL, last_error_code='ATTEMPTS_EXHAUSTED'
                    WHERE status IN ('PENDING','PROCESSING') AND available_at <= CURRENT_TIMESTAMP AND attempts >= ?
                    """, settings.maxAttempts());
            var ids = jdbc.query("""
                    SELECT id FROM contact_notification_outbox WHERE status IN ('PENDING','PROCESSING')
                    AND available_at <= CURRENT_TIMESTAMP AND attempts < ?
                    ORDER BY available_at, created_at LIMIT 1 FOR UPDATE SKIP LOCKED
                    """, (rs, row) -> rs.getObject("id", UUID.class), settings.maxAttempts());
            if (ids.isEmpty()) return Optional.empty();
            UUID id = ids.getFirst(), token = UUID.randomUUID();
            jdbc.update("""
                    UPDATE contact_notification_outbox SET status='PROCESSING', attempts=attempts+1,
                    available_at=CURRENT_TIMESTAMP + (? * INTERVAL '1 second'), lease_token=? WHERE id=?
                    """, settings.leaseSeconds(), token, id);
            return Optional.of(jdbc.queryForObject("""
                    SELECT o.id,o.lease_token,o.contact_id,o.attempts,o.recipient,s.nombre,s.correo,s.empresa,
                    s.servicio,s.presupuesto,s.descripcion,s.fecha_creacion
                    FROM contact_notification_outbox o JOIN solicitudes_contacto s ON s.id=o.contact_id WHERE o.id=?
                    """, (rs, row) -> new ContactNotification(rs.getObject("id", UUID.class), rs.getObject("lease_token", UUID.class),
                    rs.getLong("contact_id"), rs.getInt("attempts"), rs.getString("recipient"), rs.getString("nombre"),
                    rs.getString("correo"), rs.getString("empresa"), rs.getString("servicio"), rs.getString("presupuesto"),
                    rs.getString("descripcion"), rs.getObject("fecha_creacion", OffsetDateTime.class)), id));
        });
    }

    public void accepted(ContactNotification item) {
        jdbc.update("""
                UPDATE contact_notification_outbox SET status='SENT',accepted_at=CURRENT_TIMESTAMP,
                lease_token=NULL,last_error_code=NULL WHERE id=? AND lease_token=? AND status='PROCESSING'
                """, item.id(), item.leaseToken());
    }

    public void failed(ContactNotification item) {
        jdbc.update("""
                UPDATE contact_notification_outbox SET status=?,available_at=CURRENT_TIMESTAMP + (? * INTERVAL '1 second'),
                lease_token=NULL,last_error_code='SMTP_SEND_FAILED' WHERE id=? AND lease_token=? AND status='PROCESSING'
                """, item.attempts() >= settings.maxAttempts() ? "FAILED" : "PENDING",
                settings.retryDelay(item.attempts()), item.id(), item.leaseToken());
    }
}
