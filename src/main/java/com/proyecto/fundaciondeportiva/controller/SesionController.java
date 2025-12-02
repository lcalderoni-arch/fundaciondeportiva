// src/main/java/com/proyecto/fundaciondeportiva/controller/SesionController.java
package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.model.entity.Sesion;
import com.proyecto.fundaciondeportiva.repository.SesionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sesiones")
public class SesionController {

    @Autowired
    private SesionRepository sesionRepository;

    @GetMapping("/seccion/{seccionId}")
    @PreAuthorize("hasAnyRole('PROFESOR','ALUMNO')")
    public ResponseEntity<List<Sesion>> listarPorSeccion(@PathVariable Long seccionId) {
        List<Sesion> sesiones = sesionRepository.findBySeccionIdOrderByFechaAsc(seccionId);
        return ResponseEntity.ok(sesiones);
    }
}
