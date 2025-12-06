package com.proyecto.fundaciondeportiva.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CursoAlumnoProgresoDTO {
    private Long seccionId;
    private String cursoTitulo;
    private String seccionNombre;

    // Nota final en escala 0-20 (o la que uses)
    private Double notaFinal;

    // Avance en porcentaje (0-100), por si luego quieres mostrar barras
    private Double avancePorcentaje;
}
