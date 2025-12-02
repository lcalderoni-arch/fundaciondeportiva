package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.request.CambiarPermisoMatriculaRequest;
import com.proyecto.fundaciondeportiva.dto.response.ConfiguracionMatriculaResponse;
import com.proyecto.fundaciondeportiva.model.entity.ConfiguracionMatricula;
import com.proyecto.fundaciondeportiva.service.ConfiguracionMatriculaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configuracion/matricula")
public class ConfiguracionMatriculaController {

    @Autowired
    private ConfiguracionMatriculaService configuracionMatriculaService;

    // 👉 GET /api/configuracion/matricula
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ConfiguracionMatriculaResponse> obtenerConfiguracion() {
        ConfiguracionMatricula conf = configuracionMatriculaService.obtenerConfiguracionActual();
        return ResponseEntity.ok(ConfiguracionMatriculaResponse.deEntidad(conf));
    }

    // 👉 PUT /api/configuracion/matricula/permiso-matricula
    @PutMapping(value = "/permiso-matricula",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ConfiguracionMatriculaResponse> cambiarPermisoMatricula(
            @RequestBody CambiarPermisoMatriculaRequest request) {

        ConfiguracionMatricula confActualizada =
                configuracionMatriculaService.actualizarEstadoMatricula(request.isHabilitada());

        return ResponseEntity.ok(ConfiguracionMatriculaResponse.deEntidad(confActualizada));
    }
}
