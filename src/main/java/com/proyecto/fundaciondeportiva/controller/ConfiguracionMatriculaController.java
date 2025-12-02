package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.request.ActualizarFechasMatriculaRequest;
import com.proyecto.fundaciondeportiva.dto.request.CambiarPermisoMatriculaRequest;
import com.proyecto.fundaciondeportiva.dto.response.ConfiguracionMatriculaResponse;
import com.proyecto.fundaciondeportiva.service.ConfiguracionMatriculaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/configuracion")
public class ConfiguracionMatriculaController {

    @Autowired
    private ConfiguracionMatriculaService configuracionMatriculaService;

    // ================================
    //   OBTENER CONFIGURACIÓN ACTUAL
    // ================================
    @GetMapping(
            value = "/matricula",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ConfiguracionMatriculaResponse> obtenerConfiguracionMatricula() {
        ConfiguracionMatriculaResponse response =
                configuracionMatriculaService.obtenerConfiguracionMatricula();
        return ResponseEntity.ok(response);
    }

    // ================================
    //   GUARDAR / ACTUALIZAR FECHAS
    // ================================
    @PostMapping(
            value = "/matricula",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ConfiguracionMatriculaResponse> guardarFechasMatricula(
            @RequestBody ActualizarFechasMatriculaRequest request
    ) {
        // El frontend envía "yyyy-MM-dd"
        LocalDate inicio = request.getFechaInicio() != null
                ? LocalDate.parse(request.getFechaInicio())
                : null;

        LocalDate fin = request.getFechaFin() != null
                ? LocalDate.parse(request.getFechaFin())
                : null;

        ConfiguracionMatriculaResponse response =
                configuracionMatriculaService.actualizarFechasMatricula(inicio, fin);

        return ResponseEntity.ok(response);
    }

    // ================================
    //   CAMBIAR PERMISO GLOBAL
    // ================================
    @PutMapping(
            value = "/matricula/permiso-matricula",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ConfiguracionMatriculaResponse> actualizarPermisoGlobalMatricula(
            @RequestBody CambiarPermisoMatriculaRequest request
    ) {
        ConfiguracionMatriculaResponse response =
                configuracionMatriculaService.actualizarPermisoGlobalMatricula(request.isHabilitado());
        return ResponseEntity.ok(response);
    }
}
