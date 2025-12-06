package com.proyecto.fundaciondeportiva.service.impl;

import com.proyecto.fundaciondeportiva.dto.response.EntregaTareaDTO;
import com.proyecto.fundaciondeportiva.exception.ResourceNotFoundException;
import com.proyecto.fundaciondeportiva.model.entity.EntregaTarea;
import com.proyecto.fundaciondeportiva.model.entity.Recurso;
import com.proyecto.fundaciondeportiva.model.entity.Usuario;
import com.proyecto.fundaciondeportiva.model.enums.Rol;
import com.proyecto.fundaciondeportiva.repository.EntregaTareaRepository;
import com.proyecto.fundaciondeportiva.repository.RecursoRepository;
import com.proyecto.fundaciondeportiva.repository.UsuarioRepository;
import com.proyecto.fundaciondeportiva.service.EntregaTareaService;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EntregaTareaServiceImpl implements EntregaTareaService {

    @Autowired
    private EntregaTareaRepository entregaTareaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RecursoRepository recursoRepository;

    @Override
    @Transactional
    public EntregaTareaDTO registrarEntrega(Long recursoId,
                                            String titulo,
                                            String descripcion,
                                            String archivoUrl,
                                            String emailAlumno) {

        Recurso recurso = recursoRepository.findById(recursoId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado"));

        Usuario alumno = usuarioRepository.findByEmail(emailAlumno)
                .orElseThrow(() -> new ResourceNotFoundException("Alumno no encontrado"));

        EntregaTarea entrega = entregaTareaRepository
                .findByRecursoIdAndAlumnoId(recursoId, alumno.getId())
                .orElse(new EntregaTarea());

        entrega.setRecurso(recurso);
        entrega.setAlumno(alumno);
        entrega.setTitulo(titulo);
        entrega.setDescripcion(descripcion);
        entrega.setArchivoUrl(archivoUrl);
        entrega.setFechaEntrega(LocalDateTime.now());

        entrega = entregaTareaRepository.save(entrega);

        return toDTO(entrega);
    }

    @Override
    public List<EntregaTareaDTO> listarEntregasPorRecurso(Long recursoId, String emailProfesor) {

        Usuario profesor = usuarioRepository.findByEmail(emailProfesor)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (profesor.getRol() != Rol.ADMINISTRADOR && profesor.getRol() != Rol.PROFESOR) {
            throw new RuntimeException("No autorizado");
        }

        List<EntregaTarea> entregas = entregaTareaRepository.findByRecursoId(recursoId);

        return entregas.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private EntregaTareaDTO toDTO(EntregaTarea e) {
        EntregaTareaDTO dto = new EntregaTareaDTO();

        dto.setId(e.getId());
        dto.setRecursoId(e.getRecurso().getId());
        dto.setAlumnoId(e.getAlumno().getId());
        dto.setTitulo(e.getTitulo());
        dto.setDescripcion(e.getDescripcion());
        dto.setArchivoUrl(e.getArchivoUrl());
        dto.setFechaEntrega(e.getFechaEntrega());
        dto.setNota(e.getNota());
        dto.setRetroalimentacion(e.getRetroalimentacion());

        return dto;
    }
}
