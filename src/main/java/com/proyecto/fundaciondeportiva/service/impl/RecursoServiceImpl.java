// src/main/java/com/proyecto/fundaciondeportiva/service/impl/RecursoServiceImpl.java
package com.proyecto.fundaciondeportiva.service.impl;

import com.proyecto.fundaciondeportiva.dto.request.RecursoRequest;
import com.proyecto.fundaciondeportiva.dto.response.RecursoDTO;
import com.proyecto.fundaciondeportiva.exception.ResourceNotFoundException;
import com.proyecto.fundaciondeportiva.model.entity.Recurso;
import com.proyecto.fundaciondeportiva.model.entity.Sesion;
import com.proyecto.fundaciondeportiva.model.entity.Usuario;
import com.proyecto.fundaciondeportiva.model.enums.MomentoSesion;
import com.proyecto.fundaciondeportiva.model.enums.Rol;
import com.proyecto.fundaciondeportiva.model.enums.TipoRecurso;
import com.proyecto.fundaciondeportiva.repository.RecursoRepository;
import com.proyecto.fundaciondeportiva.repository.SesionRepository;
import com.proyecto.fundaciondeportiva.repository.UsuarioRepository;
import com.proyecto.fundaciondeportiva.service.RecursoService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecursoServiceImpl implements RecursoService {

    @Autowired
    private RecursoRepository recursoRepository;

    @Autowired
    private SesionRepository sesionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public List<RecursoDTO> listarPorSesion(Long sesionId) {
        List<Recurso> recursos =
                recursoRepository.findBySesionIdOrderByMomentoAsc(sesionId);

        return recursos.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RecursoDTO crearRecurso(RecursoRequest request, String emailProfesor) {

        System.out.println("📥 RecursoRequest recibido: "
                + "sesionId=" + request.getSesionId()
                + ", titulo=" + request.getTitulo()
                + ", momento=" + request.getMomento()
                + ", tipo=" + request.getTipo()
                + ", linkVideo=" + request.getLinkVideo());

        Sesion sesion = sesionRepository.findById(request.getSesionId())
                .orElseThrow(() -> new ResourceNotFoundException("Sesión no encontrada"));

        Usuario usuario = usuarioRepository.findByEmail(emailProfesor)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        validarPermisos(usuario, sesion);

        Recurso recurso = new Recurso();
        recurso.setSesion(sesion);
        recurso.setTitulo(request.getTitulo());
        recurso.setDescripcion(request.getDescripcion());
        recurso.setMomento(request.getMomento());
        recurso.setTipo(request.getTipo());
        recurso.setArchivoUrl(request.getArchivoUrl());
        recurso.setLinkVideo(request.getLinkVideo());

        recurso = recursoRepository.save(recurso);
        return toDTO(recurso);
    }

    @Override
    @Transactional
    public RecursoDTO crearRecursoArchivo(
            Long sesionId,
            String titulo,
            String descripcion,
            String momentoStr,
            String tipoRecursoStr,
            String archivoUrl,
            String emailProfesor
    ) {
        Sesion sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión no encontrada"));

        Usuario usuario = usuarioRepository.findByEmail(emailProfesor)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        validarPermisos(usuario, sesion);

        MomentoSesion momento = MomentoSesion.valueOf(momentoStr);    // p.ej. EXPLORA
        TipoRecurso tipo = TipoRecurso.valueOf(tipoRecursoStr);       // p.ej. PDF

        Recurso recurso = new Recurso();
        recurso.setSesion(sesion);
        recurso.setTitulo(titulo);
        recurso.setDescripcion(descripcion);
        recurso.setMomento(momento);
        recurso.setTipo(tipo);
        recurso.setArchivoUrl(archivoUrl);
        // linkVideo puede quedar null aquí

        recurso = recursoRepository.save(recurso);
        return toDTO(recurso);
    }

    // --- helpers privados ---

    private void validarPermisos(Usuario usuario, Sesion sesion) {
        if (usuario.getRol() == Rol.ADMINISTRADOR) {
            return;
        }
        if (usuario.getRol() == Rol.PROFESOR) {
            if (sesion.getSeccion().getProfesor() == null ||
                    !sesion.getSeccion().getProfesor().getId().equals(usuario.getId())) {
                System.out.println("❌ Permiso denegado: usuario " + usuario.getEmail()
                        + " intenta crear recurso en sección " + sesion.getSeccion().getId());
                throw new RuntimeException("No puedes registrar recursos en una sección que no es tuya.");
            }
        } else {
            throw new RuntimeException("No tienes permiso para registrar recursos.");
        }
    }

    private RecursoDTO toDTO(Recurso r) {
        RecursoDTO dto = new RecursoDTO();
        dto.setId(r.getId());
        dto.setTitulo(r.getTitulo());
        dto.setDescripcion(r.getDescripcion());
        dto.setMomento(r.getMomento());
        dto.setTipo(r.getTipo());
        dto.setArchivoUrl(r.getArchivoUrl());
        dto.setLinkVideo(r.getLinkVideo());
        dto.setFechaPublicacion(r.getFechaPublicacion());
        dto.setSesionId(r.getSesion().getId());
        return dto;
    }
}
