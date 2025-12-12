package com.proyecto.fundaciondeportiva.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "configuracion_matricula")
public class ConfiguracionMatricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // true = se permite matricular / false = matrículas bloqueadas
    @Builder.Default
    @Column(name = "matricula_habilitada", nullable = false)
    private boolean matriculaHabilitada = true;

    // ✅ NUEVO
    @Column(name="ciclo_actual", nullable = false, length = 20)
    private String cicloActual = "2025-II";

    // ⭐ NUEVO: rango de fechas de matrícula (opcional)
    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;
}
