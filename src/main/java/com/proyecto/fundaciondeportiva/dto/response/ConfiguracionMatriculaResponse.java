package com.proyecto.fundaciondeportiva.dto.response;

import com.proyecto.fundaciondeportiva.model.entity.ConfiguracionMatricula;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionMatriculaResponse {

    private Long id;

    // Fechas del periodo de matrícula
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    // Permiso global de matrícula
    private Boolean matriculaHabilitada;

    // Ciclo / Año escolar actual
    private String cicloActual;

    public static ConfiguracionMatriculaResponse deEntidad(ConfiguracionMatricula config) {
        if (config == null) return null;

        return ConfiguracionMatriculaResponse.builder()
                .id(config.getId())
                .fechaInicio(config.getFechaInicio())
                .fechaFin(config.getFechaFin())
                .matriculaHabilitada(config.isMatriculaHabilitada())
                .cicloActual(config.getCicloActual())
                .build();
    }
}
