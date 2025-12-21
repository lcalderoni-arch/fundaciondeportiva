package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.request.EventoRequest;
import com.proyecto.fundaciondeportiva.model.entity.Evento;
import com.proyecto.fundaciondeportiva.repository.EventoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    private final EventoRepository eventoRepository;

    public EventoController(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR','PROFESOR','ALUMNO')")
    public ResponseEntity<Void> registrarEvento(@RequestBody EventoRequest request) {

        Evento e = new Evento();

        // ⚠️ IMPORTANTE: usuarioId NO puede ser null porque en la entidad está nullable = false
        // Lo ideal es obtenerlo del usuario autenticado (JWT), pero para probar
        // podemos poner un valor fijo y luego lo mejoramos.
        e.setUsuarioId(1L); // 🔧 luego lo cambias para que venga del usuario logueado

        e.setTipo(request.getTipo());
        e.setDetalles(request.getDetalles());
        e.setTs(LocalDateTime.now());

        eventoRepository.save(e);

        return ResponseEntity.ok().build();
    }
}
