package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.Examen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamenRepository extends JpaRepository<Examen, Long> {

    List<Examen> findBySeccion_IdOrderByOrdenAscIdAsc(Long seccionId);

    long countBySeccion_Id(Long seccionId);
}
