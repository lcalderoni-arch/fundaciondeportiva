// src/main/java/com/proyecto/fundaciondeportiva/dto/response/AsistenciaAlumnoSemanaDTO.java
package com.proyecto.fundaciondeportiva.dto.response;

import com.proyecto.fundaciondeportiva.model.enums.EstadoAsistencia;
import lombok.Data;

@Data
public class AsistenciaAlumnoSemanaDTO {

    private Integer semanaNumero;         // 1, 2, 3, ...
    private EstadoAsistencia estado;      // ASISTIO, TARDANZA, FALTA, JUSTIFICADA
}
