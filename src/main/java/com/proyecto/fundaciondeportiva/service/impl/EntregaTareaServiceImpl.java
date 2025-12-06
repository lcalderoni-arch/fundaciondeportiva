package com.proyecto.fundaciondeportiva.service.impl;

import com.proyecto.fundaciondeportiva.dto.response.EntregaTareaDTO;
import com.proyecto.fundaciondeportiva.exception.ResourceNotFoundException;
import com.proyecto.fundaciondeportiva.model.entity.EntregaTarea;
import com.proyecto.fundaciondeportiva.model.entity.Recurso;
import com.proyecto.fundaciondeportiva.model.entity.Usuario;
import com.proyecto.fundaciondeportiva.model.enums.Rol;
import com.proyecto.fundaciondeportiva.model.enums.TipoRecurso;
import com.proyecto.fundaciondeportiva.repository.EntregaTareaRepository;
import com.proyecto.fundaciondeportiva.repository.RecursoRepository;
import com.proyecto.fundaciondeportiva.repository.UsuarioRepository;
import com.proyecto.fundaciondeportiva.service.EntregaTareaService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EntregaTareaServiceImpl implements EntregaTareaService {

    @Autowired
    private EntregaTareaRepository entregaTareaRepository;

    @Autowired
    private RecursoRepository recursoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public EntregaTareaDTO registrarEntrega(Long recursoId,
                                            String titulo,
                                            String descripcion,
                                            String archivoUrl,
                                            String emailAlumno) {

        // 1. Buscar recurso
        Recurso recurso = recursoRepository.findById(recursoId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado"));

        // Debe ser una TAREA
        if (recurso.getTipo() != TipoRecurso.TAREA) {
            throw new RuntimeException("El recurso no es de tipo TAREA.");
        }

        // Validar ventana de entrega (si la configuraste en Recurso)
        LocalDateTime ahora = LocalDateTime.now();
        if (recurso.getFechaInicioEntrega() != null &&
                ahora.isBefore(recurso.getFechaInicioEntrega())) {
            throw new RuntimeException("La entrega aún no está habilitada.");
        }
        if (recurso.getFechaFinEntrega() != null &&
                ahora.isAfter(recurso.getFechaFinEntrega())) {
            throw new RuntimeException("La fecha límite de entrega ha vencido.");
        }

        if (recurso.getPermiteEntregas() == null || !recurso.getPermiteEntregas()) {
            throw new RuntimeException("Las entregas para esta tarea están deshabilitadas.");
        }

        // 2. Buscar alumno
        Usuario alumno = usuarioRepository.findByEmail(emailAlumno)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (alumno.getRol() != Rol.ALUMNO) {
            throw new RuntimeException("Solo los alumnos pueden registrar entregas.");
        }

        // 3. Ver si ya existe entrega para ese recurso y alumno
        Optional<EntregaTarea> optExistente =
                entregaTareaRepository.findByRecursoIdAndAlumnoId(recursoId, alumno.getId());

        EntregaTarea entrega;
        if (optExistente.isPresent()) {
            // Si quieres permitir reenvío, se actualiza la entrega existente
            entrega = optExistente.get();
        } else {
            entrega = new EntregaTarea();
            entrega.setRecurso(recurso);
            entrega.setAlumno(alumno);
        }

        entrega.setTitulo(titulo);
        entrega.setDescripcion(descripcion);
        entrega.setArchivoUrl(archivoUrl);
        entrega.setFechaEntrega(LocalDateTime.now());

        // Nota y retroalimentación se dejan como están (null) al registrar
        entrega = entregaTareaRepository.save(entrega);

        return toDTO(entrega);
    }

    @Override
    @Transactional
    public List<EntregaTareaDTO> listarEntregasPorRecurso(Long recursoId,
                                                          String emailProfesor) {

        // 1. Buscar recurso
        Recurso recurso = recursoRepository.findById(recursoId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado"));

        // 2. Verificar permisos del usuario que consulta
        Usuario usuario = usuarioRepository.findByEmail(emailProfesor)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (usuario.getRol() == Rol.ADMINISTRADOR) {
            // ok
        } else if (usuario.getRol() == Rol.PROFESOR) {
            // el profesor debe ser el de la sección donde está la tarea
            if (recurso.getSesion() == null ||
                    recurso.getSesion().getSeccion() == null ||
                    recurso.getSesion().getSeccion().getProfesor() == null ||
                    !recurso.getSesion().getSeccion().getProfesor().getId().equals(usuario.getId())) {
                throw new RuntimeException("No tienes permiso para ver estas entregas.");
            }
        } else {
            throw new RuntimeException("No tienes permiso para ver estas entregas.");
        }

        // 3. Obtener entregas
        List<EntregaTarea> entregas = entregaTareaRepository.findByRecursoId(recursoId);

        return entregas.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // --------- helper para mapear a DTO ------------
    private EntregaTareaDTO toDTO(EntregaTarea e) {
        EntregaTareaDTO dto = new EntregaTareaDTO();
        dto.setId(e.getId());
        dto.setRecursoId(e.getRecurso().getId());
        dto.setAlumnoId(e.getAlumno().getId());

        // Si tu entidad Usuario tiene nombres/apellidos podrías armar aquí el nombre,
        // pero para evitar errores de compilación lo dejamos null:
        // dto.setAlumnoNombre(e.getAlumno().getNombres() + " " + e.getAlumno().getApellidos());
        dto.setAlumnoNombre(null);

        dto.setTitulo(e.getTitulo());
        dto.setDescripcion(e.getDescripcion());
        dto.setArchivoUrl(e.getArchivoUrl());
        dto.setFechaEntrega(e.getFechaEntrega());
        dto.setNota(e.getNota());
        dto.setRetroalimentacion(e.getRetroalimentacion());
        return dto;
    }
}
