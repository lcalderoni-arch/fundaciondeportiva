// src/main/java/com/proyecto/fundaciondeportiva/dto/request/RecursoRequest.java
package com.proyecto.fundaciondeportiva.dto.request;

import com.proyecto.fundaciondeportiva.model.enums.MomentoSesion;
import com.proyecto.fundaciondeportiva.model.enums.TipoRecurso;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecursoRequest {
    private Long sesionId;
    private String titulo;
    private String descripcion;
    private MomentoSesion momento;
    private TipoRecurso tipo;
    private String archivoUrl;
    private String linkVideo;

    // Para tareas
    private LocalDateTime fechaInicioEntrega;
    private LocalDateTime fechaFinEntrega;
    private Boolean permiteEntregas;
}
