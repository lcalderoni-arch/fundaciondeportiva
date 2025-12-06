package com.proyecto.fundaciondeportiva.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NotasExamenDocenteDTO {

    private Long examenId;
    private String tituloExamen;
    private Double notaMaxima;
    private Double pesoPorcentual;
    private double notaMinimaAprobatoria;

    private int totalAlumnos;
    private int aprobados;
    private int desaprobados;
    private int pendientes;
    private List<NotaExamenFilaDocenteDTO> alumnos;
}
