package com.proyecto.fundaciondeportiva.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.proyecto.fundaciondeportiva.model.enums.EstadoMatricula;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "matriculas", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"alumno_id", "seccion_id", "ciclo"})
})
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id", nullable = false)
    @JsonIgnoreProperties({"matriculas", "seccionesAsignadas", "cursosCreados", "password"})
    private Usuario alumno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seccion_id", nullable = false)
    @JsonBackReference("seccion-matriculas")
    private Seccion seccion;

    // ✅ NUEVO: separa histórico por ciclo
    @Column(nullable = false, length = 20)
    private String ciclo; // ej: "2025-II"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoMatricula estado = EstadoMatricula.ACTIVA;

    @CreationTimestamp
    @Column(name = "fecha_matricula", updatable = false, nullable = false)
    private LocalDateTime fechaMatricula;

    @Column(name = "fecha_retiro")
    private LocalDateTime fechaRetiro;

    @Column(name = "calificacion_final")
    private Double calificacionFinal;

    @Column(length = 500)
    private String observaciones;

    @OneToMany(mappedBy = "matricula", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Asistencia> asistencias;
}
