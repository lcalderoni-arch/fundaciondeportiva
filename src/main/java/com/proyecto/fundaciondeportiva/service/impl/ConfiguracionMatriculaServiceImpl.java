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
                .orElseThrow(() ->
                        new IllegalStateException("No existe configuración de matrícula. Debe inicializarse.")
                );
    }

    private ConfiguracionMatriculaResponse toResponse(ConfiguracionMatricula config) {
        ConfiguracionMatriculaResponse resp = new ConfiguracionMatriculaResponse();
        resp.setMatriculaHabilitada(config.isMatriculaHabilitada());
        resp.setFechaInicio(config.getFechaInicio());
        resp.setFechaFin(config.getFechaFin());
        return resp;
    }

    @Override
    @Transactional(readOnly = true)
    public ConfiguracionMatriculaResponse obtenerConfiguracionMatricula() {
        return toResponse(obtenerConfiguracionExistente());
    }

    @Override
    @Transactional
    public ConfiguracionMatriculaResponse actualizarFechasMatricula(LocalDate fechaInicio, LocalDate fechaFin) {
        ConfiguracionMatricula config = obtenerConfiguracionExistente();
        config.setFechaInicio(fechaInicio);
        config.setFechaFin(fechaFin);
        return toResponse(configuracionMatriculaRepository.save(config));
    }

    @Override
    @Transactional
    public ConfiguracionMatriculaResponse actualizarPermisoGlobalMatricula(boolean habilitada) {
        ConfiguracionMatricula config = obtenerConfiguracionExistente();
        config.setMatriculaHabilitada(habilitada);
        return toResponse(configuracionMatriculaRepository.save(config));
    }
}
