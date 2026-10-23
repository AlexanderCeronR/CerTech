package com.certech.notification;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ContactNotification(UUID id, UUID leaseToken, long contactId, int attempts, String recipient,
        String nombre, String correo, String empresa, String servicio, String presupuesto,
        String descripcion, OffsetDateTime receivedAt) {}
