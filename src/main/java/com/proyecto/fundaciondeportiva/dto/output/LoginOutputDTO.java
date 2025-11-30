package com.proyecto.fundaciondeportiva.dto.output;

import com.proyecto.fundaciondeportiva.model.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginOutputDTO {

    private String token; // El JWT que usará el cliente
    private String nombre;
    private Rol rol;
    private String email; // NUEVO
    private String dni;   // NUEVO (solo para profesores, null para otros roles)

    private String nivelAlumno;
    private String gradoAlumno;
}