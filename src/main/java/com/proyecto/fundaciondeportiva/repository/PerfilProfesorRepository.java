package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.PerfilProfesor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface PerfilProfesorRepository extends JpaRepository<PerfilProfesor, Long> {

    Optional<PerfilProfesor> findByDni(String dni);

    // AÑADIDO: Método para verificar unicidad del DNI
    boolean existsByDni(String dni);
}