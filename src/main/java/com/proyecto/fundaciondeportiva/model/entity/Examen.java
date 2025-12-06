package com.proyecto.fundaciondeportiva.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "examenes")
public class Examen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Siempre asociado a una sección
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seccion_id", nullable = false)
    @ToString.Exclude
    private Seccion seccion;

    @Column(length = 150, nullable = false)
    private String titulo;

    @Column(length = 500)
    private String descripcion;

    @Column
    private LocalDate fecha;   // día del examen (opcional)

    @Column(name = "peso_porcentual", nullable = false)
    private Double pesoPorcentual;   // ej: 20.0 = 20% de la nota final

    @Column(name = "nota_maxima", nullable = false)
    @Builder.Default
    private Double notaMaxima = 20.0;

    @Column
    private Integer orden; // para ordenar exámenes (1..10)

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @OneToMany(mappedBy = "examen", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private Set<NotaExamen> notas = new HashSet<>();
}
