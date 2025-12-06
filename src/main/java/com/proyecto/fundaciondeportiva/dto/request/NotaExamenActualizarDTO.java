package com.proyecto.fundaciondeportiva.dto.request;

import lombok.Data;

@Data
public class NotaExamenActualizarDTO {
    private Long alumnoId;
    private Double nota; // null = pendiente
}
