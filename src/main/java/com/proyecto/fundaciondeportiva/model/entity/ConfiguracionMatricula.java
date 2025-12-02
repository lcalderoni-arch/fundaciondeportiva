package com.proyecto.fundaciondeportiva.model.entity;

import jakarta.persistence.*;
import lombok.*;

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
    @Column(name = "matricula_habilitada", nullable = false)
    private boolean matriculaHabilitada = true;
}
