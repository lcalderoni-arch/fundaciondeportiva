package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.PerfilAlumno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PerfilAlumnoRepository extends JpaRepository<PerfilAlumno, Long> {

    Optional<PerfilAlumno> findByDni(String dni);

    Optional<PerfilAlumno> findByCodigoEstudiante(String codigoEstudiante);

    boolean existsByCodigoEstudiante(String codigoEstudiante);

    // AÑADIDO: Método para verificar unicidad del DNI
    boolean existsByDni(String dni);

}