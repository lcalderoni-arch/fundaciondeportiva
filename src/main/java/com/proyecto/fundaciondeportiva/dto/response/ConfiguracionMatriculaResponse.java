package com.proyecto.fundaciondeportiva.dto.response;

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

    private boolean matriculaHabilitada;

    // ⭐ NUEVO: fechas de inicio y fin
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
}
