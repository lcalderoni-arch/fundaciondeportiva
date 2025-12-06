// src/main/java/com/proyecto/fundaciondeportiva/dto/response/SesionHorarioDTO.java
package com.proyecto.fundaciondeportiva.dto.response;

import com.proyecto.fundaciondeportiva.model.entity.Sesion;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SesionHorarioDTO {
    private Long sesionId;
    private String fecha;       // "2025-09-23"
    private String horaInicio;  // "08:00"
    private String horaFin;     // "10:00"

    private String tituloCurso;
    private String nombreSeccion;
    private String aula;

    // opcional según rol
    private String profesor;
}
