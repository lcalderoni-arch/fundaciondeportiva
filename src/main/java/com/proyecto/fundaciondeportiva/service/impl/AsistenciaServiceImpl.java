// src/main/java/com/proyecto/fundaciondeportiva/service/impl/AsistenciaServiceImpl.java
package com.proyecto.fundaciondeportiva.service.impl;

import com.proyecto.fundaciondeportiva.dto.request.RegistrarAsistenciasSesionRequest;
import com.proyecto.fundaciondeportiva.dto.request.RegistroAsistenciaAlumnoRequest;
import com.proyecto.fundaciondeportiva.dto.response.AsistenciaAlumnoSemanaDTO;
import com.proyecto.fundaciondeportiva.dto.response.AsistenciaDetalleAlumnoDTO;
import com.proyecto.fundaciondeportiva.exception.ResourceNotFoundException;
import com.proyecto.fundaciondeportiva.model.entity.Asistencia;
import com.proyecto.fundaciondeportiva.model.entity.Matricula;
import com.proyecto.fundaciondeportiva.model.entity.Sesion;
import com.proyecto.fundaciondeportiva.model.entity.Usuario;
import com.proyecto.fundaciondeportiva.model.enums.EstadoMatricula;
import com.proyecto.fundaciondeportiva.model.enums.Rol;
import com.proyecto.fundaciondeportiva.repository.AsistenciaRepository;
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

    // ========= DOCENTE: VER ASISTENCIAS DE UNA SESIÓN =========
    @Override
    public List<AsistenciaDetalleAlumnoDTO> obtenerAsistenciasPorSesion(Long sesionId) {

        Sesion sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión no encontrada"));

        Long seccionId = sesion.getSeccion().getId();

        System.out.println("=== DEBUG ASISTENCIAS ===");
        System.out.println("Sesión ID: " + sesionId + " -> Sección ID: " + seccionId);

        List<Matricula> matriculas = matriculaRepository
                .findBySeccionIdAndEstado(seccionId, EstadoMatricula.ACTIVA);

        System.out.println("MATRÍCULAS ACTIVAS EN SECCION " + seccionId + ": " + matriculas.size());
        for (Matricula m : matriculas) {
            if (m.getAlumno() != null) {
                System.out.println("  Matricula ID " + m.getId() +
                        " -> alumnoId=" + m.getAlumno().getId() +
                        ", nombre=" + m.getAlumno().getNombre() +
                        ", email=" + m.getAlumno().getEmail());
            } else {
                System.out.println("  Matricula ID " + m.getId() + " -> SIN ALUMNO (null)");
            }
        }
        System.out.println("=========================");

        Map<Long, Matricula> matriculaPorAlumno = matriculas.stream()
                .collect(Collectors.toMap(m -> m.getAlumno().getId(), m -> m));

        // 2) Traer asistencias que ya existan para esa sesión
        List<Asistencia> asistencias = asistenciaRepository.findBySesionId(sesionId);
        Map<Long, Asistencia> asistenciaPorAlumno = asistencias.stream()
                .collect(Collectors.toMap(a -> a.getAlumno().getId(), a -> a));

        // 3) Armar la lista de DTOs (uno por alumno matriculado)
        List<AsistenciaDetalleAlumnoDTO> resultado = new ArrayList<>();

        for (Matricula m : matriculas) {
            Usuario alumno = m.getAlumno();
            Asistencia asistencia = asistenciaPorAlumno.get(alumno.getId());

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
                dto.setEstado(null);          // aún no tomado
                dto.setObservaciones(null);
            }

            resultado.add(dto);
        }

        // Opcional: ordenar por nombre
        resultado.sort(Comparator.comparing(AsistenciaDetalleAlumnoDTO::getNombreAlumno));

        return resultado;
    }

    // ========= DOCENTE: REGISTRAR/ACTUALIZAR ASISTENCIAS =========
    @Override
    @Transactional
    public void registrarAsistenciasSesion(RegistrarAsistenciasSesionRequest request, String emailUsuario) {

        Sesion sesion = sesionRepository.findById(request.getSesionId())
                .orElseThrow(() -> new ResourceNotFoundException("Sesión no encontrada"));

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // ✅ Lógica según rol
        if (usuario.getRol() == Rol.PROFESOR) {
            // Validar que sea el profesor dueño de la sección
            if (sesion.getSeccion().getProfesor() == null ||
                    !sesion.getSeccion().getProfesor().getId().equals(usuario.getId())) {
                throw new RuntimeException("No puedes registrar asistencia en una sección que no es tuya.");
            }

        } else if (usuario.getRol() == Rol.ADMINISTRADOR) {
            // ✅ Admin / coordinador pueden editar cualquier sesión
            // no validamos que sean el profesor

        } else {
            throw new RuntimeException("No tienes permiso para registrar asistencias.");
        }

        // Para cada registro recibido
        for (RegistroAsistenciaAlumnoRequest reg : request.getRegistros()) {

            Usuario alumno = usuarioRepository.findById(reg.getAlumnoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Alumno no encontrado"));

            Optional<Asistencia> opt = asistenciaRepository
                    .findBySesionIdAndAlumnoId(sesion.getId(), alumno.getId());

            Asistencia asistencia = opt.orElseGet(() -> {
                Asistencia nueva = new Asistencia();
                nueva.setSesion(sesion);
                nueva.setAlumno(alumno);
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

        // traemos TODAS las asistencias del alumno en esa sección:
        List<Asistencia> asistencias = asistenciaRepository
                .findByAlumnoIdAndSesion_SeccionId(alumno.getId(), seccionId);

        // Ordenamos por id de sesión (o por fecha si tienes campo fecha en Sesion)
        asistencias.sort(Comparator.comparing(a -> a.getSesion().getId()));

        List<AsistenciaAlumnoSemanaDTO> resultado = new ArrayList<>();

        int contadorSemana = 1;
        for (Asistencia a : asistencias) {
            AsistenciaAlumnoSemanaDTO dto = new AsistenciaAlumnoSemanaDTO();

            // Asignamos números de semana secuenciales: 1, 2, 3, ...
            dto.setSemanaNumero(contadorSemana++);
            dto.setEstado(a.getEstado());

            resultado.add(dto);
        }

        return resultado;
    }
}