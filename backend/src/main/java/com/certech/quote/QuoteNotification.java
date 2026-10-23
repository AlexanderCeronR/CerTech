package com.certech.quote;

import java.time.OffsetDateTime;
import java.util.UUID;

public record QuoteNotification(UUID id, UUID leaseToken, UUID quoteId, int attempts, String recipient,
        String nombre, String correo, String empresa, String servicio, String presupuesto,
        String descripcion, OffsetDateTime receivedAt, String titulo, String objetivo, String requisitos, String plazo, String telefono, String pais) {}
