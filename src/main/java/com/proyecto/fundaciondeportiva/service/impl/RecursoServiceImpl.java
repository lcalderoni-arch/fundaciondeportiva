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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RecursoServiceImpl implements RecursoService {

    @Autowired
    private RecursoRepository recursoRepository;

    @Autowired
    private SesionRepository sesionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // directorio base donde se guardarán los archivos
    @Value("${file.upload-dir}")
    private String uploadDir;

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

        System.out.println("RecursoRequest recibido: "
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

        recurso.setFechaInicioEntrega(request.getFechaInicioEntrega());
        recurso.setFechaFinEntrega(request.getFechaFinEntrega());
        recurso.setPermiteEntregas(
                request.getPermiteEntregas() != null ? request.getPermiteEntregas() : false
        );

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
            MultipartFile archivo,
            String emailProfesor
    ) {

        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Debe adjuntar un archivo.");
        }

        Sesion sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión no encontrada"));

        Usuario usuario = usuarioRepository.findByEmail(emailProfesor)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        validarPermisos(usuario, sesion);

        MomentoSesion momento = MomentoSesion.valueOf(momentoStr);    // ANTES / DURANTE / DESPUES
        TipoRecurso tipo = TipoRecurso.valueOf(tipoRecursoStr);       // PDF / DOCUMENTO / ARCHIVO / IMAGEN, etc.

        try {
            // 1. Generar nombre único
            String originalFilename = archivo.getOriginalFilename();
            String extension = "";

            if (StringUtils.hasText(originalFilename) && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String nuevoNombre = UUID.randomUUID() + extension;

            // 2. Crear carpeta uploads/recursos dentro de uploadDir si no existe
            Path carpetaRecursos = Paths.get(uploadDir, "recursos");
            Files.createDirectories(carpetaRecursos);

            // 3. Guardar archivo físico
            Path destino = carpetaRecursos.resolve(nuevoNombre);
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            // 4. URL relativa que el frontend usará: /uploads/recursos/archivo.ext
            String archivoUrl = "/uploads/recursos/" + nuevoNombre;

            // 5. Crear entidad Recurso
            Recurso recurso = new Recurso();
            recurso.setSesion(sesion);
            recurso.setTitulo(titulo);
            recurso.setDescripcion(descripcion);
            recurso.setMomento(momento);
            recurso.setTipo(tipo);
            recurso.setArchivoUrl(archivoUrl);
            recurso.setLinkVideo(null);  // no aplica para archivos

            recurso = recursoRepository.save(recurso);

            return toDTO(recurso);

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo en el servidor", e);
        }
    }

    // NUEVO: actualizar recurso
    @Override
    @Transactional
    public RecursoDTO actualizarRecurso(Long id, RecursoRequest request, String emailProfesor) {

        Recurso recurso = recursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado"));

        // Si cambias de sesión, hay que validar permisos sobre la nueva sesión
        Sesion sesion = recurso.getSesion();
        if (request.getSesionId() != null &&
                (sesion == null || !sesion.getId().equals(request.getSesionId()))) {

            sesion = sesionRepository.findById(request.getSesionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sesión no encontrada"));
            recurso.setSesion(sesion);
        }

        Usuario usuario = usuarioRepository.findByEmail(emailProfesor)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        validarPermisos(usuario, sesion);

        // Actualizar campos editables
        recurso.setTitulo(request.getTitulo());
        recurso.setDescripcion(request.getDescripcion());
        if (request.getMomento() != null) {
            recurso.setMomento(request.getMomento());
        }
        if (request.getTipo() != null) {
            recurso.setTipo(request.getTipo());
        }
        recurso.setLinkVideo(request.getLinkVideo());
        recurso.setArchivoUrl(request.getArchivoUrl());

        recurso = recursoRepository.save(recurso);
        return toDTO(recurso);
    }

    // NUEVO: eliminar recurso
    @Override
    @Transactional
    public void eliminarRecurso(Long id, String emailProfesor) {

        Recurso recurso = recursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado"));

        Sesion sesion = recurso.getSesion();

        Usuario usuario = usuarioRepository.findByEmail(emailProfesor)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        validarPermisos(usuario, sesion);

        recursoRepository.delete(recurso);
    }

    // --- helpers privados ---

    private void validarPermisos(Usuario usuario, Sesion sesion) {
        if (usuario.getRol() == Rol.ADMINISTRADOR) {
            return;
        }
        if (usuario.getRol() == Rol.PROFESOR) {
            if (sesion.getSeccion().getProfesor() == null ||
                    !sesion.getSeccion().getProfesor().getId().equals(usuario.getId())) {
                System.out.println("Permiso denegado: usuario " + usuario.getEmail()
                        + " intenta crear/editar/eliminar recurso en sección " + sesion.getSeccion().getId());
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

        // NUEVO – solo para tareas
        dto.setFechaInicioEntrega(r.getFechaInicioEntrega());
        dto.setFechaFinEntrega(r.getFechaFinEntrega());
        dto.setPermiteEntregas(r.getPermiteEntregas());

        return dto;
    }
}
