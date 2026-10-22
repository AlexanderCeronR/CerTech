package com.certech.repository;

import com.certech.entity.SolicitudContacto;

@FunctionalInterface
public interface SolicitudContactoStore {
    SolicitudContacto save(SolicitudContacto solicitud);
}
