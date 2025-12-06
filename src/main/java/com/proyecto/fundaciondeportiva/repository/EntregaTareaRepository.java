package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.EntregaTarea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// repository/EntregaTareaRepository.java
public interface EntregaTareaRepository extends JpaRepository<EntregaTarea, Long> {
    Optional<EntregaTarea> findByRecursoIdAndAlumnoId(Long recursoId, Long alumnoId);
    List<EntregaTarea> findByRecursoId(Long recursoId);
}
