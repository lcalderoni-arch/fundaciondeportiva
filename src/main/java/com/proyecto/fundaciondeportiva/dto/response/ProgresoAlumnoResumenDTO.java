package com.proyecto.fundaciondeportiva.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProgresoAlumnoResumenDTO {
    private int totalCursosActivos;
    private Double notaPromedio;       // misma escala de calificación
    private Double avancePromedio;     // % promedio para el UI
    private List<CursoAlumnoProgresoDTO> cursos;
}
