package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    List<Asistencia> findBySesionId(Long sesionId);

    int countBySesionId(Long sesionId);
    int countBySesionIdAndEstadoIsNotNull(Long sesionId);

    // ✅ CLAVE: ahora se usa Matricula + Sesion
    Optional<Asistencia> findByMatriculaIdAndSesionId(Long matriculaId, Long sesionId);

    List<Asistencia> findByMatriculaId(Long matriculaId);
}
