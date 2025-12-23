package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.Matricula;
import com.proyecto.fundaciondeportiva.model.enums.EstadoMatricula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    List<Matricula> findByAlumnoId(Long alumnoId);

    List<Matricula> findBySeccionId(Long seccionId);

    List<Matricula> findByAlumnoIdAndEstado(Long alumnoId, EstadoMatricula estado);

    List<Matricula> findBySeccionIdAndEstado(Long seccionId, EstadoMatricula estado);

    List<Matricula> findByEstado(EstadoMatricula estado);

    // Para retirar / obtener matrícula ACTIVA sin mezclar histórico
    Optional<Matricula> findByAlumnoIdAndSeccionIdAndCiclo(Long alumnoId, Long seccionId, String ciclo);

    Optional<Matricula> findByAlumnoIdAndSeccionIdAndCicloAndEstado(
            Long alumnoId, Long seccionId, String ciclo, EstadoMatricula estado
    );
}
