// src/main/java/com/proyecto/fundaciondeportiva/controller/SesionController.java
package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.response.SesionSimpleDTO;
import com.proyecto.fundaciondeportiva.exception.RecursoNoEncontradoException;
import com.proyecto.fundaciondeportiva.model.entity.Sesion;
import com.proyecto.fundaciondeportiva.model.entity.Seccion;
import com.proyecto.fundaciondeportiva.repository.SesionRepository;
import com.proyecto.fundaciondeportiva.repository.SeccionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sesiones")
public class SesionController {

    @Autowired
    private SesionRepository sesionRepository;

    @Autowired
    private SeccionRepository seccionRepository;

    @GetMapping("/seccion/{seccionId}")
    @PreAuthorize("hasAnyRole('PROFESOR','ALUMNO')")
    public ResponseEntity<List<SesionSimpleDTO>> listarPorSeccion(@PathVariable Long seccionId) {

        Seccion seccion = seccionRepository.findById(seccionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sección no encontrada con id: " + seccionId));

        List<Sesion> sesiones = sesionRepository.findBySeccion_IdOrderByFechaAsc(seccion.getId());

        // DEBUG
        System.out.println("=== DEBUG SESIONES ===");
        System.out.println("Sección solicitada ID path: " + seccionId + " | entidad.getId(): " + seccion.getId());
        System.out.println("Total sesiones encontradas: " + sesiones.size());
        for (Sesion s : sesiones) {
            Long secId = (s.getSeccion() != null) ? s.getSeccion().getId() : null;
            System.out.println("  Sesion ID " + s.getId()
                    + " | seccion_id=" + secId
                    + " | fecha=" + s.getFecha());
        }
        System.out.println("=======================");

        List<SesionSimpleDTO> dtoList = sesiones.stream()
                .map(SesionSimpleDTO::deEntidad)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }
}
