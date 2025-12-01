package com.proyecto.fundaciondeportiva.dto.response;

import com.proyecto.fundaciondeportiva.model.entity.Seccion;
import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
import com.proyecto.fundaciondeportiva.model.enums.Turno;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeccionResponseDTO {

    private Long id;
    private String codigo;
    private String nombre;
    private NivelAcademico nivelSeccion;
    private String gradoSeccion;
    private Turno turno;
    private String aula;
    private Integer capacidad;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    // ⭐ NUEVO CAMPO
    private Integer numeroSemanas;

    private Boolean activa;
    private LocalDateTime fechaCreacion;

    // Información del curso
    private Long cursoId;
    private String codigoCurso;
    private String tituloCurso;
    private NivelAcademico nivelCurso;

    // Información del profesor
    private Long profesorId;
    private String nombreProfesor;
    private String dniProfesor;

    // Estadísticas
    private Integer estudiantesMatriculados;
    private Integer cuposDisponibles;
    private Boolean tieneCupo;
    private Boolean enPeriodoActivo;

    // ⭐ NUEVAS ESTADÍSTICAS DE SEMANAS
    private Integer semanaActual; // Número de la semana actual (0 si no está en periodo)
    private Integer totalSemanas; // Total de semanas generadas

    public static SeccionResponseDTO deEntidad(Seccion seccion) {
        if (seccion == null) {
            return null;
        }

        // --- Estadísticas de alumnos / cupos ---
        Integer numEst = seccion.getNumeroEstudiantesMatriculados();
        int estudiantesMatriculados = (numEst != null) ? numEst : 0;

        Integer cap = seccion.getCapacidad();
        int capacidad = (cap != null) ? cap : 0;

        int cuposDisponibles = Math.max(capacidad - estudiantesMatriculados, 0);

        // --- Curso (puede ser null en datos viejos / inconsistentes) ---
        Long cursoId = null;
        String codigoCurso = null;
        String tituloCurso = null;
        NivelAcademico nivelCurso = null;

        if (seccion.getCurso() != null) {
            cursoId = seccion.getCurso().getId();
            codigoCurso = seccion.getCurso().getCodigo();
            tituloCurso = seccion.getCurso().getTitulo();
            nivelCurso = seccion.getCurso().getNivelDestino();
        }

        // --- Profesor (también puede ser null) ---
        Long profesorId = null;
        String nombreProfesor = null;
        String dniProfesor = null;

        if (seccion.getProfesor() != null) {
            profesorId = seccion.getProfesor().getId();
            nombreProfesor = seccion.getProfesor().getNombre();

            if (seccion.getProfesor().getPerfilProfesor() != null) {
                dniProfesor = seccion.getProfesor().getPerfilProfesor().getDni();
            } else {
                dniProfesor = "N/A";
            }
        }

        // --- Semanas ---
        Integer numeroSemanas = seccion.getNumeroSemanas(); // puede ser null
        int totalSemanas = 0;
        if (seccion.getSemanas() != null) {
            totalSemanas = seccion.getSemanas().size();
        }

        Integer semanaActual = null;
        try {
            semanaActual = seccion.getNumeroSemanaActual();
        } catch (Exception e) {
            // por si internamente asume algo que aún no existe
            semanaActual = 0;
        }

        // --- Estado / flags ---
        Boolean activa = seccion.getActiva();
        Boolean activaSafe = (activa != null) ? activa : Boolean.TRUE;

        Boolean tieneCupo = null;
        Boolean enPeriodoActivo = null;

        try {
            tieneCupo = seccion.tieneCupoDisponible();
        } catch (Exception e) {
            tieneCupo = capacidad > estudiantesMatriculados;
        }

        try {
            enPeriodoActivo = seccion.estaEnPeriodoActivo();
        } catch (Exception e) {
            enPeriodoActivo = false;
        }

        return SeccionResponseDTO.builder()
                .id(seccion.getId())
                .codigo(seccion.getCodigo())
                .nombre(seccion.getNombre())
                .nivelSeccion(seccion.getNivelSeccion())
                .gradoSeccion(seccion.getGradoSeccion())
                .turno(seccion.getTurno())
                .aula(seccion.getAula())
                .capacidad(capacidad)
                .fechaInicio(seccion.getFechaInicio())
                .fechaFin(seccion.getFechaFin())
                .numeroSemanas(numeroSemanas)
                .activa(activaSafe)
                .fechaCreacion(seccion.getFechaCreacion())

                // Curso
                .cursoId(cursoId)
                .codigoCurso(codigoCurso)
                .tituloCurso(tituloCurso)
                .nivelCurso(nivelCurso)

                // Profesor
                .profesorId(profesorId)
                .nombreProfesor(nombreProfesor)
                .dniProfesor(dniProfesor)

                // Estadísticas
                .estudiantesMatriculados(estudiantesMatriculados)
                .cuposDisponibles(cuposDisponibles)
                .tieneCupo(tieneCupo)
                .enPeriodoActivo(enPeriodoActivo)

                // Semanas
                .semanaActual(semanaActual)
                .totalSemanas(totalSemanas)
                .build();
    }
}