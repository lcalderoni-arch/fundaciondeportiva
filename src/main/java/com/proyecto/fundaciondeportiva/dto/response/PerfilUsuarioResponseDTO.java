// src/main/java/com/proyecto/fundaciondeportiva/dto/response/PerfilUsuarioResponseDTO.java
package com.proyecto.fundaciondeportiva.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PerfilUsuarioResponseDTO {

    private Long id;
    private String nombre;
    private String email;
    private String rol;

    // 🔹 Datos comunes opcionales
    private String telefono;

    // 🔹 Datos Alumno
    private String dniAlumno;
    private String nivelAlumno;   // INICIAL / PRIMARIA / SECUNDARIA
    private String gradoAlumno;   // Ej. "2do B"
    private String telefonoEmergencia;

    // 🔹 Datos Profesor
    private String dniProfesor;
    private String telefonoProfesor;
    private String experienciaProfesor;
}
