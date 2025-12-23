package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.PerfilProfesor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface PerfilProfesorRepository extends JpaRepository<PerfilProfesor, Long> {

    boolean existsByDni(String dni);

    // NUEVO: Para obtener el perfil completo y dar mejor mensaje de error
    Optional<PerfilProfesor> findByDni(String dni);
}
