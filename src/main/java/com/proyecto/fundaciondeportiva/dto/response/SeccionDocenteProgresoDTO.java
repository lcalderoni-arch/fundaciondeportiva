package com.proyecto.fundaciondeportiva.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeccionDocenteProgresoDTO {
    private Long seccionId;
    private String cursoTitulo;
    private String seccionNombre;

    private int alumnosActivos;
    private Double notaPromedioSeccion;   // promedio de calificaciones de la sección
}
