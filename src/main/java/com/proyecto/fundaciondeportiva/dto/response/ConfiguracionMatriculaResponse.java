package com.proyecto.fundaciondeportiva.dto.response;

import com.proyecto.fundaciondeportiva.model.entity.ConfiguracionMatricula;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionMatriculaResponse {

    // nombre pensado para el front, AJUSTA si quieres
    private boolean matriculaHabilitada;

    public static ConfiguracionMatriculaResponse deEntidad(ConfiguracionMatricula entidad) {
        if (entidad == null) return null;
        return ConfiguracionMatriculaResponse.builder()
                .matriculaHabilitada(entidad.isMatriculaHabilitada())
                .build();
    }
}
