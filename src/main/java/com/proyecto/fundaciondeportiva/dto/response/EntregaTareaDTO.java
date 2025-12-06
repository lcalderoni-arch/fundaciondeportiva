package com.proyecto.fundaciondeportiva.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EntregaTareaDTO {

    private Long id;
    private Long recursoId;
    private Long alumnoId;
    private String alumnoNombre;

    private String titulo;
    private String descripcion;
    private String archivoUrl;
    private LocalDateTime fechaEntrega;

    private Double nota;
    private String retroalimentacion;
}
