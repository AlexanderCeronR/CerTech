CREATE TABLE solicitudes_contacto (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    correo VARCHAR(180) NOT NULL,
    empresa VARCHAR(140),
    servicio VARCHAR(100) NOT NULL,
    presupuesto VARCHAR(80),
    descripcion VARCHAR(3000) NOT NULL,
    fecha_creacion TIMESTAMPTZ NOT NULL,
    estado VARCHAR(30) NOT NULL
);
CREATE INDEX idx_solicitudes_contacto_fecha ON solicitudes_contacto (fecha_creacion DESC);
