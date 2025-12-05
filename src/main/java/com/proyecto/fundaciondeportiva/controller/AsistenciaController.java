// src/main/java/com/proyecto/fundaciondeportiva/controller/AsistenciaController.java
package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.request.RegistrarAsistenciasSesionRequest;
import com.proyecto.fundaciondeportiva.dto.response.AsistenciaAlumnoSemanaDTO;
import com.proyecto.fundaciondeportiva.dto.response.AsistenciaDetalleAlumnoDTO;
import com.proyecto.fundaciondeportiva.service.AsistenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asistencias")
public class AsistenciaController {

    @Autowired
    private AsistenciaService asistenciaService;

    // ======== DOCENTE / ADMIN / COORDINADOR: VER ASISTENCIAS DE UNA SESIÓN ========

    @GetMapping(
            value = "/sesion/{sesionId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('PROFESOR','ADMINISTRADOR','COORDINADOR')")
    public ResponseEntity<List<AsistenciaDetalleAlumnoDTO>> obtenerAsistenciasSesion(
            @PathVariable Long sesionId
    ) {
        List<AsistenciaDetalleAlumnoDTO> lista =
                asistenciaService.obtenerAsistenciasPorSesion(sesionId);
        return ResponseEntity.ok(lista);
    }

    // Registrar/actualizar asistencias de una sesión
    @PostMapping(
            value = "/registrar-sesion",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('PROFESOR','ADMINISTRADOR','COORDINADOR')")
    public ResponseEntity<Void> registrarAsistenciasSesion(
            @RequestBody RegistrarAsistenciasSesionRequest request,
            Authentication authentication
    ) {
        String emailUsuario = authentication.getName();
        asistenciaService.registrarAsistenciasSesion(request, emailUsuario);
        return ResponseEntity.ok().build();
    }

    // ======== ALUMNO ========

    @GetMapping(
            value = "/mis-asistencias",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ALUMNO')")
    public ResponseEntity<List<AsistenciaAlumnoSemanaDTO>> obtenerMisAsistencias(
            @RequestParam Long seccionId,
            Authentication authentication
    ) {
        String emailAlumno = authentication.getName();
        List<AsistenciaAlumnoSemanaDTO> lista =
                asistenciaService.obtenerMisAsistenciasEnSeccion(seccionId, emailAlumno);
        return ResponseEntity.ok(lista);
    }
}