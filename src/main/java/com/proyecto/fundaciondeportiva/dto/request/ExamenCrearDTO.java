package com.proyecto.fundaciondeportiva.dto.request;

import lombok.Data;

@Data
public class ExamenCrearDTO {
    private String titulo;
    private String descripcion;
    private String fecha;          // "2025-12-10" opcional
    private Double pesoPorcentual; // obligatorio
    private Double notaMaxima;     // opcional, default 20
    private Integer orden;         // opcional (1..10)
}
