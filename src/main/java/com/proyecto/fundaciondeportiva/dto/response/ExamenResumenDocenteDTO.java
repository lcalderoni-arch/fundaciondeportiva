package com.proyecto.fundaciondeportiva.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExamenResumenDocenteDTO {

    private Long examenId;
    private String titulo;
    private String descripcion;
    private String fecha; // ISO string (opcional)

    private Double pesoPorcentual;
    private Double notaMaxima;
    private Integer orden;

    private int totalAlumnos;
    private int aprobados;
    private int desaprobados;
    private int pendientes;
    private Double promedio;
}
