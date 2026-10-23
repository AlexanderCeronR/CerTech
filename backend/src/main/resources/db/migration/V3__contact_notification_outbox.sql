-- One durable notification per contact; no duplicated contact payload or credentials.
CREATE TABLE contact_notification_outbox (
    id UUID PRIMARY KEY,
    contact_id BIGINT NOT NULL UNIQUE REFERENCES solicitudes_contacto(id) ON DELETE CASCADE,
    recipient VARCHAR(180) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','PROCESSING','SENT','FAILED')),
    attempts INTEGER NOT NULL DEFAULT 0 CHECK (attempts >= 0),
    available_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lease_token UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMPTZ,
    last_error_code VARCHAR(40)
);
CREATE INDEX idx_contact_notification_due ON contact_notification_outbox(available_at, created_at)
    WHERE status IN ('PENDING', 'PROCESSING');
