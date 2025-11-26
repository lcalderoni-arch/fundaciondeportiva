package com.proyecto.fundaciondeportiva.dto.output;


import com.proyecto.fundaciondeportiva.model.entity.Usuario;
import com.proyecto.fundaciondeportiva.model.entity.PerfilAlumno;
import com.proyecto.fundaciondeportiva.model.entity.PerfilProfesor;
import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
import com.proyecto.fundaciondeportiva.model.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioOutputDTO {

    private Long id;
    private String nombre;
    private String email;
    private Rol rol;
    private LocalDateTime fechaCreacion;

    // AÑADIDO: DNI; TELEFONO
    private String codigoEstudiante;
    private String dniAlumno;
    private NivelAcademico nivel;
    private String grado;
    private String telefonoEmergencia; // NUEVO

    // CAMPO DE ALUMNO: 'grado' en lugar de 'carrera'
    private String dniProfesor;
    private String telefono;
    private String experiencia;
    private String gradoAcademico;

    // ELIMINADO: private String departamento;
    // ELIMINADO: private String carrera;

    public static UsuarioOutputDTO deEntidad(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        UsuarioOutputDTO dto = UsuarioOutputDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .fechaCreacion(usuario.getFechaCreacion())
                .build();

        // Mapea los campos de PerfilAlumno si existen
        if (usuario.getPerfilAlumno() != null) {
            PerfilAlumno perfil = usuario.getPerfilAlumno();
            dto.setCodigoEstudiante(perfil.getCodigoEstudiante());
            dto.setDniAlumno(perfil.getDni()); // <-- USA EL SETTER CORRECTO
            dto.setNivel(perfil.getNivel());
            dto.setGrado(perfil.getGrado());
            dto.setTelefonoEmergencia(perfil.getTelefonoEmergencia()); // NUEVO
        }

        // Mapea los campos de PerfilProfesor si existen
        if (usuario.getPerfilProfesor() != null) {
            PerfilProfesor perfil = usuario.getPerfilProfesor();
            dto.setDniProfesor(perfil.getDni()); // <-- USA EL SETTER CORRECTO
            dto.setTelefono(perfil.getTelefono());
            dto.setExperiencia(perfil.getExperiencia());
            dto.setGradoAcademico(perfil.getGradoAcademico());
        }

        return dto;
    }

}

