package com.certech.controller;

import com.certech.dto.SolicitudContactoRequest;
import com.certech.dto.SolicitudContactoResponse;
import com.certech.service.SolicitudContactoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contacto")
public class ContactoController {
    private final SolicitudContactoService service;
    public ContactoController(SolicitudContactoService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<SolicitudContactoResponse> crear(@Valid @RequestBody SolicitudContactoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }
}
