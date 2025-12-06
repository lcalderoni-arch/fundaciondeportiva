package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.response.CursoAlumnoProgresoDTO;
import com.proyecto.fundaciondeportiva.dto.response.ProgresoAlumnoResumenDTO;
import com.proyecto.fundaciondeportiva.model.entity.Matricula;
import com.proyecto.fundaciondeportiva.model.entity.Usuario;
import com.proyecto.fundaciondeportiva.model.enums.EstadoMatricula;
import com.proyecto.fundaciondeportiva.service.UsuarioService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alumno")
public class AlumnoProgresoController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/progreso")
    @PreAuthorize("hasRole('ALUMNO')")
    @Transactional
    public ResponseEntity<ProgresoAlumnoResumenDTO> obtenerProgresoAlumno() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Usuario alumno = usuarioService.obtenerUsuarioPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Alumno autenticado no encontrado"));

        Set<Matricula> todasMatriculas = alumno.getMatriculas(); // asegúrate de tener el mapeo

        List<Matricula> matriculasActivas = todasMatriculas.stream()
                .filter(m -> m.getEstado() == EstadoMatricula.ACTIVA)
                .toList();

        int totalCursosActivos = matriculasActivas.size();

        // Construimos la lista de cursos
        List<CursoAlumnoProgresoDTO> cursosDTO = matriculasActivas.stream()
                .map(m -> {
                    var seccion = m.getSeccion();
                    var curso = seccion.getCurso();

                    Double notaFinal = m.getCalificacionFinal(); // puede ser null
                    Double avancePorcentaje = null;

                    // Si tu escala es de 0 a 20, convertimos a 0-100
                    if (notaFinal != null) {
                        avancePorcentaje = (notaFinal / 20.0) * 100.0;
                    }

                    return CursoAlumnoProgresoDTO.builder()
                            .seccionId(seccion.getId())
                            .cursoTitulo(curso.getTitulo())
                            .seccionNombre(seccion.getNombre())
                            .notaFinal(notaFinal)
                            .avancePorcentaje(avancePorcentaje)
                            .build();
                })
                .collect(Collectors.toList());

        // Promedios
        DoubleSummaryStatistics statsNotas = matriculasActivas.stream()
                .filter(m -> m.getCalificacionFinal() != null)
                .mapToDouble(Matricula::getCalificacionFinal)
                .summaryStatistics();

        Double notaPromedio = statsNotas.getCount() > 0
                ? statsNotas.getAverage()
                : null;

        Double avancePromedio = (notaPromedio != null)
                ? (notaPromedio / 20.0) * 100.0
                : null;

        ProgresoAlumnoResumenDTO dto = ProgresoAlumnoResumenDTO.builder()
                .totalCursosActivos(totalCursosActivos)
                .notaPromedio(notaPromedio)
                .avancePromedio(avancePromedio)
                .cursos(cursosDTO)
                .build();

        return ResponseEntity.ok(dto);
    }
}
