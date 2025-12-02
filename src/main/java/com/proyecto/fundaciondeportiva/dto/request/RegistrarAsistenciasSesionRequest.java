// src/main/java/com/proyecto/fundaciondeportiva/dto/request/RegistrarAsistenciasSesionRequest.java
package com.proyecto.fundaciondeportiva.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class RegistrarAsistenciasSesionRequest {

    private Long sesionId;
    private List<RegistroAsistenciaAlumnoRequest> registros;
}
