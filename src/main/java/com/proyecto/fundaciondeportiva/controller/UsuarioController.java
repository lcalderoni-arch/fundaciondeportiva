package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.input.UsuarioInputDTO;
import com.proyecto.fundaciondeportiva.dto.output.UsuarioUpdateDTO;
import com.proyecto.fundaciondeportiva.dto.output.UsuarioOutputDTO;
import com.proyecto.fundaciondeportiva.dto.request.CambiarPermisoMatriculaRequest;
import com.proyecto.fundaciondeportiva.model.entity.Usuario;
import com.proyecto.fundaciondeportiva.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping(value = "/crear", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UsuarioOutputDTO> crearUsuario(@Valid @RequestBody UsuarioInputDTO inputDTO) {
        Usuario nuevoUsuario = usuarioService.crearUsuario(inputDTO);
        UsuarioOutputDTO outputDTO = UsuarioOutputDTO.deEntidad(nuevoUsuario);
        return new ResponseEntity<>(outputDTO, HttpStatus.CREATED);
    }

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioOutputDTO> obtenerPerfilPropio() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String emailAutenticado = userDetails.getUsername();

        Usuario usuario = usuarioService.obtenerUsuarioPorEmail(emailAutenticado)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado en la base de datos."));

        UsuarioOutputDTO outputDTO = UsuarioOutputDTO.deEntidad(usuario);
        return ResponseEntity.ok(outputDTO);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<UsuarioOutputDTO>> listarTodosLosUsuarios() {
        List<Usuario> usuarios = usuarioService.listarTodosLosUsuarios();
        List<UsuarioOutputDTO> outputDTOs = usuarios.stream()
                .map(UsuarioOutputDTO::deEntidad)
                .collect(Collectors.toList());
        return ResponseEntity.ok(outputDTOs);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UsuarioOutputDTO> obtenerUsuarioPorId(@PathVariable Long id) {
        Usuario usuario = usuarioService.obtenerUsuarioPorId(id);
        UsuarioOutputDTO outputDTO = UsuarioOutputDTO.deEntidad(usuario);
        return ResponseEntity.ok(outputDTO);
    }

    @GetMapping(value = "/alumnos", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<UsuarioOutputDTO>> listarAlumnos() {
        List<Usuario> alumnos = usuarioService.listarAlumnos();

        List<UsuarioOutputDTO> dtoList = alumnos.stream()
                .map(UsuarioOutputDTO::deEntidad)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    @PutMapping(value = "/editar/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UsuarioOutputDTO> editarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateDTO updateDTO) {
        Usuario usuarioActualizado = usuarioService.actualizarUsuario(id, updateDTO);
        UsuarioOutputDTO outputDTO = UsuarioOutputDTO.deEntidad(usuarioActualizado);
        return ResponseEntity.ok(outputDTO);
    }

    @DeleteMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    // ⭐ NUEVO ENDPOINT: cambiar permiso de matrícula de un alumno
    @PatchMapping(
            value = "/{id}/permiso-matricula",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UsuarioOutputDTO> actualizarPermisoMatricula(
            @PathVariable Long id,
            @RequestBody CambiarPermisoMatriculaRequest request
    ) {
        Usuario usuarioActualizado = usuarioService.actualizarPermisoMatricula(id, request.isHabilitado());
        UsuarioOutputDTO outputDTO = UsuarioOutputDTO.deEntidad(usuarioActualizado);
        return ResponseEntity.ok(outputDTO);
    }
}
