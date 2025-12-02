package com.proyecto.fundaciondeportiva.service;

import com.proyecto.fundaciondeportiva.dto.input.UsuarioInputDTO;
import com.proyecto.fundaciondeportiva.dto.output.UsuarioUpdateDTO;
import com.proyecto.fundaciondeportiva.dto.output.UsuarioOutputDTO;
import com.proyecto.fundaciondeportiva.dto.response.UsuarioResponse;
import com.proyecto.fundaciondeportiva.exception.RecursoNoEncontradoException;
import com.proyecto.fundaciondeportiva.exception.ValidacionException;
import com.proyecto.fundaciondeportiva.model.entity.PerfilAlumno;
import com.proyecto.fundaciondeportiva.model.entity.PerfilProfesor;
import com.proyecto.fundaciondeportiva.model.entity.Usuario;
import com.proyecto.fundaciondeportiva.model.enums.Rol;
import com.proyecto.fundaciondeportiva.repository.PerfilAlumnoRepository;
import com.proyecto.fundaciondeportiva.repository.PerfilProfesorRepository;
import com.proyecto.fundaciondeportiva.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REFACTORIZACIÓN COMPLETA de tu UsuarioService.
 * AHORA usa los nuevos DTOs (UsuarioInputDTO, UsuarioOutputDTO, UsuarioUpdateDTO)
 * y las nuevas entidades (Usuario, PerfilAlumno, PerfilProfesor del diagrama).
 * Sigue implementando UserDetailsService.
 */
