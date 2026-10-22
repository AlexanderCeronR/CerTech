package com.certech.dto;

import java.time.OffsetDateTime;

public record SolicitudContactoResponse(Long id, String servicio, OffsetDateTime fechaCreacion) {}
