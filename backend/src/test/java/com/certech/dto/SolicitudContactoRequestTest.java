package com.certech.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

class SolicitudContactoRequestTest {
    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    void acceptsAValidContactRequest() {
        assertTrue(validator.validate(validRequest()).isEmpty());
    }

    @Test
    void rejectsInvalidEmailServiceAndMissingPrivacyAcceptance() {
        SolicitudContactoRequest request = new SolicitudContactoRequest(
                "Ada Lovelace", "not-an-email", null, "Servicio inventado", null,
                "Necesito una aplicación web.", false);
        Set<?> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.toString().contains("correo")));
        assertTrue(violations.stream().anyMatch(v -> v.toString().contains("servicio")));
        assertTrue(violations.stream().anyMatch(v -> v.toString().contains("aceptaPrivacidad")));
    }

    static SolicitudContactoRequest validRequest() {
        return new SolicitudContactoRequest("Ada Lovelace", "ada@example.com", null,
                "Desarrollo Web", null, "Necesito una aplicación web.", true);
    }
}
