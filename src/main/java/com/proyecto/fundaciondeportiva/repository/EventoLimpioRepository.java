// EventoLimpioRepository.java
package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.EventoLimpio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EventoLimpioRepository extends JpaRepository<EventoLimpio, Long> {

    @Query("SELECT COALESCE(MAX(el.eventoId), 0) FROM EventoLimpio el")
    Long findUltimoEventoProcesado();

    @Query("SELECT el.calidad, COUNT(el) FROM EventoLimpio el GROUP BY el.calidad")
    List<Object[]> countByCalidad();  // 👈 NUEVO
}
