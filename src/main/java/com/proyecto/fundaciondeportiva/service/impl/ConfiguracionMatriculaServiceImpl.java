package com.proyecto.fundaciondeportiva.service.impl;

import com.proyecto.fundaciondeportiva.dto.response.ConfiguracionMatriculaResponse;
import com.proyecto.fundaciondeportiva.exception.RecursoNoEncontradoException;
import com.proyecto.fundaciondeportiva.exception.ValidacionException;
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

    @Override
    @Transactional(readOnly = true)
    public ConfiguracionMatriculaResponse obtenerConfiguracionMatricula() {
        ConfiguracionMatricula config = configuracionMatriculaRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe configuración de matrícula."));

        return ConfiguracionMatriculaResponse.deEntidad(config);
    }

    @Override
    @Transactional
    public ConfiguracionMatriculaResponse actualizarFechasMatricula(LocalDate fechaInicio, LocalDate fechaFin) {
        ConfiguracionMatricula config = configuracionMatriculaRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe configuración de matrícula."));

        if (fechaInicio == null || fechaFin == null) {
            throw new ValidacionException("Fecha inicio y fecha fin son obligatorias.");
        }
        if (fechaInicio.isAfter(fechaFin)) {
            throw new ValidacionException("La fecha de inicio no puede ser mayor que la fecha de cierre.");
        }

        config.setFechaInicio(fechaInicio);
        config.setFechaFin(fechaFin);

        ConfiguracionMatricula guardada = configuracionMatriculaRepository.save(config);
        return ConfiguracionMatriculaResponse.deEntidad(guardada);
    }

    @Override
    @Transactional
    public ConfiguracionMatriculaResponse actualizarPermisoGlobalMatricula(boolean habilitado) {
        ConfiguracionMatricula config = configuracionMatriculaRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe configuración de matrícula."));

        config.setMatriculaHabilitada(habilitado);

        ConfiguracionMatricula guardada = configuracionMatriculaRepository.save(config);
        return ConfiguracionMatriculaResponse.deEntidad(guardada);
    }

    // NUEVO
    @Override
    @Transactional
    public ConfiguracionMatriculaResponse actualizarCicloActual(String cicloActual) {
        ConfiguracionMatricula config = configuracionMatriculaRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe configuración de matrícula."));

        String valor = (cicloActual == null) ? "" : cicloActual.trim();
        if (valor.isEmpty()) {
            throw new ValidacionException("El ciclo actual no puede estar vacío.");
        }

        config.setCicloActual(valor);

        ConfiguracionMatricula guardada = configuracionMatriculaRepository.save(config);
        return ConfiguracionMatriculaResponse.deEntidad(guardada);
    }
}
