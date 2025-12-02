// src/main/java/com/proyecto/fundaciondeportiva/dto/response/AsistenciaDetalleAlumnoDTO.java
package com.proyecto.fundaciondeportiva.dto.response;

import com.proyecto.fundaciondeportiva.model.enums.EstadoAsistencia;
import lombok.Data;

@Data
public class AsistenciaDetalleAlumnoDTO {

    private Long alumnoId;
    private String nombreAlumno;
    private String codigoEstudiante;  // si lo tienes en PerfilAlumno
    private EstadoAsistencia estado;  // puede venir null si aún no se tomó
    private String observaciones;
}
