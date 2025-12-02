// src/main/java/com/proyecto/fundaciondeportiva/dto/request/ActualizarFechasMatriculaRequest.java
package com.proyecto.fundaciondeportiva.dto.request;

import lombok.Data;

@Data
public class ActualizarFechasMatriculaRequest {
    // El frontend envía strings "yyyy-MM-dd"
    private String fechaInicio;
    private String fechaFin;
}
