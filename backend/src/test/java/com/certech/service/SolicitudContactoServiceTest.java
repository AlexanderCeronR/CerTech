package com.certech.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import com.certech.dto.SolicitudContactoRequest;
import com.certech.dto.SolicitudContactoResponse;
import com.certech.entity.SolicitudContacto;
import com.certech.repository.SolicitudContactoStore;
import org.junit.jupiter.api.Test;

class SolicitudContactoServiceTest {
    private SolicitudContacto saved;
    private final SolicitudContactoStore repository = solicitud -> { saved = solicitud; return solicitud; };
    private final SolicitudContactoService service = new SolicitudContactoService(repository);

    @Test
    void savesTheRequestAndReturnsOnlyThePublicConfirmationFields() {
        SolicitudContactoRequest request = new SolicitudContactoRequest(" Ada Lovelace ", " ada@example.com ",
                " ", "Desarrollo Web", " ", " Necesito una aplicación web. ", true);

        SolicitudContactoResponse response = service.crear(request);

        assertEquals("Desarrollo Web", response.servicio());
        assertEquals(OffsetDateTime.class, response.fechaCreacion().getClass());
        assertEquals(Boolean.TRUE, saved.getAceptaPrivacidad());
        assertNotNull(saved.getPrivacidadAceptadaEn());
        assertEquals(saved.getFechaCreacion(), saved.getPrivacidadAceptadaEn());
    }

    @Test
    void propagatesPersistenceFailureSoTheControllerCannotReturnCreated() {
        SolicitudContactoService failingService = new SolicitudContactoService(solicitud -> {
            throw new IllegalStateException("database unavailable");
        });
        assertThrows(IllegalStateException.class, () -> failingService.crear(new SolicitudContactoRequest(
                "Ada Lovelace", "ada@example.com", null, "Desarrollo Web", null,
                "Necesito una aplicación web.", true)));
    }

    @Test
    void serviceRejectsARequestWithoutPrivacyConsent() {
        assertThrows(IllegalArgumentException.class, () -> service.crear(new SolicitudContactoRequest(
                "Ada Lovelace", "ada@example.com", null, "Desarrollo Web", null,
                "Necesito una aplicación web.", false)));
    }
}
