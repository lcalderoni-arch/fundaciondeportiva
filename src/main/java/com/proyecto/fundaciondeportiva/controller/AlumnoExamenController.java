package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.response.AlumnoExamenDTO;
import com.proyecto.fundaciondeportiva.model.entity.Examen;
import com.proyecto.fundaciondeportiva.model.entity.NotaExamen;
import com.proyecto.fundaciondeportiva.model.entity.Seccion;
import com.proyecto.fundaciondeportiva.model.entity.Usuario;
import com.proyecto.fundaciondeportiva.model.enums.EstadoNotaExamen;
import com.proyecto.fundaciondeportiva.repository.NotaExamenRepository;
import com.proyecto.fundaciondeportiva.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alumno")
public class AlumnoExamenController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private NotaExamenRepository notaExamenRepository;

    private Usuario getAlumnoActual() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioService.obtenerUsuarioPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Alumno autenticado no encontrado"));
    }

    @GetMapping("/examenes")
    @PreAuthorize("hasRole('ALUMNO')")
    public ResponseEntity<List<AlumnoExamenDTO>> listarExamenesAlumno() {

        Usuario alumno = getAlumnoActual();

        List<NotaExamen> notas = notaExamenRepository.findByAlumno_Id(alumno.getId());

        List<AlumnoExamenDTO> dtoList = notas.stream().map(n -> {
            Examen ex = n.getExamen();
            Seccion seccion = ex.getSeccion();

            String estadoStr = n.getEstado() != null ? n.getEstado().name() : EstadoNotaExamen.PENDIENTE.name();

            return AlumnoExamenDTO.builder()
                    .examenId(ex.getId())
                    .cursoTitulo(seccion.getCurso().getTitulo())
                    .seccionNombre(seccion.getNombre())
                    .tituloExamen(ex.getTitulo())
                    .fecha(ex.getFecha() != null ? ex.getFecha().toString() : null)
                    .nota(n.getNota())
                    .estado(estadoStr)
                    .pesoPorcentual(ex.getPesoPorcentual())
                    .notaMaxima(ex.getNotaMaxima())
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }
}
