package com.certech.service;

import java.time.OffsetDateTime;
import com.certech.dto.SolicitudContactoRequest;
import com.certech.dto.SolicitudContactoResponse;
import com.certech.entity.SolicitudContacto;
import com.certech.repository.SolicitudContactoStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SolicitudContactoService {
    private final SolicitudContactoStore repository;
    public SolicitudContactoService(SolicitudContactoStore repository) { this.repository = repository; }

    @Transactional
    public SolicitudContactoResponse crear(SolicitudContactoRequest request) {
        if (!request.aceptaPrivacidad()) {
            throw new IllegalArgumentException("Privacy consent is required.");
        }
        OffsetDateTime acceptedAt = OffsetDateTime.now();
        SolicitudContacto solicitud = new SolicitudContacto(
                request.nombre().trim(), request.correo().trim(), cleanOptional(request.empresa()),
                request.servicio().trim(), cleanOptional(request.presupuesto()), request.descripcion().trim(),
                acceptedAt, "NUEVA", true, acceptedAt);
        SolicitudContacto saved = repository.save(solicitud);
        return new SolicitudContactoResponse(saved.getId(), saved.getServicio(), saved.getFechaCreacion());
    }

    private String cleanOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
