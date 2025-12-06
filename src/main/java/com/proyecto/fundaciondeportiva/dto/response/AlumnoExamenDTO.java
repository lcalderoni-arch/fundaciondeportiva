package com.proyecto.fundaciondeportiva.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AlumnoExamenDTO {

    private Long examenId;
    private String cursoTitulo;
    private String seccionNombre;
    private String tituloExamen;
    private String fecha;           // opcional
    private Double nota;
    private String estado;          // APROBADO / DESAPROBADO / PENDIENTE
    private Double pesoPorcentual;
    private Double notaMaxima;
}
