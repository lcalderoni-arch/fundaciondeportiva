// src/main/java/com/proyecto/fundaciondeportiva/dto/request/RegistroAsistenciaAlumnoRequest.java
package com.proyecto.fundaciondeportiva.dto.request;

import com.proyecto.fundaciondeportiva.model.enums.EstadoAsistencia;
import lombok.Data;

@Data
public class RegistroAsistenciaAlumnoRequest {

    private Long alumnoId;
    private EstadoAsistencia estado;      // ASISTIO, TARDANZA, FALTA, JUSTIFICADA
    private String observaciones;         // opcional
}
