package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.request.EventoRequest;
import com.proyecto.fundaciondeportiva.model.entity.Evento;
import com.proyecto.fundaciondeportiva.model.entity.Usuario;
import com.proyecto.fundaciondeportiva.repository.EventoRepository;
import com.proyecto.fundaciondeportiva.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    private final EventoRepository eventoRepository;
    private final UsuarioService usuarioService;

    public EventoController(EventoRepository eventoRepository, UsuarioService usuarioService) {
        this.eventoRepository = eventoRepository;
        this.usuarioService = usuarioService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','PROFESOR','ALUMNO')")
    public ResponseEntity<Void> registrarEvento(@RequestBody EventoRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Usuario u = usuarioService.obtenerUsuarioPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        Evento e = new Evento();
        e.setUsuarioId(u.getId());
        e.setTipo(request.getTipo());
        e.setDetalles(request.getDetalles());
        e.setTs(LocalDateTime.now());

        eventoRepository.save(e);
        return ResponseEntity.ok().build();
    }
}
