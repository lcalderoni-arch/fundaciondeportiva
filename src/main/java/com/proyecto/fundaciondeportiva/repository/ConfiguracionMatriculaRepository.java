// src/main/java/com/proyecto/fundaciondeportiva/repository/ConfiguracionMatriculaRepository.java
package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.ConfiguracionMatricula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfiguracionMatriculaRepository extends JpaRepository<ConfiguracionMatricula, Long> {

    Optional<ConfiguracionMatricula> findFirstByOrderByIdAsc();
}
