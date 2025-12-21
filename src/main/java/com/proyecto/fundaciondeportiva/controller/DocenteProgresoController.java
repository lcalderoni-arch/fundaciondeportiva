package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.response.ProgresoDocenteResumenDTO;
import com.proyecto.fundaciondeportiva.dto.response.SeccionDocenteProgresoDTO;
import com.proyecto.fundaciondeportiva.model.entity.Matricula;
import com.proyecto.fundaciondeportiva.model.entity.Seccion;
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
@RequestMapping("/api/docente")
public class DocenteProgresoController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/progreso")
    @PreAuthorize("hasAuthority('PROFESOR')")
    @Transactional
    public ResponseEntity<ProgresoDocenteResumenDTO> obtenerProgresoDocente() {

        var auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("AUTH name = " + auth.getName());
        System.out.println("AUTH authorities = " + auth.getAuthorities());

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Usuario profesor = usuarioService.obtenerUsuarioPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Profesor autenticado no encontrado"));

        // Asegúrate de que Usuario tenga algo como:
        // @OneToMany(mappedBy = "profesor")
        // private Set<Seccion> seccionesAsignadas;
        Set<Seccion> secciones = profesor.getSeccionesAsignadas();

        int totalSecciones = secciones.size();

        // Construimos DTO por sección
        List<SeccionDocenteProgresoDTO> seccionesDTO = secciones.stream()
                .map(seccion -> {
                    var curso = seccion.getCurso();

                    // Solo matrículas activas
                    List<Matricula> matriculasActivas = seccion.getMatriculas().stream()
                            .filter(m -> m.getEstado() == EstadoMatricula.ACTIVA)
                            .toList();

                    int alumnosActivos = matriculasActivas.size();

                    DoubleSummaryStatistics statsNotas = matriculasActivas.stream()
                            .filter(m -> m.getCalificacionFinal() != null)
                            .mapToDouble(Matricula::getCalificacionFinal)
                            .summaryStatistics();

                    Double notaPromedioSeccion = statsNotas.getCount() > 0
                            ? statsNotas.getAverage()
                            : null;

                    return SeccionDocenteProgresoDTO.builder()
                            .seccionId(seccion.getId())
                            .cursoTitulo(curso.getTitulo())
                            .seccionNombre(seccion.getNombre())
                            .alumnosActivos(alumnosActivos)
                            .notaPromedioSeccion(notaPromedioSeccion)
                            .build();
                })
                .collect(Collectors.toList());

        int totalAlumnos = seccionesDTO.stream()
                .mapToInt(SeccionDocenteProgresoDTO::getAlumnosActivos)
                .sum();

        // calculamos promedio global de calificaciones
        DoubleSummaryStatistics statsGlobal = seccionesDTO.stream()
                .filter(s -> s.getNotaPromedioSeccion() != null)
                .mapToDouble(SeccionDocenteProgresoDTO::getNotaPromedioSeccion)
                .summaryStatistics();

        Double calificacionPromedioGlobal = statsGlobal.getCount() > 0
                ? statsGlobal.getAverage()
                : null;

        ProgresoDocenteResumenDTO dto = ProgresoDocenteResumenDTO.builder()
                .totalSecciones(totalSecciones)
                .totalAlumnos(totalAlumnos)
                .calificacionPromedioGlobal(calificacionPromedioGlobal)
                .secciones(seccionesDTO)
                .build();

        return ResponseEntity.ok(dto);
    }
}
