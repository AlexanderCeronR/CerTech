package com.certech.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.certech.dto.SolicitudContactoRequest;
import com.certech.dto.SolicitudContactoResponse;
import com.certech.exception.ApiExceptionHandler;
import com.certech.service.SolicitudContactoService;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import com.certech.config.ContactRateLimitInterceptor;
import com.certech.config.WebConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@WebMvcTest(ContactoController.class)
@Import({ApiExceptionHandler.class, WebConfig.class, ContactoControllerTest.FakeServiceConfiguration.class})
class ContactoControllerTest {
    @Autowired private MockMvc mvc;
    @Autowired private FakeService service;

    @Test
    void returns201WhenTheRequestHasBeenPersisted() throws Exception {
        service.reset();

        mvc.perform(post("/api/contacto").contentType(MediaType.APPLICATION_JSON).content(validJson()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.servicio").value("Desarrollo Web"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'; base-uri 'none'"))
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
                .andExpect(header().string("Permissions-Policy", "camera=(), geolocation=(), microphone=()"));
        org.junit.jupiter.api.Assertions.assertEquals(1, service.calls);
    }

    @Test
    void returns400ForInvalidRequestData() throws Exception {
        mvc.perform(post("/api/contacto").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"correo\":\"bad\",\"servicio\":\"Otra cosa\",\"descripcion\":\"x\",\"aceptaPrivacidad\":false}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void returns500WhenPersistenceFailsInsteadOfClaimingSuccess() throws Exception {
        service.reset();
        mvc.perform(post("/api/contacto").contentType(MediaType.APPLICATION_JSON)
                        .content(validJson().replace("Ada Lovelace", "Trigger Failure")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("No fue posible procesar la solicitud. Inténtalo más tarde."))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist());
    }

    @Test
    void doesNotAllowCorsFromAnUnlistedOrigin() throws Exception {
        mvc.perform(options("/api/contacto").header("Origin", "https://untrusted.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void rateLimitReturns429AndRetryAfterWithoutClientDetails() throws Exception {
        ContactRateLimitInterceptor limiter = new ContactRateLimitInterceptor(1, 60);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/contacto");
        MockHttpServletResponse first = new MockHttpServletResponse();
        MockHttpServletResponse second = new MockHttpServletResponse();

        org.junit.jupiter.api.Assertions.assertTrue(limiter.preHandle(request, first, new Object()));
        org.junit.jupiter.api.Assertions.assertFalse(limiter.preHandle(request, second, new Object()));
        org.junit.jupiter.api.Assertions.assertEquals(429, second.getStatus());
        org.junit.jupiter.api.Assertions.assertNotNull(second.getHeader("Retry-After"));
        org.junit.jupiter.api.Assertions.assertFalse(second.getContentAsString().contains(request.getRemoteAddr()));
    }

    private String validJson() {
        return "{\"nombre\":\"Ada Lovelace\",\"correo\":\"ada@example.com\",\"servicio\":\"Desarrollo Web\",\"descripcion\":\"Necesito una aplicación web.\",\"aceptaPrivacidad\":true}";
    }

    @TestConfiguration
    static class FakeServiceConfiguration {
        @Bean @Primary FakeService fakeSolicitudContactoService() { return new FakeService(); }
    }

    static class FakeService extends SolicitudContactoService {
        private int calls;
        FakeService() { super(solicitud -> solicitud); }
        @Override public SolicitudContactoResponse crear(SolicitudContactoRequest request) {
            calls++;
            if (request.nombre().equals("Trigger Failure")) throw new IllegalStateException("database unavailable");
            return new SolicitudContactoResponse(7L, "Desarrollo Web", OffsetDateTime.parse("2026-10-21T12:00:00Z"));
        }
        void reset() { calls = 0; }
    }
}
