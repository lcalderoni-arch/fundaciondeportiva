// src/main/java/com/proyecto/fundaciondeportiva/dto/response/RecursoDTO.java
package com.proyecto.fundaciondeportiva.dto.response;

import com.proyecto.fundaciondeportiva.model.enums.MomentoSesion;
import com.proyecto.fundaciondeportiva.model.enums.TipoRecurso;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecursoDTO {

    private Long id;

    private String titulo;
    private String descripcion;

    private MomentoSesion momento;
    private TipoRecurso tipo;

    private String archivoUrl;
    private String linkVideo;

    private LocalDateTime fechaPublicacion;

    private Long sesionId;
}
