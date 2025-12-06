package com.proyecto.fundaciondeportiva.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotaExamenFilaDocenteDTO {

    private Long alumnoId;
    private String alumnoNombre;
    private Double nota;
    private String estado;            // APROBADO / DESAPROBADO / PENDIENTE
    private String fechaActualizacion; // opcional
}
