package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.ConfiguracionMatricula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfiguracionMatriculaRepository extends JpaRepository<ConfiguracionMatricula, Long> {
}
