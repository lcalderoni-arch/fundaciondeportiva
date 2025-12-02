// src/main/java/com/proyecto/fundaciondeportiva/service/AsistenciaService.java
package com.proyecto.fundaciondeportiva.service;

import com.proyecto.fundaciondeportiva.dto.request.RegistrarAsistenciasSesionRequest;
import com.proyecto.fundaciondeportiva.dto.response.AsistenciaAlumnoSemanaDTO;
import com.proyecto.fundaciondeportiva.dto.response.AsistenciaDetalleAlumnoDTO;

import java.util.List;

public interface AsistenciaService {

    /**
     * DOCENTE: ver asistencias de una sesión concreta (una clase).
     */
    List<AsistenciaDetalleAlumnoDTO> obtenerAsistenciasPorSesion(Long sesionId);

    /**
     * DOCENTE: registrar/actualizar asistencias de una sesión.
     * El email del profesor viene del token (Authentication.getName()).
     */
    void registrarAsistenciasSesion(RegistrarAsistenciasSesionRequest request, String emailProfesor);

    /**
     * ALUMNO: ver su historial de asistencias en una sección.
     */
    List<AsistenciaAlumnoSemanaDTO> obtenerMisAsistenciasEnSeccion(Long seccionId, String emailAlumno);
}
