package com.proyecto.fundaciondeportiva.service.impl;

import com.proyecto.fundaciondeportiva.model.entity.ConfiguracionMatricula;

import com.proyecto.fundaciondeportiva.dto.request.MatriculaRequestDTO;
import com.proyecto.fundaciondeportiva.dto.response.MatriculaResponseDTO;
import com.proyecto.fundaciondeportiva.exception.RecursoNoEncontradoException;
import com.proyecto.fundaciondeportiva.exception.ValidacionException;
import com.proyecto.fundaciondeportiva.model.entity.Matricula;
import com.proyecto.fundaciondeportiva.model.entity.Seccion;
import com.proyecto.fundaciondeportiva.model.entity.Usuario;
import com.proyecto.fundaciondeportiva.model.enums.EstadoMatricula;
import com.proyecto.fundaciondeportiva.model.enums.Rol;
import com.proyecto.fundaciondeportiva.repository.ConfiguracionMatriculaRepository;
import com.proyecto.fundaciondeportiva.repository.MatriculaRepository;
import com.proyecto.fundaciondeportiva.repository.SeccionRepository;
import com.proyecto.fundaciondeportiva.repository.UsuarioRepository;
import com.proyecto.fundaciondeportiva.service.ServicioMatricula;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioMatriculaImpl implements ServicioMatricula {

    private static final Logger logger = LoggerFactory.getLogger(ServicioMatriculaImpl.class);

    @Autowired
    private MatriculaRepository matriculaRepository;

    @Autowired
    private SeccionRepository seccionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ConfiguracionMatriculaRepository configuracionMatriculaRepository;

    // --- OPERACIONES DE ALUMNO ---

    @Override
    @Transactional
    public MatriculaResponseDTO matricularseEnSeccion(Long alumnoId, MatriculaRequestDTO request) {
        Usuario alumno = usuarioRepository.findById(alumnoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alumno no encontrado con ID: " + alumnoId));

        if (alumno.getRol() != Rol.ALUMNO) throw new ValidacionException("El usuario no es un alumno");
        if (alumno.getPerfilAlumno() == null) throw new ValidacionException("El alumno no tiene perfil asociado");
        if (Boolean.FALSE.equals(alumno.getHabilitadoMatricula())) throw new ValidacionException("El alumno no tiene habilitada la matrícula");

        Seccion seccion = seccionRepository.findById(request.getSeccionId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Sección no encontrada con ID: " + request.getSeccionId()));

        if (!seccion.getActiva()) throw new ValidacionException("La sección no está activa.");
        if (seccion.getFechaFin().isBefore(LocalDate.now())) throw new ValidacionException("La sección ya finalizó.");

        // Ciclo actual
        String cicloActual = configuracionMatriculaRepository.findFirstByOrderByIdAsc()
                .map(ConfiguracionMatricula::getCicloActual)
                .orElse("2025-II");

        // Buscar si ya existe matrícula RETIRADA en el mismo ciclo
        Optional<Matricula> existente = matriculaRepository.findByAlumnoIdAndSeccionIdAndCiclo(alumnoId, seccion.getId(), cicloActual);

        if (existente.isPresent()) {
            Matricula matriculaExistente = existente.get();

            if (matriculaExistente.getEstado() == EstadoMatricula.RETIRADA) {
                // Si ya está retirada, reactivar la matrícula
                matriculaExistente.setEstado(EstadoMatricula.ACTIVA);
                matriculaExistente.setFechaRetiro(null);  // Eliminar la fecha de retiro
                matriculaExistente.setObservaciones(request.getObservaciones());

                Matricula guardada = matriculaRepository.save(matriculaExistente);
                return MatriculaResponseDTO.deEntidad(guardada);
            } else {
                throw new ValidacionException("El alumno ya tiene una matrícula activa en esta sección.");
            }
        }

        // Validación de cupo
        long matriculasActivas = matriculaRepository.findBySeccionIdAndEstado(seccion.getId(), EstadoMatricula.ACTIVA).size();
        if (matriculasActivas >= seccion.getCapacidad()) {
            throw new ValidacionException("La sección alcanzó su capacidad máxima.");
        }

        // Validar nivel
        if (!alumno.getPerfilAlumno().getNivel().equals(seccion.getNivelSeccion())) {
            throw new ValidacionException("No puedes matricularte. Tu nivel no coincide con la sección.");
        }

        // Crear nueva matrícula
        Matricula nueva = Matricula.builder()
                .alumno(alumno)
                .seccion(seccion)
                .ciclo(cicloActual)
                .estado(EstadoMatricula.ACTIVA)
                .observaciones(request.getObservaciones())
                .build();

        Matricula guardada = matriculaRepository.save(nueva);
        return MatriculaResponseDTO.deEntidad(guardada);
    }

    @Override
    @Transactional
    public MatriculaResponseDTO retirarseDeSeccion(Long alumnoId, Long seccionId) {
        logger.info("Alumno ID {} solicita retirarse de sección ID {}", alumnoId, seccionId);

        try {
            // Buscar la matrícula
            String cicloActual = configuracionMatriculaRepository.findFirstByOrderByIdAsc()
                    .map(ConfiguracionMatricula::getCicloActual)
                    .orElse("2025-II");

            Matricula matricula = matriculaRepository
                    .findByAlumnoIdAndSeccionIdAndCicloAndEstado(alumnoId, seccionId, cicloActual, EstadoMatricula.ACTIVA)
                    .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró una matrícula activa en esta sección (ciclo " + cicloActual + ")."));


            // Validar que la matrícula esté activa
            if (matricula.getEstado() != EstadoMatricula.ACTIVA) {
                throw new ValidacionException("Esta matrícula ya no está activa");
            }

            // Validar que la sección no haya finalizado
            if (matricula.getSeccion().getFechaFin().isBefore(LocalDate.now())) {
                throw new ValidacionException("No puedes retirarte de una sección que ya finalizó");
            }

            // Actualizar el estado
            matricula.setEstado(EstadoMatricula.RETIRADA);
            matricula.setFechaRetiro(LocalDateTime.now());

            Matricula matriculaActualizada = matriculaRepository.save(matricula);
            logger.info("Alumno retirado exitosamente de la sección");

            return MatriculaResponseDTO.deEntidad(matriculaActualizada);

        } catch (RecursoNoEncontradoException | ValidacionException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al retirarse de la sección", e);
            throw new RuntimeException("Error al procesar el retiro: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatriculaResponseDTO> listarMisMatriculas(Long alumnoId) {
        logger.info("Listando todas las matrículas del alumno ID: {}", alumnoId);

        // Validar que el alumno existe
        if (!usuarioRepository.existsById(alumnoId)) {
            throw new RecursoNoEncontradoException("Alumno no encontrado con ID: " + alumnoId);
        }

        return matriculaRepository.findByAlumnoId(alumnoId)
                .stream()
                .map(MatriculaResponseDTO::deEntidad)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatriculaResponseDTO> listarMisMatriculasActivas(Long alumnoId) {
        logger.info("Listando matrículas activas del alumno ID: {}", alumnoId);

        // Validar que el alumno existe
        if (!usuarioRepository.existsById(alumnoId)) {
            throw new RecursoNoEncontradoException("Alumno no encontrado con ID: " + alumnoId);
        }

        return matriculaRepository.findByAlumnoIdAndEstado(alumnoId, EstadoMatricula.ACTIVA)
                .stream()
                .map(MatriculaResponseDTO::deEntidad)
                .collect(Collectors.toList());
    }

    // --- OPERACIONES DE PROFESOR ---

    @Override
    @Transactional(readOnly = true)
    public List<MatriculaResponseDTO> listarAlumnosDeSeccion(Long seccionId) {
        logger.info("Listando todos los alumnos de la sección ID: {}", seccionId);

        // Validar que la sección existe
        if (!seccionRepository.existsById(seccionId)) {
            throw new RecursoNoEncontradoException("Sección no encontrada con ID: " + seccionId);
        }

        return matriculaRepository.findBySeccionId(seccionId)
                .stream()
                .map(MatriculaResponseDTO::deEntidad)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatriculaResponseDTO> listarAlumnosActivosDeSeccion(Long seccionId) {
        logger.info("Listando alumnos activos de la sección ID: {}", seccionId);

        // Validar que la sección existe
        if (!seccionRepository.existsById(seccionId)) {
            throw new RecursoNoEncontradoException("Sección no encontrada con ID: " + seccionId);
        }

        return matriculaRepository.findBySeccionIdAndEstado(seccionId, EstadoMatricula.ACTIVA)
                .stream()
                .map(MatriculaResponseDTO::deEntidad)
                .collect(Collectors.toList());
    }

    // --- OPERACIONES DE ADMINISTRADOR ---

    @Override
    @Transactional(readOnly = true)
    public List<MatriculaResponseDTO> listarTodasLasMatriculas() {
        logger.info("Listando todas las matrículas del sistema");

        return matriculaRepository.findAll()
                .stream()
                .map(MatriculaResponseDTO::deEntidad)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MatriculaResponseDTO obtenerMatriculaPorId(Long id) {
        logger.info("Obteniendo matrícula con ID: {}", id);

        Matricula matricula = matriculaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Matrícula no encontrada con ID: " + id));

        return MatriculaResponseDTO.deEntidad(matricula);
    }

    @Override
    @Transactional
    public MatriculaResponseDTO actualizarEstadoMatricula(Long id, EstadoMatricula nuevoEstado) {
        logger.info("Actualizando estado de matrícula ID {} a {}", id, nuevoEstado);

        try {
            Matricula matricula = matriculaRepository.findById(id)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Matrícula no encontrada con ID: " + id));

            // Si se marca como retirada, registrar la fecha
            if (nuevoEstado == EstadoMatricula.RETIRADA && matricula.getFechaRetiro() == null) {
                matricula.setFechaRetiro(LocalDateTime.now());
            }

            matricula.setEstado(nuevoEstado);
            Matricula matriculaActualizada = matriculaRepository.save(matricula);

            logger.info("Estado de matrícula actualizado exitosamente");
            return MatriculaResponseDTO.deEntidad(matriculaActualizada);

        } catch (RecursoNoEncontradoException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al actualizar estado de matrícula", e);
            throw new RuntimeException("Error al actualizar el estado: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public MatriculaResponseDTO asignarCalificacion(Long id, Double calificacion) {
        logger.info("Asignando calificación {} a matrícula ID {}", calificacion, id);

        try {
            // Validar rango de calificación
            if (calificacion < 0 || calificacion > 20) {
                throw new ValidacionException("La calificación debe estar entre 0 y 20");
            }

            Matricula matricula = matriculaRepository.findById(id)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Matrícula no encontrada con ID: " + id));

            matricula.setCalificacionFinal(calificacion);

            // Si la calificación es aprobatoria y el curso terminó, marcar como completada
            if (calificacion >= 11 && matricula.getSeccion().getFechaFin().isBefore(LocalDate.now())) {
                matricula.setEstado(EstadoMatricula.COMPLETADA);
            }

            Matricula matriculaActualizada = matriculaRepository.save(matricula);

            logger.info("Calificación asignada exitosamente");
            return MatriculaResponseDTO.deEntidad(matriculaActualizada);

        } catch (RecursoNoEncontradoException | ValidacionException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al asignar calificación", e);
            throw new RuntimeException("Error al asignar calificación: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void eliminarMatricula(Long id) {
        logger.info("Eliminando matrícula con ID: {}", id);

        if (!matriculaRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Matrícula no encontrada con ID: " + id);
        }

        try {
            matriculaRepository.deleteById(id);
            logger.info("Matrícula eliminada exitosamente");
        } catch (Exception e) {
            logger.error("Error al eliminar matrícula", e);
            throw new RuntimeException("Error al eliminar la matrícula: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public int resetCicloAcademico() {
        logger.info("Iniciando reinicio de ciclo académico: archivando matrículas activas");

        // 1) ARCHIVAR MATRÍCULAS ACTIVAS
        List<Matricula> activas = matriculaRepository.findByEstado(EstadoMatricula.ACTIVA);

        if (activas.isEmpty()) {
            logger.info("No hay matrículas activas para archivar");
        } else {
            for (Matricula m : activas) {
                m.setEstado(EstadoMatricula.RETIRADA); // o ARCHIVADA si tienes ese estado
                if (m.getFechaRetiro() == null) {
                    m.setFechaRetiro(LocalDateTime.now());
                }
            }
            matriculaRepository.saveAll(activas);
            logger.info("Matrículas archivadas: {}", activas.size());
        }

        // 2) BLOQUEAR MATRÍCULA A NIVEL GLOBAL
        try {
            Optional<ConfiguracionMatricula> optConfig =
                    configuracionMatriculaRepository.findFirstByOrderByIdAsc();

            if (optConfig.isPresent()) {
                ConfiguracionMatricula config = optConfig.get();
                config.setMatriculaHabilitada(false);
                configuracionMatriculaRepository.save(config);
                logger.info("Configuración global de matrícula marcada como BLOQUEADA");
            } else {
                logger.warn("No se encontró configuración de matrícula global para actualizar");
            }
        } catch (Exception e) {
            logger.error("Error actualizando configuración global de matrícula", e);
            // No lanzamos excepción para no romper el cierre; solo lo registramos
        }

        // 3) BLOQUEAR LA MATRÍCULA DE TODOS LOS ALUMNOS
        try {
            List<Usuario> alumnos = usuarioRepository.findAllAlumnos();
            if (!alumnos.isEmpty()) {
                for (Usuario u : alumnos) {
                    u.setHabilitadoMatricula(false);
                }
                usuarioRepository.saveAll(alumnos);
                logger.info("Se ha bloqueado la matrícula de {} alumnos", alumnos.size());
            }
        } catch (Exception e) {
            logger.error("Error al bloquear la matrícula de los alumnos", e);
        }

        // Devolvemos cuántas matrículas se archivaron, como antes
        return activas.size();
    }
}
