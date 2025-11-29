package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.SemanaSemana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SemanaSemanaRepository extends JpaRepository<SemanaSemana, Long> {

    List<SemanaSemana> findBySeccionIdOrderByNumeroAsc(Long seccionId);

    List<SemanaSemana> findBySeccionIdAndActivaTrueOrderByNumeroAsc(Long seccionId);

    Optional<SemanaSemana> findBySeccionIdAndNumero(Long seccionId, Integer numero);

    @Query("SELECT s FROM SemanaSemana s WHERE s.seccion.id = :seccionId " +
            "AND s.activa = true AND :fecha BETWEEN s.fechaInicio AND s.fechaFin")
    Optional<SemanaSemana> findSemanaActualPorSeccion(
            @Param("seccionId") Long seccionId,
            @Param("fecha") LocalDate fecha
    );

    @Query("SELECT s FROM SemanaSemana s WHERE s.seccion.id = :seccionId " +
            "AND s.fechaInicio > :fecha ORDER BY s.numero ASC")
    List<SemanaSemana> findSemanasFuturas(
            @Param("seccionId") Long seccionId,
            @Param("fecha") LocalDate fecha
    );

    @Query("SELECT s FROM SemanaSemana s WHERE s.seccion.id = :seccionId " +
            "AND s.fechaFin < :fecha ORDER BY s.numero DESC")
    List<SemanaSemana> findSemanasPasadas(
            @Param("seccionId") Long seccionId,
            @Param("fecha") LocalDate fecha
    );
}