// src/main/java/com/proyecto/fundaciondeportiva/dto/response/MonitorAsistenciaSesionDTO.java
package com.proyecto.fundaciondeportiva.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MonitorAsistenciaSesionDTO {

    private Long sesionId;
    private Long seccionId;

    // NUEVO
    private String nombreSeccion;

    private String curso;
    private String gradoSeccion;
    private String nivelSeccion;

    // Strings "08:30", "10:00" para mostrar fácil en front
    private String horaInicio;
    private String horaFin;

    private int totalAlumnos;
    private int conAsistencia;
    private int sinAsistencia;

    // PROXIMA, EN_CURSO, ALERTA, OK
    private String estadoSemaforo;
}
