// src/main/java/com/proyecto/fundaciondeportiva/controller/AlumnoHorarioController.java
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
@RequestMapping("/api/alumno")
public class AlumnoHorarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private SesionRepository sesionRepository;

    @GetMapping("/horario")
    @PreAuthorize("hasRole('ALUMNO')")
    public ResponseEntity<List<SesionHorarioDTO>> obtenerHorarioAlumno() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario alumno = usuarioService.obtenerUsuarioPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Alumno autenticado no encontrado"));

        Long alumnoId = alumno.getId();

        // Ajusta el método al nombre real de la relación en tu modelo
        List<Sesion> sesiones = sesionRepository
                .findBySeccion_Matriculas_Alumno_IdOrderByFechaAsc(alumnoId);

        List<SesionHorarioDTO> dtoList = sesiones.stream()
                .map(s -> SesionHorarioDTO.builder()
                        .sesionId(s.getId())
                        .fecha(s.getFecha().toString())
                        .horaInicio(s.getHoraInicio() != null ? s.getHoraInicio().toString() : null)
                        .horaFin(s.getHoraFin() != null ? s.getHoraFin().toString() : null)
                        .tituloCurso(s.getSeccion().getCurso().getTitulo())
                        .nombreSeccion(s.getSeccion().getNombre())
                        .aula(s.getSeccion().getAula())
                        .profesor(
                                s.getSeccion().getProfesor() != null
                                        ? s.getSeccion().getProfesor().getNombre()
                                        : null
                        )
                        .build()
                )
                .toList();

        return ResponseEntity.ok(dtoList);
    }
}
