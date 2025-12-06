package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.response.EntregaTareaDTO;
import com.proyecto.fundaciondeportiva.service.EntregaTareaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tareas")
public class EntregaTareaController {

    @Autowired
    private EntregaTareaService entregaTareaService;

    @PostMapping(
            value = "/{recursoId}/entregar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ALUMNO')")
    public ResponseEntity<EntregaTareaDTO> entregarTarea(
            @PathVariable Long recursoId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "titulo", required = false) String titulo,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            Authentication authentication
    ) throws Exception {

        String email = authentication.getName();

        String urlArchivo = guardarArchivo(file, "uploads/entregas");

        EntregaTareaDTO dto = entregaTareaService.registrarEntrega(
                recursoId, titulo, descripcion, urlArchivo, email
        );
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{recursoId}/entregas")
    @PreAuthorize("hasAnyRole('PROFESOR','ADMINISTRADOR')")
    public ResponseEntity<List<EntregaTareaDTO>> listarEntregas(
            @PathVariable Long recursoId,
            Authentication authentication
    ) {
        String email = authentication.getName();
        List<EntregaTareaDTO> lista =
                entregaTareaService.listarEntregasPorRecurso(recursoId, email);
        return ResponseEntity.ok(lista);
    }

    private String guardarArchivo(MultipartFile file, String carpetaBase) throws IOException {
        Path uploadDir = Paths.get(carpetaBase);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String nuevoNombre = UUID.randomUUID() + extension;
        Path destino = uploadDir.resolve(nuevoNombre);
        Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/entregas/" + nuevoNombre;
    }
}
