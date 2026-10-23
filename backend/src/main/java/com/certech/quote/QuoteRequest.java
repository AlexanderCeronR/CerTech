package com.certech.quote;
import jakarta.validation.constraints.*;
import java.util.UUID;
public record QuoteRequest(
 @NotNull UUID requestId,
 @NotBlank @Pattern(regexp="^(Desarrollo Web|Ciberseguridad|Desarrollo de Software|Soporte TI|Shopify Development|Administración de Bases de Datos|Inteligencia Artificial y Automatización)$") String servicio,
 @NotBlank @Size(min=2,max=160) String titulo,
 @NotBlank @Size(min=10,max=3000) String descripcion,
 @NotBlank @Size(min=5,max=1000) String objetivo,
 @NotBlank @Size(min=5,max=1500) String requisitos,
 @NotBlank @Pattern(regexp="^(flexible|month|quarter|later)$") String plazo,
 @Pattern(regexp="^(|under|small|medium|large|discuss)$") String presupuesto,
 @NotBlank @Size(min=2,max=120) String nombre,
 @NotBlank @Email @Size(max=180) @Pattern(regexp="^[^\\s\\r\\n]+@[^\\s\\r\\n]+$") String correo,
 @Size(max=140) String empresa,
 @Size(max=32) @Pattern(regexp="^[+0-9 ()\\-]*$") String telefono,
 @Size(max=80) String pais,
 @AssertTrue boolean aceptaPrivacidad) {
 public QuoteRequest {
  titulo=clean(titulo);descripcion=clean(descripcion);objetivo=clean(objetivo);requisitos=clean(requisitos);
  nombre=clean(nombre);correo=clean(correo);empresa=clean(empresa);telefono=clean(telefono);pais=clean(pais);
 }
 private static String clean(String value){return value==null?null:value.trim();}
}
