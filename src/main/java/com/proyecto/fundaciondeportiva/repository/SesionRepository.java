// src/main/java/com/proyecto/fundaciondeportiva/repository/SesionRepository.java
package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SesionRepository extends JpaRepository<Sesion, Long> {

    List<Sesion> findBySeccion_IdOrderByFechaAsc(Long seccionId);

    // ⭐ sesiones de un día
    List<Sesion> findByFecha(LocalDate fecha);

    // ⭐ TODAS las sesiones de un profesor (por id del usuario profesor)
    List<Sesion> findBySeccion_Profesor_IdOrderByFechaAsc(Long profesorId);

    // ⭐ TODAS las sesiones de un alumno (por id del usuario alumno vía matrícula)
    List<Sesion> findBySeccion_Matriculas_Alumno_IdOrderByFechaAsc(Long alumnoId);
}