package com.proyecto.fundaciondeportiva.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProgresoDocenteResumenDTO {
    private int totalSecciones;
    private int totalAlumnos;
    private Double calificacionPromedioGlobal;
    private List<SeccionDocenteProgresoDTO> secciones;
}
