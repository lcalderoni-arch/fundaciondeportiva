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

    @Transactional
    protected ConfiguracionMatricula obtenerOCrearConfiguracion() {
        return configuracionMatriculaRepository
                .findFirstByOrderByIdAsc()
                .orElseGet(() -> {
                    ConfiguracionMatricula nueva = new ConfiguracionMatricula();
                    nueva.setMatriculaHabilitada(false);
                    nueva.setFechaInicio(null);
                    nueva.setFechaFin(null);
                    return configuracionMatriculaRepository.save(nueva);
                });
    }

    private ConfiguracionMatriculaResponse toResponse(ConfiguracionMatricula config) {
        if (config == null) return null;

        ConfiguracionMatriculaResponse resp = new ConfiguracionMatriculaResponse();
        resp.setMatriculaHabilitada(config.isMatriculaHabilitada());
        resp.setFechaInicio(config.getFechaInicio());
        resp.setFechaFin(config.getFechaFin());
        return resp;
    }

    @Override
    @Transactional(readOnly = true)
    public ConfiguracionMatriculaResponse obtenerConfiguracionMatricula() {
        ConfiguracionMatricula config = obtenerOCrearConfiguracion();
        return toResponse(config);
    }

    @Override
    @Transactional
    public ConfiguracionMatriculaResponse actualizarFechasMatricula(LocalDate fechaInicio, LocalDate fechaFin) {
        ConfiguracionMatricula config = obtenerOCrearConfiguracion();
        config.setFechaInicio(fechaInicio);
        config.setFechaFin(fechaFin);
        config = configuracionMatriculaRepository.save(config);
        return toResponse(config);
    }

    @Override
    @Transactional
    public ConfiguracionMatriculaResponse actualizarPermisoGlobalMatricula(boolean habilitada) {
        ConfiguracionMatricula config = obtenerOCrearConfiguracion();
        config.setMatriculaHabilitada(habilitada);
        config = configuracionMatriculaRepository.save(config);
        return toResponse(config);
    }
}
