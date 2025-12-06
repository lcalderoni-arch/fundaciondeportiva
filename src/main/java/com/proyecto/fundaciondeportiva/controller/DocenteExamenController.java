package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.request.ExamenCrearDTO;
import com.proyecto.fundaciondeportiva.dto.request.NotaExamenActualizarDTO;
import com.proyecto.fundaciondeportiva.dto.response.ExamenResumenDocenteDTO;
import com.proyecto.fundaciondeportiva.dto.response.NotaExamenFilaDocenteDTO;
import com.proyecto.fundaciondeportiva.dto.response.NotasExamenDocenteDTO;
import com.proyecto.fundaciondeportiva.exception.RecursoNoEncontradoException;
import com.proyecto.fundaciondeportiva.model.entity.*;
import com.proyecto.fundaciondeportiva.model.enums.EstadoMatricula;
import com.proyecto.fundaciondeportiva.model.enums.EstadoNotaExamen;
import com.proyecto.fundaciondeportiva.repository.ExamenRepository;
import com.proyecto.fundaciondeportiva.repository.NotaExamenRepository;
import com.proyecto.fundaciondeportiva.repository.SeccionRepository;
import com.proyecto.fundaciondeportiva.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/docente")
public class DocenteExamenController {

    private static final double NOTA_MIN_APROBATORIA = 10.5;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private SeccionRepository seccionRepository;

    @Autowired
    private ExamenRepository examenRepository;

    @Autowired
    private NotaExamenRepository notaExamenRepository;

