// src/main/java/com/proyecto/fundaciondeportiva/dto/request/RecursoRequest.java
package com.proyecto.fundaciondeportiva.dto.request;

import com.proyecto.fundaciondeportiva.model.enums.MomentoSesion;
import com.proyecto.fundaciondeportiva.model.enums.TipoRecurso;
import lombok.Data;

@Data
public class RecursoRequest {

    private Long sesionId;          // sesión a la que pertenece

    private String titulo;
    private String descripcion;

    // EXPLORA / ESTUDIA / APLICA (o lo que tengas)
    private MomentoSesion momento;

    // PDF, LINK, VIDEO, etc (según tu enum)
    private TipoRecurso tipo;

    // Para recursos tipo link (ej: YouTube u otros)
    private String linkVideo;

    // Para recursos ya subidos y que solo quieres registrar
    private String archivoUrl;
}
