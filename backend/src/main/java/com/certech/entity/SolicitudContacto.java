package com.certech.entity;

import java.time.OffsetDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "solicitudes_contacto")
public class SolicitudContacto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120) private String nombre;
    @Column(nullable = false, length = 180) private String correo;
    @Column(length = 140) private String empresa;
    @Column(nullable = false, length = 100) private String servicio;
    @Column(length = 80) private String presupuesto;
    @Column(nullable = false, length = 3000) private String descripcion;
    @Column(name = "fecha_creacion", nullable = false) private OffsetDateTime fechaCreacion;
    @Column(nullable = false, length = 30) private String estado;
    // Nullable for legacy records created before consent evidence was persisted.
    @Column(name = "acepta_privacidad") private Boolean aceptaPrivacidad;
    @Column(name = "privacidad_aceptada_en") private OffsetDateTime privacidadAceptadaEn;

    protected SolicitudContacto() {}

    public SolicitudContacto(String nombre, String correo, String empresa, String servicio,
                             String presupuesto, String descripcion, OffsetDateTime fechaCreacion, String estado,
                             Boolean aceptaPrivacidad, OffsetDateTime privacidadAceptadaEn) {
        this.nombre = nombre; this.correo = correo; this.empresa = empresa; this.servicio = servicio;
        this.presupuesto = presupuesto; this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion; this.estado = estado;
        this.aceptaPrivacidad = aceptaPrivacidad; this.privacidadAceptadaEn = privacidadAceptadaEn;
    }
    public Long getId() { return id; }
    public String getServicio() { return servicio; }
    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public Boolean getAceptaPrivacidad() { return aceptaPrivacidad; }
    public OffsetDateTime getPrivacidadAceptadaEn() { return privacidadAceptadaEn; }
}
