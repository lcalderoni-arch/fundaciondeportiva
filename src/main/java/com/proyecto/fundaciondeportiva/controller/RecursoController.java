// src/main/java/com/proyecto/fundaciondeportiva/controller/RecursoController.java
package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.request.RecursoRequest;
import com.proyecto.fundaciondeportiva.dto.response.RecursoDTO;
import com.proyecto.fundaciondeportiva.service.RecursoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/recursos")
public class RecursoController {

    @Autowired
    private RecursoService recursoService;

    // 🔹 Listar recursos de una sesión (docente, admin, alumno)
    @GetMapping("/sesion/{sesionId}")
    @PreAuthorize("hasAnyRole('PROFESOR','ADMINISTRADOR','ALUMNO')")
    public ResponseEntity<List<RecursoDTO>> listarPorSesion(@PathVariable Long sesionId) {
        List<RecursoDTO> lista = recursoService.listarPorSesion(sesionId);
        return ResponseEntity.ok(lista);
    }

    // 🔹 Crear recurso tipo LINK / simple (JSON)
    @PostMapping(
            value = "/crear",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('PROFESOR','ADMINISTRADOR')")
    public ResponseEntity<RecursoDTO> crearRecurso(
            @RequestBody RecursoRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        RecursoDTO dto = recursoService.crearRecurso(request, email);
        return ResponseEntity.ok(dto);
    }

    // 🔹 Subir archivo y asociarlo a una sesión
    @PostMapping(
            value = "/sesion/{sesionId}/archivo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('PROFESOR','ADMINISTRADOR')")
    public ResponseEntity<RecursoDTO> subirArchivo(
            @PathVariable Long sesionId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("titulo") String titulo,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam("momento") String momento,         // ANTES / DURANTE / DESPUES
            @RequestParam("tipoRecurso") String tipoRecurso, // PDF / DOCUMENTO / ARCHIVO / IMAGEN
            Authentication authentication
    ) {

        String email = authentication.getName();

        RecursoDTO dto = recursoService.crearRecursoArchivo(
                sesionId,
                titulo,
                descripcion,
                momento,
                tipoRecurso,
                file,
                email
        );

        return ResponseEntity.ok(dto);
    }

    // 🔹 ACTUALIZAR recurso (LINK o archivo)
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('PROFESOR','ADMINISTRADOR')")
    public ResponseEntity<RecursoDTO> actualizarRecurso(
            @PathVariable Long id,
            @RequestBody RecursoRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        RecursoDTO dto = recursoService.actualizarRecurso(id, request, email);
        return ResponseEntity.ok(dto);
    }

    // 🔹 ELIMINAR recurso
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROFESOR','ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarRecurso(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();
        recursoService.eliminarRecurso(id, email);
        return ResponseEntity.noContent().build();
    }
}
