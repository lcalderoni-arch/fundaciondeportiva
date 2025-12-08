package com.proyecto.fundaciondeportiva.service;

import com.proyecto.fundaciondeportiva.model.entity.Evento;
import com.proyecto.fundaciondeportiva.model.entity.EventoLimpio;
import com.proyecto.fundaciondeportiva.repository.EventoLimpioRepository;
import com.proyecto.fundaciondeportiva.repository.EventoRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DataCleaningService {

    private final EventoRepository eventoRepo;
    private final EventoLimpioRepository limpioRepo;

    public DataCleaningService(EventoRepository eventoRepo,
                               EventoLimpioRepository limpioRepo) {
        this.eventoRepo = eventoRepo;
        this.limpioRepo = limpioRepo;
    }

    @Transactional
    public int limpiarEventosNuevos() {
        Long lastId = Optional.ofNullable(limpioRepo.findUltimoEventoProcesado()).orElse(0L);
        List<Evento> nuevos = eventoRepo.findNuevos(lastId);

        int procesados = 0;

        for (Evento e : nuevos) {
            EventoLimpio limpio = limpiarUno(e);
            if (limpio != null) {
                limpioRepo.save(limpio);
                procesados++;
            }
        }
        return procesados;
    }

    private EventoLimpio limpiarUno(Evento e) {
        // 1) descartar registros muy incompletos
        if (e.getUsuarioId() == null || e.getTipo() == null) {
            return null;
        }

        String tipoNorm = normalizarTipo(e.getTipo());
        String detallesNorm = normalizarDetalles(e.getDetalles());
        String calidad = tipoNorm.equals(e.getTipo()) ? "OK" : "CORREGIDO";

        EventoLimpio limpio = new EventoLimpio();
        limpio.setEventoId(e.getId());
        limpio.setUsuarioId(e.getUsuarioId());
        limpio.setTipo(tipoNorm);
        limpio.setDetalles(detallesNorm);
        limpio.setTs(e.getTs()); // aquí podrías ajustar zona horaria
        limpio.setCalidad(calidad);

        return limpio;
    }

    private String normalizarTipo(String tipo) {
        if (tipo == null) return "OTRO";
        String t = tipo.trim().toUpperCase();

        if (t.contains("CURSO")) return "VER_CURSO";
        if (t.contains("QUIZ") || t.contains("EXAM")) return "INICIAR_QUIZ";
        if (t.contains("LOGIN")) return "LOGIN";

        return "OTRO";
    }

    private String normalizarDetalles(String detalles) {
        if (detalles == null || detalles.isBlank()) {
            return "{}";
        }
        // aquí podrías parsear JSON y corregir campos si quieres
        return detalles;
    }
}
