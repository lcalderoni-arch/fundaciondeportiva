package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.NotaExamen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotaExamenRepository extends JpaRepository<NotaExamen, Long> {

    List<NotaExamen> findByExamen_Id(Long examenId);

    Optional<NotaExamen> findByExamen_IdAndAlumno_Id(Long examenId, Long alumnoId);

    List<NotaExamen> findByAlumno_Id(Long alumnoId);
}
