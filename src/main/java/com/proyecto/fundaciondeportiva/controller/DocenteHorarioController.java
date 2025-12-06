// src/main/java/com/proyecto/fundaciondeportiva/controller/DocenteHorarioController.java
package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.response.SesionHorarioDTO;
import com.proyecto.fundaciondeportiva.model.entity.Sesion;
import com.proyecto.fundaciondeportiva.model.entity.Usuario;
import com.proyecto.fundaciondeportiva.repository.SesionRepository;
import com.proyecto.fundaciondeportiva.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/docente")
public class DocenteHorarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private SesionRepository sesionRepository;

    @GetMapping("/horario")
    @PreAuthorize("hasRole('PROFESOR')")
    public ResponseEntity<List<SesionHorarioDTO>> obtenerHorarioDocente() {

        // 🔹 Por ahora no usamos el repositorio para evitar errores de compilación
        //    (porque findBySeccion_DniProfesorOrderByFechaAsc está comentado).

        // String email = SecurityContextHolder.getContext().getAuthentication().getName();
        // Usuario profesor = usuarioService.obtenerUsuarioPorEmail(email)
        //         .orElseThrow(() -> new RuntimeException("Profesor autenticado no encontrado"));
        //
        // String dniProfesor = profesor.getPerfilProfesor().getDni();
        //
        // List<Sesion> sesiones = sesionRepository
        //         .findBySeccion_DniProfesorOrderByFechaAsc(dniProfesor);
        //
        // List<SesionHorarioDTO> dtoList = sesiones.stream()
        //         .map(s -> SesionHorarioDTO.builder()
        //                 .sesionId(s.getId())
        //                 .fecha(s.getFecha().toString())
        //                 .horaInicio(s.getHoraInicio() != null ? s.getHoraInicio().toString() : null)
        //                 .horaFin(s.getHoraFin() != null ? s.getHoraFin().toString() : null)
        //                 .tituloCurso(s.getSeccion().getCurso().getTitulo())
        //                 .nombreSeccion(s.getSeccion().getNombre())
        //                 .aula(s.getSeccion().getAula())
        //                 .build()
        //         )
        //         .toList();

        // 🔸 Devolvemos lista vacía temporalmente
        return ResponseEntity.ok(List.of());
    }
}