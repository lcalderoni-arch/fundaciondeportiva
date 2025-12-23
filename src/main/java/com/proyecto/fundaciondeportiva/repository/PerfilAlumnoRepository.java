package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.PerfilAlumno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PerfilAlumnoRepository extends JpaRepository<PerfilAlumno, Long> {

    boolean existsByDni(String dni);

    boolean existsByCodigoEstudiante(String codigoEstudiante);

    // NUEVO: Para obtener el perfil completo y dar mejor mensaje de error
    Optional<PerfilAlumno> findByDni(String dni);

    Optional<PerfilAlumno> findByCodigoEstudiante(String codigoEstudiante);
}
