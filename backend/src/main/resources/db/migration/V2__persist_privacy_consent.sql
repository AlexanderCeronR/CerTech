-- Existing rows remain NULL: this database has no evidence to prove historic consent.
ALTER TABLE solicitudes_contacto
    ADD COLUMN acepta_privacidad BOOLEAN,
    ADD COLUMN privacidad_aceptada_en TIMESTAMPTZ;
