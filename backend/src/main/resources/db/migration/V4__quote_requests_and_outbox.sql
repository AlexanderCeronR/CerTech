CREATE TABLE solicitudes_cotizacion (
 id UUID PRIMARY KEY, request_key UUID NOT NULL UNIQUE, payload_hash CHAR(64) NOT NULL,
 servicio VARCHAR(100) NOT NULL, titulo VARCHAR(160) NOT NULL, descripcion VARCHAR(3000) NOT NULL,
 objetivo VARCHAR(1000) NOT NULL, requisitos VARCHAR(1500) NOT NULL, plazo VARCHAR(20) NOT NULL,
 presupuesto VARCHAR(20), nombre VARCHAR(120) NOT NULL, correo VARCHAR(180) NOT NULL,
 empresa VARCHAR(140), telefono VARCHAR(32), pais VARCHAR(80), acepta_privacidad BOOLEAN NOT NULL CHECK(acepta_privacidad),
 fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 consentimiento_fecha TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE quote_notification_outbox (
 id UUID PRIMARY KEY, quote_id UUID NOT NULL UNIQUE REFERENCES solicitudes_cotizacion(id) ON DELETE CASCADE,
 recipient VARCHAR(180) NOT NULL, status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK(status IN ('PENDING','PROCESSING','SENT','FAILED')),
 attempts INTEGER NOT NULL DEFAULT 0 CHECK(attempts>=0), available_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 lease_token UUID, created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, accepted_at TIMESTAMPTZ, last_error_code VARCHAR(40)
);
CREATE INDEX idx_quote_notification_due ON quote_notification_outbox(available_at,created_at) WHERE status IN ('PENDING','PROCESSING');
