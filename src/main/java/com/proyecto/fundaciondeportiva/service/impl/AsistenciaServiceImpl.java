package com.proyecto.fundaciondeportiva.service.impl;

import com.proyecto.fundaciondeportiva.dto.request.RegistrarAsistenciasSesionRequest;
import com.proyecto.fundaciondeportiva.dto.request.RegistroAsistenciaAlumnoRequest;
import com.proyecto.fundaciondeportiva.dto.response.AsistenciaAlumnoSemanaDTO;
import com.proyecto.fundaciondeportiva.dto.response.AsistenciaDetalleAlumnoDTO;
import com.proyecto.fundaciondeportiva.exception.ResourceNotFoundException;
import com.proyecto.fundaciondeportiva.model.entity.Asistencia;
import com.proyecto.fundaciondeportiva.model.entity.ConfiguracionMatricula;
import com.proyecto.fundaciondeportiva.model.entity.Matricula;
import com.proyecto.fundaciondeportiva.model.entity.Sesion;
import com.proyecto.fundaciondeportiva.model.entity.Usuario;
import com.proyecto.fundaciondeportiva.model.enums.EstadoMatricula;
import com.proyecto.fundaciondeportiva.model.enums.Rol;
import com.proyecto.fundaciondeportiva.repository.AsistenciaRepository;
import com.proyecto.fundaciondeportiva.repository.ConfiguracionMatriculaRepository;
import com.proyecto.fundaciondeportiva.repository.MatriculaRepository;
import com.proyecto.fundaciondeportiva.repository.SesionRepository;
import com.proyecto.fundaciondeportiva.repository.UsuarioRepository;
import com.proyecto.fundaciondeportiva.service.AsistenciaService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AsistenciaServiceImpl implements AsistenciaService {

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private SesionRepository sesionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MatriculaRepository matriculaRepository;

    @Autowired
    private ConfiguracionMatriculaRepository configuracionMatriculaRepository;

    private String obtenerCicloActual() {
        return configuracionMatriculaRepository.findFirstByOrderByIdAsc()
                .map(ConfiguracionMatricula::getCicloActual)
                .orElse("2025-II");
    }

    // ========= DOCENTE/ADMIN: VER ASISTENCIAS DE UNA SESIÓN =========
    @Override
    public List<AsistenciaDetalleAlumnoDTO> obtenerAsistenciasPorSesion(Long sesionId) {

        Sesion sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión no encontrada"));

        Long seccionId = sesion.getSeccion().getId();
        String cicloActual = obtenerCicloActual();

        // 1) Matrículas activas de la sección (ideal: filtrar por ciclo)
        // Si aún no tienes ciclo en BD, usa findBySeccionIdAndEstado(...)
        List<Matricula> matriculas = matriculaRepository
                .findBySeccionIdAndEstado(seccionId, EstadoMatricula.ACTIVA)
                .stream()
                .filter(m -> cicloActual.equals(m.getCiclo())) // ✅ si ya agregaste ciclo
                .collect(Collectors.toList());

        // 2) Asistencias ya registradas para esa sesión
        List<Asistencia> asistencias = asistenciaRepository.findBySesionId(sesionId);

        // Mapear por MatriculaId (no por alumnoId)
        Map<Long, Asistencia> asistenciaPorMatriculaId = asistencias.stream()
                .collect(Collectors.toMap(a -> a.getMatricula().getId(), a -> a, (a1, a2) -> a1));

        // 3) Armar DTOs
        List<AsistenciaDetalleAlumnoDTO> resultado = new ArrayList<>();

        for (Matricula m : matriculas) {
            Usuario alumno = m.getAlumno();
            Asistencia asistencia = asistenciaPorMatriculaId.get(m.getId());

            AsistenciaDetalleAlumnoDTO dto = new AsistenciaDetalleAlumnoDTO();
            dto.setAlumnoId(alumno.getId());
            dto.setNombreAlumno(alumno.getNombre());

            if (alumno.getPerfilAlumno() != null) {
                dto.setCodigoEstudiante(alumno.getPerfilAlumno().getCodigoEstudiante());
            }

            if (asistencia != null) {
                dto.setEstado(asistencia.getEstado());
                dto.setObservaciones(asistencia.getObservaciones());
            } else {
                dto.setEstado(null);
                dto.setObservaciones(null);
            }

            resultado.add(dto);
        }

        resultado.sort(Comparator.comparing(AsistenciaDetalleAlumnoDTO::getNombreAlumno));
        return resultado;
    }

    // ========= DOCENTE/ADMIN: REGISTRAR/ACTUALIZAR ASISTENCIAS =========
    @Override
    @Transactional
    public void registrarAsistenciasSesion(RegistrarAsistenciasSesionRequest request, String emailUsuario) {

        Sesion sesion = sesionRepository.findById(request.getSesionId())
                .orElseThrow(() -> new ResourceNotFoundException("Sesión no encontrada"));

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Permisos
        if (usuario.getRol() == Rol.PROFESOR) {
            if (sesion.getSeccion().getProfesor() == null ||
                    !sesion.getSeccion().getProfesor().getId().equals(usuario.getId())) {
                throw new RuntimeException("No puedes registrar asistencia en una sección que no es tuya.");
            }
        } else if (usuario.getRol() != Rol.ADMINISTRADOR) {
            throw new RuntimeException("No tienes permiso para registrar asistencias.");
        }

        Long seccionId = sesion.getSeccion().getId();
        String cicloActual = obtenerCicloActual();

        // Por cada alumno recibido
        for (RegistroAsistenciaAlumnoRequest reg : request.getRegistros()) {

            // ✅ 1) Obtener la matrícula ACTIVA del alumno en ESTA sección y ciclo
            Matricula matriculaActiva = matriculaRepository
                    .findByAlumnoIdAndSeccionIdAndCicloAndEstado(reg.getAlumnoId(), seccionId, cicloActual, EstadoMatricula.ACTIVA)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No existe matrícula ACTIVA para el alumno en esta sección (ciclo " + cicloActual + ")."
                    ));

            // ✅ 2) Buscar asistencia por Matricula + Sesion
            Asistencia asistencia = asistenciaRepository
                    .findByMatriculaIdAndSesionId(matriculaActiva.getId(), sesion.getId())
                    .orElseGet(() -> {
                        Asistencia nueva = new Asistencia();
                        nueva.setSesion(sesion);
                        nueva.setMatricula(matriculaActiva);
                        return nueva;
                    });

            asistencia.setEstado(reg.getEstado());
            asistencia.setObservaciones(reg.getObservaciones());

            asistenciaRepository.save(asistencia);
        }
    }

    // ========= ALUMNO: VER MIS ASISTENCIAS EN UNA SECCIÓN =========
    @Override
    public List<AsistenciaAlumnoSemanaDTO> obtenerMisAsistenciasEnSeccion(Long seccionId, String emailAlumno) {

        Usuario alumno = usuarioRepository.findByEmail(emailAlumno)
                .orElseThrow(() -> new ResourceNotFoundException("Alumno no encontrado"));

        String cicloActual = obtenerCicloActual();

        // ✅ Matrícula ACTIVA del alumno en esa sección (del ciclo actual)
        Matricula matriculaActiva = matriculaRepository
                .findByAlumnoIdAndSeccionIdAndCicloAndEstado(alumno.getId(), seccionId, cicloActual, EstadoMatricula.ACTIVA)
                .orElseThrow(() -> new ResourceNotFoundException("No tienes matrícula activa en esta sección (ciclo " + cicloActual + ")."));

        // ✅ Traer asistencias por matrícula (NO por alumno+sección)
        List<Asistencia> asistencias = asistenciaRepository.findByMatriculaId(matriculaActiva.getId());

        // Ordenar por sesión (ideal por fecha/hora, aquí por ID)
        asistencias.sort(Comparator.comparing(a -> a.getSesion().getId()));

        List<AsistenciaAlumnoSemanaDTO> resultado = new ArrayList<>();
        int contadorSemana = 1;

        for (Asistencia a : asistencias) {
            AsistenciaAlumnoSemanaDTO dto = new AsistenciaAlumnoSemanaDTO();
            dto.setSemanaNumero(contadorSemana++);
            dto.setEstado(a.getEstado());
            resultado.add(dto);
        }

        return resultado;
    }
}
