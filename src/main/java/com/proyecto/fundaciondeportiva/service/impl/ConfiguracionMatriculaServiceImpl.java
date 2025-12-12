// src/main/java/com/proyecto/fundaciondeportiva/service/impl/ConfiguracionMatriculaServiceImpl.java
package com.proyecto.fundaciondeportiva.service.impl;

import com.proyecto.fundaciondeportiva.dto.response.ConfiguracionMatriculaResponse;
import com.proyecto.fundaciondeportiva.model.entity.ConfiguracionMatricula;
import com.proyecto.fundaciondeportiva.repository.ConfiguracionMatriculaRepository;
import com.proyecto.fundaciondeportiva.service.ConfiguracionMatriculaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ConfiguracionMatriculaServiceImpl implements ConfiguracionMatriculaService {

    @Autowired
    private ConfiguracionMatriculaRepository configuracionMatriculaRepository;

    // Este método realiza una escritura, por lo que debe ser transaccional (sin readOnly)
    @Transactional
    protected ConfiguracionMatricula obtenerOCrearConfiguracion() {
        // Primero intenta encontrar la configuración existente
        return configuracionMatriculaRepository
                .findFirstByOrderByIdAsc()
                .orElseGet(() -> {
                    // Si no existe, crea una nueva configuración predeterminada
                    ConfiguracionMatricula nueva = new ConfiguracionMatricula();
                    nueva.setMatriculaHabilitada(false);
                    nueva.setFechaInicio(null);  // Puedes ajustarlo según sea necesario
                    nueva.setFechaFin(null);     // Lo mismo para las fechas
                    return configuracionMatriculaRepository.save(nueva);
                });
    }

    // Método para convertir la entidad a DTO para respuesta
    private ConfiguracionMatriculaResponse toResponse(ConfiguracionMatricula config) {
        if (config == null) return null;

        ConfiguracionMatriculaResponse resp = new ConfiguracionMatriculaResponse();
        resp.setMatriculaHabilitada(config.isMatriculaHabilitada());
        resp.setFechaInicio(config.getFechaInicio());
        resp.setFechaFin(config.getFechaFin());
        return resp;
    }

    // Este método solo obtiene la configuración sin modificarla, por lo que puede ser read-only
    @Override
    @Transactional(readOnly = true)  // Solo para lectura, sin modificaciones
    public ConfiguracionMatriculaResponse obtenerConfiguracionMatricula() {
        ConfiguracionMatricula config = obtenerOCrearConfiguracion();
        return toResponse(config);
    }

    // Este método actualiza las fechas de la matrícula
    @Override
    @Transactional  // Este debe ser transaccional para modificar los datos
    public ConfiguracionMatriculaResponse actualizarFechasMatricula(LocalDate fechaInicio, LocalDate fechaFin) {
        ConfiguracionMatricula config = obtenerOCrearConfiguracion();
        config.setFechaInicio(fechaInicio);
        config.setFechaFin(fechaFin);
        config = configuracionMatriculaRepository.save(config);  // Guarda la nueva configuración
        return toResponse(config);
    }

    // Este método actualiza si la matrícula está habilitada o no
    @Override
    @Transactional  // Este también debe ser transaccional para modificar los datos
    public ConfiguracionMatriculaResponse actualizarPermisoGlobalMatricula(boolean habilitada) {
        ConfiguracionMatricula config = obtenerOCrearConfiguracion();
        config.setMatriculaHabilitada(habilitada);
        config = configuracionMatriculaRepository.save(config);  // Guarda la nueva configuración
        return toResponse(config);
    }
}
