package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.request.EventoRequest;
import com.proyecto.fundaciondeportiva.model.entity.Evento;
import com.proyecto.fundaciondeportiva.repository.EventoRepository;
import com.proyecto.fundaciondeportiva.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;

    public EventoController(EventoRepository eventoRepository, UsuarioRepository usuarioRepository) {
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR','PROFESOR','ALUMNO')")
    public ResponseEntity<Void> registrarEvento(@RequestBody EventoRequest request,
                                                Authentication authentication) {

        // username/email viene del JWT (según tu JwtAuthenticationFilter)
        String email = authentication.getName();

        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        Evento e = new Evento();
        e.setUsuarioId(usuario.getId());      // ✅ id real
        e.setTipo(request.getTipo());
        e.setDetalles(request.getDetalles());
        e.setTs(LocalDateTime.now());

        eventoRepository.save(e);

        return ResponseEntity.ok().build();
    }
}
