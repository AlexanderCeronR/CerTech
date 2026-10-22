package com.certech.repository;

import com.certech.entity.SolicitudContacto;
import org.springframework.stereotype.Repository;

@Repository
public class JpaSolicitudContactoStore implements SolicitudContactoStore {
    private final SolicitudContactoRepository repository;

    public JpaSolicitudContactoStore(SolicitudContactoRepository repository) {
        this.repository = repository;
    }

    @Override
    public SolicitudContacto save(SolicitudContacto solicitud) {
        return repository.save(solicitud);
    }
}
