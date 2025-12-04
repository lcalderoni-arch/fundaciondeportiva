package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    // Ya lo debes tener:
    List<Asistencia> findBySesionId(Long sesionId);

    // 👉 Nuevo: contar las asistencias de una sesión
    int countBySesionId(Long sesionId);

    // 👉 Aún mejor: solo contar las asistencias con estado NO nulo
    int countBySesionIdAndEstadoIsNotNull(Long sesionId);

    List<Asistencia> findByAlumnoIdAndSesion_SeccionId(Long alumnoId, Long seccionId);

    Optional<Asistencia> findBySesionIdAndAlumnoId(Long sesionId, Long alumnoId);

}