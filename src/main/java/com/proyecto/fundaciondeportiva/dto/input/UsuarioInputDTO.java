package com.proyecto.fundaciondeportiva.dto.input;


import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
import com.proyecto.fundaciondeportiva.model.enums.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioInputDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 100)
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotNull(message = "El rol es obligatorio")
    private Rol rol;

    // AÑADIDO: DNI (Común para Alumno y Profesor)
    @Size(min = 8, max = 15, message = "DNI de Profesor debe tener entre 8 y 15 caracteres")
    private String dniProfesor;

    private String telefono;
    private String experiencia;
    private String gradoAcademico;

    @Size(min = 8, max = 15, message = "DNI de Alumno debe tener entre 8 y 15 caracteres")
    private String dniAlumno;

    private String codigoEstudiante;

    // CAMPO DE ALUMNO: 'grado' en lugar de 'carrera'
    private NivelAcademico nivel;
    private String grado;

    @Size(max = 9, message = "Teléfono de emergencia no debe exceder 9 caracteres")
    private String telefonoEmergencia;
    // ELIMINADO: private String carrera;
    // ELIMINADO: private String codigoEstudiante; (Se genera en el servicio)
    // ELIMINADO: private String departamento;
}