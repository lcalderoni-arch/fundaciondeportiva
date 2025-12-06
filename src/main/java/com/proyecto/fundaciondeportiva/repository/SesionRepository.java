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

    // ⭐ NUEVO: sesiones de un día
    List<Sesion> findByFecha(LocalDate fecha);

    // Por DNI del profesor (si Seccion tiene dniProfesor)
    //List<Sesion> findBySeccion_DniProfesorOrderByFechaAsc(String dniProfesor);

    // Por id del alumno (si Matricula tiene alumno.id o usuario.id)
    //List<Sesion> findBySeccion_Matriculas_Alumno_IdOrderByFechaAsc(Long alumnoId);
}