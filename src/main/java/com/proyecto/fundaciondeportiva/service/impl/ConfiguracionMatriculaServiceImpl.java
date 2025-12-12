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

    private ConfiguracionMatricula obtenerConfiguracionExistente() {
        return configuracionMatriculaRepository
                .findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException(
                        "No existe configuración de matrícula. Debe inicializarse."
                ));
    }

    private ConfiguracionMatriculaResponse toResponse(ConfiguracionMatricula config) {
        return ConfiguracionMatriculaResponse.builder()
                .matriculaHabilitada(config.isMatriculaHabilitada())
                .fechaInicio(config.getFechaInicio())
                .fechaFin(config.getFechaFin())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ConfiguracionMatriculaResponse obtenerConfiguracionMatricula() {
        ConfiguracionMatricula config = obtenerConfiguracionExistente();
        return toResponse(config);
    }

    @Override
    @Transactional
    public ConfiguracionMatriculaResponse actualizarFechasMatricula(LocalDate fechaInicio, LocalDate fechaFin) {
        ConfiguracionMatricula config = obtenerConfiguracionExistente();
        config.setFechaInicio(fechaInicio);
        config.setFechaFin(fechaFin);
        config = configuracionMatriculaRepository.save(config);
        return toResponse(config);
    }

    @Override
    @Transactional
    public ConfiguracionMatriculaResponse actualizarPermisoGlobalMatricula(boolean habilitada) {
        ConfiguracionMatricula config = obtenerConfiguracionExistente();
        config.setMatriculaHabilitada(habilitada);
        config = configuracionMatriculaRepository.save(config);
        return toResponse(config);
    }
}
