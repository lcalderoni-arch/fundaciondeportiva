// src/main/java/com/proyecto/fundaciondeportiva/controller/MonitorAsistenciaController.java
package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.response.MonitorAsistenciaSesionDTO;
import com.proyecto.fundaciondeportiva.model.entity.Asistencia;
import com.proyecto.fundaciondeportiva.model.entity.Matricula;
import com.proyecto.fundaciondeportiva.model.entity.Sesion;
import com.proyecto.fundaciondeportiva.model.entity.Seccion;
import com.proyecto.fundaciondeportiva.model.enums.EstadoMatricula;
import com.proyecto.fundaciondeportiva.model.enums.Turno;
import com.proyecto.fundaciondeportiva.repository.AsistenciaRepository;
import com.proyecto.fundaciondeportiva.repository.MatriculaRepository;
import com.proyecto.fundaciondeportiva.repository.SesionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/monitor")
public class MonitorAsistenciaController {

    @Autowired
    private SesionRepository sesionRepository;

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private MatriculaRepository matriculaRepository;

    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private LocalTime obtenerHoraInicioBase(Seccion seccion) {
        if (seccion == null || seccion.getTurno() == null) return null;

        Turno turno = seccion.getTurno(); // ← ahora es enum, no String

        switch (turno) {
            case MAÑANA:
                return LocalTime.of(8, 30);
            case TARDE:
                return LocalTime.of(14, 0);
            case NOCHE:
                return LocalTime.of(18, 30);
            default:
                return null;
        }
    }

    // Solo admin / coordinación, tú decides los roles
    @GetMapping("/asistencias/hoy")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COORDINADOR','PROFESOR')")
    public ResponseEntity<List<MonitorAsistenciaSesionDTO>> monitorAsistenciasHoy() {

        // Zona horaria Perú
        ZoneId zone = ZoneId.of("America/Lima");
        LocalDate hoy = LocalDate.now(zone);
        LocalTime ahoraHora = LocalTime.now(zone);

        List<Sesion> sesionesHoy = sesionRepository.findByFecha(hoy);

        List<MonitorAsistenciaSesionDTO> resultado = sesionesHoy.stream()
                .map(sesion -> mapearSesion(sesion, ahoraHora))
                .collect(Collectors.toList());

        return ResponseEntity.ok(resultado);
    }

    private MonitorAsistenciaSesionDTO mapearSesion(Sesion sesion, LocalTime ahora) {
        Seccion seccion = sesion.getSeccion();

        // 1) alumnos activos de la sección
        List<Matricula> matriculasActivas =
                matriculaRepository.findBySeccionIdAndEstado(seccion.getId(), EstadoMatricula.ACTIVA);

        int totalAlumnos = matriculasActivas.size();

        // 2) asistencias ya registradas para esa sesión
        int conAsistencia = asistenciaRepository
                .countBySesionIdAndEstadoIsNotNull(sesion.getId());

        int sinAsistencia = Math.max(totalAlumnos - conAsistencia, 0);

        // 3) decidir horaInicio "real"
        LocalTime horaInicioReal = sesion.getHoraInicio();
        if (horaInicioReal == null) {
            // si la sesión no tiene hora_inicio en BD, usamos el turno como fallback
            horaInicioReal = obtenerHoraInicioBase(seccion);
        }

        String horaInicioStr = horaInicioReal != null
                ? horaInicioReal.format(HORA_FORMATTER)
                : null;

        System.out.println("DEBUG MONITOR -> Sesión " + sesion.getId()
                + " | totalAlumnos=" + totalAlumnos
                + " | conAsistencia=" + conAsistencia
                + " | sinAsistencia=" + sinAsistencia
                + " | horaInicio=" + horaInicioStr);

        // 👉 aquí ahora sí le pasas un LocalTime
        String estadoSemaforo = calcularEstadoSemaforo(horaInicioReal, ahora, sinAsistencia);

        return MonitorAsistenciaSesionDTO.builder()
                .sesionId(sesion.getId())
                .seccionId(seccion.getId())
                .nombreSeccion(seccion.getNombre())        // NUEVO, lo mantienes
                .curso(seccion.getCurso().getTitulo())
                .gradoSeccion(seccion.getGradoSeccion())
                .nivelSeccion(seccion.getNivelSeccion().name())
                .horaInicio(horaInicioStr)
                .horaFin(sesion.getHoraFin() != null ? sesion.getHoraFin().format(HORA_FORMATTER) : null)
                .totalAlumnos(totalAlumnos)
                .conAsistencia(conAsistencia)
                .sinAsistencia(sinAsistencia)
                .estadoSemaforo(estadoSemaforo)
                .build();
    }

    private String calcularEstadoSemaforo(LocalTime inicio, LocalTime ahora, int sinAsistencia) {
        if (inicio == null) {
            // ni la sesión tiene hora, ni pudimos deducirla por turno
            return "SIN_HORARIO";
        }

        // tolerancia de 10 minutos
        LocalTime tiempoTolerancia = inicio.plusMinutes(10);

        if (ahora.isBefore(inicio)) {
            return "PROXIMA"; // todavía no empieza
        }

        if (!ahora.isAfter(tiempoTolerancia)) {
            // entre inicio y tolerancia
            return sinAsistencia > 0 ? "EN_CURSO" : "OK";
        }

        // ya se pasó la tolerancia
        if (sinAsistencia > 0) {
            return "ALERTA"; // aquí es donde al jefe le interesa
        } else {
            return "OK";
        }
    }
}