    private Usuario getProfesorActual() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioService.obtenerUsuarioPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Profesor autenticado no encontrado"));
    }

    private void validarSeccionDelProfesor(Seccion seccion, Usuario profesor) {
        if (seccion.getProfesor() == null ||
                !seccion.getProfesor().getId().equals(profesor.getId())) {
            throw new RuntimeException("No tienes permiso para gestionar esta sección.");
        }
    }

    // ================= LISTAR EXÁMENES POR SECCIÓN ==================

    @GetMapping("/secciones/{seccionId}/examenes")
    @PreAuthorize("hasRole('PROFESOR')")
    public ResponseEntity<List<ExamenResumenDocenteDTO>> listarExamenesSeccion(
            @PathVariable Long seccionId) {

        Usuario profesor = getProfesorActual();

        Seccion seccion = seccionRepository.findById(seccionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sección no encontrada"));

        validarSeccionDelProfesor(seccion, profesor);

        List<Examen> examenes = examenRepository.findBySeccion_IdOrderByOrdenAscIdAsc(seccionId);

        int totalAlumnos = (int) seccion.getMatriculas().stream()
                .filter(m -> m.getEstado() == EstadoMatricula.ACTIVA)
                .count();

        List<ExamenResumenDocenteDTO> dtoList = examenes.stream().map(ex -> {
            List<NotaExamen> notas = notaExamenRepository.findByExamen_Id(ex.getId());

            long conNota = notas.stream()
                    .filter(n -> n.getNota() != null)
                    .count();

            long aprobados = notas.stream()
                    .filter(n -> n.getNota() != null && n.getNota() >= NOTA_MIN_APROBATORIA)
                    .count();

            long desaprobados = notas.stream()
                    .filter(n -> n.getNota() != null && n.getNota() < NOTA_MIN_APROBATORIA)
                    .count();

            int pendientes = totalAlumnos - (int) conNota;

            DoubleSummaryStatistics stats = notas.stream()
                    .filter(n -> n.getNota() != null)
                    .mapToDouble(NotaExamen::getNota)
                    .summaryStatistics();

            Double promedio = stats.getCount() > 0 ? stats.getAverage() : null;

            return ExamenResumenDocenteDTO.builder()
                    .examenId(ex.getId())
                    .titulo(ex.getTitulo())
                    .descripcion(ex.getDescripcion())
                    .fecha(ex.getFecha() != null ? ex.getFecha().toString() : null)
                    .pesoPorcentual(ex.getPesoPorcentual())
                    .notaMaxima(ex.getNotaMaxima())
                    .orden(ex.getOrden())
                    .totalAlumnos(totalAlumnos)
                    .aprobados((int) aprobados)
                    .desaprobados((int) desaprobados)
                    .pendientes(pendientes)
                    .promedio(promedio)
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    // ================= CREAR EXAMEN ==================

    @PostMapping("/secciones/{seccionId}/examenes")
    @PreAuthorize("hasRole('PROFESOR')")
    public ResponseEntity<ExamenResumenDocenteDTO> crearExamen(
            @PathVariable Long seccionId,
            @RequestBody ExamenCrearDTO request) {

        Usuario profesor = getProfesorActual();

        Seccion seccion = seccionRepository.findById(seccionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sección no encontrada"));

        validarSeccionDelProfesor(seccion, profesor);

        long totalExamenes = examenRepository.countBySeccion_Id(seccionId);
        if (totalExamenes >= 10) {
            return ResponseEntity.badRequest()
                    .body(null); // opcional: lanzar excepción con mensaje más claro
        }

        // Validar peso
        if (request.getPesoPorcentual() == null || request.getPesoPorcentual() <= 0) {
            throw new IllegalArgumentException("El peso porcentual del examen es obligatorio y debe ser mayor a 0.");
        }

        // Validar sumatoria de pesos <= 100
        List<Examen> examenesActuales = examenRepository.findBySeccion_IdOrderByOrdenAscIdAsc(seccionId);
        double sumaPesosActual = examenesActuales.stream()
                .mapToDouble(Examen::getPesoPorcentual)
                .sum();

        double nuevoPeso = request.getPesoPorcentual();
        if (sumaPesosActual + nuevoPeso > 100.0 + 0.0001) {
            throw new IllegalArgumentException("La suma de los pesos de los exámenes no puede superar el 100%.");
        }

        Examen examen = new Examen();
        examen.setSeccion(seccion);
        examen.setTitulo(request.getTitulo());
        examen.setDescripcion(request.getDescripcion());
        examen.setPesoPorcentual(nuevoPeso);
        examen.setNotaMaxima(
                request.getNotaMaxima() != null ? request.getNotaMaxima() : 20.0);

        if (request.getFecha() != null) {
            try {
                examen.setFecha(LocalDate.parse(request.getFecha()));
            } catch (DateTimeParseException e) {
                // ignorar o lanzar error, a tu gusto
            }
        }

        if (request.getOrden() != null) {
            examen.setOrden(request.getOrden());
        } else {
            examen.setOrden((int) totalExamenes + 1);
        }

        examen.setActivo(true);

        examen = examenRepository.save(examen);

        int totalAlumnos = (int) seccion.getMatriculas().stream()
                .filter(m -> m.getEstado() == EstadoMatricula.ACTIVA).count();

        ExamenResumenDocenteDTO dto = ExamenResumenDocenteDTO.builder()
                .examenId(examen.getId())
                .titulo(examen.getTitulo())
                .descripcion(examen.getDescripcion())
                .fecha(examen.getFecha() != null ? examen.getFecha().toString() : null)
                .pesoPorcentual(examen.getPesoPorcentual())
                .notaMaxima(examen.getNotaMaxima())
                .orden(examen.getOrden())
                .totalAlumnos(totalAlumnos)
                .aprobados(0)
                .desaprobados(0)
                .pendientes(totalAlumnos)
                .promedio(null)
                .build();

        return ResponseEntity.ok(dto);
    }

    // ================= LISTAR NOTAS DE UN EXAMEN ==================

    @GetMapping("/examenes/{examenId}/notas")
    @PreAuthorize("hasRole('PROFESOR')")
    public ResponseEntity<NotasExamenDocenteDTO> listarNotasExamen(
            @PathVariable Long examenId) {

        Usuario profesor = getProfesorActual();

        Examen examen = examenRepository.findById(examenId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Examen no encontrado"));

        Seccion seccion = examen.getSeccion();
        validarSeccionDelProfesor(seccion, profesor);

        // Alumnos activos de la sección
        List<Matricula> matriculasActivas = seccion.getMatriculas().stream()
                .filter(m -> m.getEstado() == EstadoMatricula.ACTIVA)
                .toList();

        List<NotaExamen> notas = notaExamenRepository.findByExamen_Id(examenId);

        // Mapa alumnoId -> NotaExamen
        var notasPorAlumno = notas.stream()
                .collect(Collectors.toMap(n -> n.getAlumno().getId(), n -> n));

        int aprobados = 0;
        int desaprobados = 0;
        int pendientes = 0;

        List<NotaExamenFilaDocenteDTO> filas = new java.util.ArrayList<>();

        for (Matricula m : matriculasActivas) {
            Usuario alumno = m.getAlumno();
            NotaExamen notaExamen = notasPorAlumno.get(alumno.getId());

            Double nota = (notaExamen != null) ? notaExamen.getNota() : null;

            String estadoStr;
            if (nota == null) {
                estadoStr = "PENDIENTE";
                pendientes++;
            } else if (nota >= NOTA_MIN_APROBATORIA) {
                estadoStr = "APROBADO";
                aprobados++;
            } else {
                estadoStr = "DESAPROBADO";
                desaprobados++;
            }

            String fechaAct = (notaExamen != null && notaExamen.getFechaActualizacion() != null)
                    ? notaExamen.getFechaActualizacion().toString()
                    : null;

            NotaExamenFilaDocenteDTO fila = NotaExamenFilaDocenteDTO.builder()
                    .alumnoId(alumno.getId())
                    .alumnoNombre(alumno.getNombre())
                    .nota(nota)
                    .estado(estadoStr)
                    .fechaActualizacion(fechaAct)
                    .build();

            filas.add(fila);
        }

        NotasExamenDocenteDTO respuesta = NotasExamenDocenteDTO.builder()
                .examenId(examen.getId())
                .tituloExamen(examen.getTitulo())
                .notaMaxima(examen.getNotaMaxima())
                .pesoPorcentual(examen.getPesoPorcentual())
                .notaMinimaAprobatoria(NOTA_MIN_APROBATORIA)
                .totalAlumnos(matriculasActivas.size())
                .aprobados(aprobados)
                .desaprobados(desaprobados)
                .pendientes(pendientes)
                .alumnos(filas)
                .build();

        return ResponseEntity.ok(respuesta);
    }

    // ================= ACTUALIZAR NOTAS DE UN EXAMEN (EN BLOQUE) ==================

    @PutMapping("/examenes/{examenId}/notas")
    @PreAuthorize("hasRole('PROFESOR')")
    public ResponseEntity<NotasExamenDocenteDTO> actualizarNotasExamen(
            @PathVariable Long examenId,
            @RequestBody List<NotaExamenActualizarDTO> notasRequest) {

        Usuario profesor = getProfesorActual();

        Examen examen = examenRepository.findById(examenId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Examen no encontrado"));

        Seccion seccion = examen.getSeccion();
        validarSeccionDelProfesor(seccion, profesor);

        Double notaMaxima = examen.getNotaMaxima() != null ? examen.getNotaMaxima() : 20.0;

        for (NotaExamenActualizarDTO dto : notasRequest) {
            if (dto.getAlumnoId() == null) continue;

            Double nota = dto.getNota();
            if (nota != null && (nota < 0 || nota > notaMaxima)) {
                throw new IllegalArgumentException(
                        "La nota para el alumno " + dto.getAlumnoId() + " debe estar entre 0 y " + notaMaxima);
            }

            Usuario alumno = seccion.getMatriculas().stream()
                    .map(Matricula::getAlumno)
                    .filter(a -> a.getId().equals(dto.getAlumnoId()))
                    .findFirst()
                    .orElseThrow(() -> new RecursoNoEncontradoException("Alumno no pertenece a la sección"));

            Optional<NotaExamen> optNota = notaExamenRepository
                    .findByExamen_IdAndAlumno_Id(examenId, alumno.getId());

            NotaExamen notaExamen = optNota.orElseGet(() -> {
                NotaExamen ne = new NotaExamen();
                ne.setExamen(examen);
                ne.setAlumno(alumno);
                return ne;
            });

            notaExamen.setNota(nota);

            if (nota == null) {
                notaExamen.setEstado(EstadoNotaExamen.PENDIENTE);
            } else if (nota >= NOTA_MIN_APROBATORIA) {
                notaExamen.setEstado(EstadoNotaExamen.APROBADO);
            } else {
                notaExamen.setEstado(EstadoNotaExamen.DESAPROBADO);
            }

            notaExamenRepository.save(notaExamen);
        }

        // Devolvemos el estado actualizado
        return listarNotasExamen(examenId);
    }
}
