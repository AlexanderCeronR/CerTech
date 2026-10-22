package com.certech.dto;

import jakarta.validation.constraints.*;

public record SolicitudContactoRequest(
    @NotBlank(message = "El nombre es obligatorio.") @Size(min = 2, max = 120, message = "El nombre debe tener entre 2 y 120 caracteres.") String nombre,
    @NotBlank(message = "El correo es obligatorio.") @Email(message = "Escribe un correo electrónico válido.") @Size(max = 180, message = "El correo no debe superar 180 caracteres.") String correo,
    @Size(max = 140, message = "La empresa no debe superar 140 caracteres.") String empresa,
    @NotBlank(message = "Selecciona un servicio.") @Pattern(regexp = "^(Desarrollo Web|Ciberseguridad|Desarrollo de Software|Soporte TI|Shopify Development|Administración de Bases de Datos|Inteligencia Artificial y Automatización)$", message = "Selecciona uno de los servicios disponibles.") String servicio,
    @Size(max = 80, message = "El presupuesto no debe superar 80 caracteres.") String presupuesto,
    @NotBlank(message = "La descripción es obligatoria.") @Size(min = 10, max = 3000, message = "La descripción debe tener entre 10 y 3000 caracteres.") String descripcion,
    @AssertTrue(message = "Debes aceptar el uso de datos para responder a tu solicitud.") boolean aceptaPrivacidad
) {}
