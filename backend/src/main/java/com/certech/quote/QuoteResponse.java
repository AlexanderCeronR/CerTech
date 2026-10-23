package com.certech.quote;
import java.time.OffsetDateTime;
import java.util.UUID;
public record QuoteResponse(UUID id, String servicio, OffsetDateTime fechaCreacion) {}
