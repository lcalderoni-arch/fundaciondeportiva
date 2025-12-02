package com.proyecto.fundaciondeportiva.service;

import com.proyecto.fundaciondeportiva.model.entity.ConfiguracionMatricula;
import com.proyecto.fundaciondeportiva.repository.ConfiguracionMatriculaRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfiguracionMatriculaService {

    @Autowired
    private ConfiguracionMatriculaRepository configuracionMatriculaRepository;

    // Opcional: crear un registro por defecto al iniciar la app si no hay ninguno
    @PostConstruct
    @Transactional
    public void inicializarConfiguracion() {
        long total = configuracionMatriculaRepository.count();
        if (total == 0) {
            ConfiguracionMatricula conf = ConfiguracionMatricula.builder()
                    .matriculaHabilitada(true) // por defecto habilitada
                    .build();
            configuracionMatriculaRepository.save(conf);
        }
    }

    @Transactional(readOnly = true)
    public ConfiguracionMatricula obtenerConfiguracionActual() {
        // en este diseño asumimos que solo hay 1 registro
        return configuracionMatriculaRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No se encontró configuración de matrícula. Verifica inicialización."
                ));
    }

    @Transactional
    public ConfiguracionMatricula actualizarEstadoMatricula(boolean habilitada) {
        ConfiguracionMatricula conf = obtenerConfiguracionActual();
        conf.setMatriculaHabilitada(habilitada);
        return configuracionMatriculaRepository.save(conf);
    }
}
