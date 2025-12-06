// src/main/java/com/proyecto/fundaciondeportiva/dto/response/EntregaTareaDTO.java
package com.proyecto.fundaciondeportiva.dto.response;

import java.time.LocalDateTime;

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

    // getters y setters
}
