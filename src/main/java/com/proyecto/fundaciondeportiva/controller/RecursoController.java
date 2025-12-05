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
            @RequestParam("momento") String momento,         // EXPLORA / ESTUDIA / APLICA
            @RequestParam("tipoRecurso") String tipoRecurso, // PDF / WORD / VIDEO / etc.
            Authentication authentication
    ) throws Exception {

        String email = authentication.getName();

        // 📂 Carpeta (ajusta ruta según tu servidor)
        Path uploadDir = Paths.get("uploads/recursos");
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

        // URL pública (asegúrate de configurar un ResourceHandler para /uploads/**)
        String urlArchivo = "/uploads/recursos/" + nuevoNombre;

        RecursoDTO dto = recursoService.crearRecursoArchivo(
                sesionId,
                titulo,
                descripcion,
                momento,
                tipoRecurso,
                urlArchivo,
                email
        );

        return ResponseEntity.ok(dto);
    }
}