@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PerfilAlumnoRepository perfilAlumnoRepository;

    @Autowired
    private PerfilProfesorRepository perfilProfesorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- 1. Lógica de Seguridad (Implementación de UserDetailsService) ---
    @Transactional(readOnly = true)
    public List<Usuario> listarAlumnos() {
        return usuarioRepository.findByRol(Rol.ALUMNO);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));
    }

    // --- 2. Lógica de Negocio (CRUD del Admin) ---

    @Transactional
    public Usuario crearUsuario(UsuarioInputDTO request) {

        // --- 1. Validación de Email (MEJORADA) ---
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            Usuario usuarioExistente = usuarioRepository.findByEmail(request.getEmail()).orElse(null);
            if (usuarioExistente != null) {
                throw new ValidacionException(
                        String.format("Ya existe un usuario con el correo '%s' (Nombre: %s, Rol: %s)",
                                request.getEmail(),
                                usuarioExistente.getNombre(),
                                usuarioExistente.getRol())
                );
            } else {
                throw new ValidacionException("El correo electrónico ya está en uso.");
            }
        }

        // Construir la entidad Usuario base
        Usuario nuevoUsuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(request.getRol())
                // opcional: habilitarMatricula por defecto para alumnos
                .build();

        // --- 2. Lógica de Perfiles (basada en el diagrama PlantUML) ---
        if (request.getRol() == Rol.ALUMNO) {
            if (request.getDniAlumno() == null || request.getDniAlumno().isBlank()) {
                throw new ValidacionException("El DNI es obligatorio para el alumno.");
            }

            if (perfilAlumnoRepository.existsByDni(request.getDniAlumno())) {
                PerfilAlumno perfilExistente = perfilAlumnoRepository.findByDni(request.getDniAlumno()).orElse(null);
                if (perfilExistente != null && perfilExistente.getUsuario() != null) {
                    throw new ValidacionException(
                            String.format("Ya existe un alumno con el DNI '%s' (Nombre: %s, Código: %s)",
                                    request.getDniAlumno(),
                                    perfilExistente.getUsuario().getNombre(),
                                    perfilExistente.getCodigoEstudiante())
                    );
                } else {
                    throw new ValidacionException("El DNI del alumno ya está registrado.");
                }
            }

            if (request.getNivel() == null) {
                throw new ValidacionException("El Nivel Académico es obligatorio para el alumno.");
            }
            if (request.getGrado() == null || request.getGrado().isBlank()) {
                throw new ValidacionException("El Grado es obligatorio para el alumno.");
            }

            if (request.getTelefonoEmergencia() == null || request.getTelefonoEmergencia().isBlank()) {
                throw new ValidacionException("El teléfono de emergencia es obligatorio para el alumno.");
            }

            String codigoEstudiante = request.getCodigoEstudiante();
            if (codigoEstudiante == null || codigoEstudiante.isBlank()) {
                codigoEstudiante = generarCodigoEstudianteUnico();
            } else if (perfilAlumnoRepository.existsByCodigoEstudiante(codigoEstudiante)) {
                throw new ValidacionException(
                        String.format("El código de estudiante '%s' ya está en uso.", codigoEstudiante)
                );
            }

            PerfilAlumno perfil = PerfilAlumno.builder()
                    .dni(request.getDniAlumno())
                    .nivel(request.getNivel())
                    .grado(request.getGrado())
                    .codigoEstudiante(codigoEstudiante)
                    .telefonoEmergencia(request.getTelefonoEmergencia())
                    .usuario(nuevoUsuario)
                    .build();
            nuevoUsuario.setPerfilAlumno(perfil);

        } else if (request.getRol() == Rol.PROFESOR) {
            if (request.getDniProfesor() == null || request.getDniProfesor().isBlank()) {
                throw new ValidacionException("El DNI es obligatorio para el profesor.");
            }
            if (perfilProfesorRepository.existsByDni(request.getDniProfesor())) {
                PerfilProfesor perfilExistente = perfilProfesorRepository.findByDni(request.getDniProfesor()).orElse(null);
                if (perfilExistente != null && perfilExistente.getUsuario() != null) {
                    throw new ValidacionException(
                            String.format("Ya existe un profesor con el DNI '%s' (Nombre: %s)",
                                    request.getDniProfesor(),
                                    perfilExistente.getUsuario().getNombre())
                    );
                } else {
                    throw new ValidacionException("El DNI del profesor ya está registrado.");
                }
            }

            PerfilProfesor perfil = PerfilProfesor.builder()
                    .dni(request.getDniProfesor())
                    .telefono(request.getTelefono())
                    .experiencia(request.getExperiencia())
                    .gradoAcademico(request.getGradoAcademico())
                    .usuario(nuevoUsuario)
                    .build();
            nuevoUsuario.setPerfilProfesor(perfil);

        } else if (request.getRol() == Rol.ADMINISTRADOR) {
            // El Admin no tiene perfil.
        }

        return usuarioRepository.save(nuevoUsuario);
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Usuario obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id: " + id));
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Transactional
    public Usuario actualizarUsuario(Long id, UsuarioUpdateDTO request) {

        Usuario usuario = obtenerUsuarioPorId(id);

        if (StringUtils.hasText(request.getNombre())) {
            usuario.setNombre(request.getNombre());
        }
        if (StringUtils.hasText(request.getPassword())) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(request.getEmail())) {
                throw new ValidacionException("El nuevo correo electrónico ya está en uso.");
            }
            usuario.setEmail(request.getEmail());
        }

        if (usuario.getRol() == Rol.ALUMNO) {
            PerfilAlumno perfil = Optional.ofNullable(usuario.getPerfilAlumno()).orElse(new PerfilAlumno());
            perfil.setUsuario(usuario);
            usuario.setPerfilAlumno(perfil);

            if (StringUtils.hasText(request.getDniAlumno()) && !request.getDniAlumno().equals(perfil.getDni())) {
                if (perfilAlumnoRepository.existsByDni(request.getDniAlumno())) {
                    throw new ValidacionException("El nuevo DNI de alumno ya está en uso.");
                }
                perfil.setDni(request.getDniAlumno());
            }
            if (request.getNivel() != null) {
                perfil.setNivel(request.getNivel());
            }
            if (StringUtils.hasText(request.getGrado())) {
                perfil.setGrado(request.getGrado());
            }
            if (StringUtils.hasText(request.getCodigoEstudiante())) {
                perfil.setCodigoEstudiante(request.getCodigoEstudiante());
            }
        }
        else if (usuario.getRol() == Rol.PROFESOR) {
            PerfilProfesor perfil = Optional.ofNullable(usuario.getPerfilProfesor()).orElse(new PerfilProfesor());
            perfil.setUsuario(usuario);
            usuario.setPerfilProfesor(perfil);

            if (StringUtils.hasText(request.getDniProfesor()) && !request.getDniProfesor().equals(perfil.getDni())) {
                if (perfilProfesorRepository.existsByDni(request.getDniProfesor())) {
                    throw new ValidacionException("El nuevo DNI de profesor ya está en uso.");
                }
                perfil.setDni(request.getDniProfesor());
            }
            if (StringUtils.hasText(request.getTelefono())) {
                perfil.setTelefono(request.getTelefono());
            }
            if (StringUtils.hasText(request.getExperiencia())) {
                perfil.setExperiencia(request.getExperiencia());
            }
            if (StringUtils.hasText(request.getGradoAcademico())) {
                perfil.setGradoAcademico(request.getGradoAcademico());
            }
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Usuario no encontrado con id: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    private String generarCodigoEstudianteUnico() {
        String codigoGenerado;
        boolean codigoExiste;
        do {
            codigoGenerado = "E-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            codigoExiste = perfilAlumnoRepository.existsByCodigoEstudiante(codigoGenerado);
        } while (codigoExiste);
        return codigoGenerado;
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerUsuarioResponsePorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con email: " + email));
        return UsuarioResponse.deEntidad(usuario);
    }

    // ⭐ NUEVO MÉTODO: actualizar permiso de matrícula de un alumno
    @Transactional
    public Usuario actualizarPermisoMatricula(Long idUsuario, boolean habilitado) {
        Usuario usuario = obtenerUsuarioPorId(idUsuario);

        if (usuario.getRol() != Rol.ALUMNO) {
            throw new ValidacionException("Solo se puede cambiar el permiso de matrícula a alumnos.");
        }

        usuario.setHabilitadoMatricula(habilitado);
        return usuarioRepository.save(usuario);
    }
}
