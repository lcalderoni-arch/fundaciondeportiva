// src/main/java/com/proyecto/fundaciondeportiva/repository/SesionRepository.java
package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SesionRepository extends JpaRepository<Sesion, Long> {

    // Usamos el campo 'fecha' porque tu entidad Sesion tiene:
    // private LocalDate fecha;
    List<Sesion> findBySeccion_IdOrderByFechaAsc(Long seccionId);
}
