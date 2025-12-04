// src/main/java/com/proyecto/fundaciondeportiva/dto/response/SesionSimpleDTO.java
package com.proyecto.fundaciondeportiva.dto.response;

import com.proyecto.fundaciondeportiva.model.entity.Sesion;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SesionSimpleDTO {
    private Long id;
    private String tema;
    private LocalDate fecha;

    public static SesionSimpleDTO deEntidad(Sesion sesion) {
        SesionSimpleDTO dto = new SesionSimpleDTO();
        dto.setId(sesion.getId());
        dto.setTema(sesion.getTema());
        dto.setFecha(sesion.getFecha());
        return dto;
    }
}
