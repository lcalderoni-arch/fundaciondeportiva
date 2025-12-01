package com.proyecto.fundaciondeportiva.model.entity;

import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
import com.proyecto.fundaciondeportiva.model.enums.Turno;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entidad 'secciones'.
 * Representa una instancia real de un curso en un periodo específico.
 */
@Getter // Reemplaza @Data
@Setter // Reemplaza @Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "secciones", uniqueConstraints = {
        @UniqueConstraint(columnNames = "codigo")
})
public class Seccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String codigo;

    @Column(length = 100, nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_seccion", nullable = false)
    private NivelAcademico nivelSeccion;

    @Column(name = "grado_seccion", length = 20, nullable = false)
    private String gradoSeccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Turno turno;

    @Column(length = 50)
    private String aula;

    @Column(nullable = false)
    @Builder.Default
    private Integer capacidad = 30;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "numero_semanas", nullable = false)
    @Builder.Default
    private Integer numeroSemanas = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activa = true;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false, nullable = false)
    private LocalDateTime fechaCreacion;

    // --- Relaciones ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    @ToString.Exclude // 👈 ¡ESTO EVITA EL ERROR 500!
    private Curso curso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesor_id", nullable = false)
    @ToString.Exclude // 👈 ¡ESTO TAMBIÉN!
    private Usuario profesor;

    @OneToMany(mappedBy = "seccion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude // 👈 IMPORTANTE EN LISTAS
    private Set<Matricula> matriculas = new HashSet<>();

    @OneToMany(mappedBy = "seccion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private Set<Sesion> sesiones = new HashSet<>();

    @OneToMany(mappedBy = "seccion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numero ASC")
    @Builder.Default
    @ToString.Exclude
    private List<SemanaSemana> semanas = new ArrayList<>();

    // --- Métodos de utilidad existentes ---

    public int getNumeroEstudiantesMatriculados() {
        return matriculas != null ? matriculas.size() : 0;
    }

    public boolean tieneCupoDisponible() {
        return getNumeroEstudiantesMatriculados() < capacidad;
    }

    public boolean estaEnPeriodoActivo() {
        LocalDate hoy = LocalDate.now();
        return !hoy.isBefore(fechaInicio) && !hoy.isAfter(fechaFin);
    }

    public void agregarSemana(SemanaSemana semana) {
        semanas.add(semana);
        semana.setSeccion(this);
    }

    public void generarSemanas() {
        this.semanas.clear();
        LocalDate fechaActual = this.fechaInicio;

        for (int i = 1; i <= this.numeroSemanas; i++) {
            SemanaSemana semana = SemanaSemana.builder()
                    .numero(i)
                    .fechaInicio(fechaActual)
                    .fechaFin(fechaActual.plusDays(6)) // 7 días = 1 semana
                    .activa(true)
                    .descripcion("Semana " + i + " del curso")
                    .build();

            this.agregarSemana(semana);
            fechaActual = fechaActual.plusWeeks(1);
        }
    }

    public SemanaSemana getSemanaActual() {
        LocalDate hoy = LocalDate.now();
        if (semanas == null) return null; // Validación extra por seguridad

        return semanas.stream()
                .filter(s -> !hoy.isBefore(s.getFechaInicio()) && !hoy.isAfter(s.getFechaFin()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Obtiene el número de la semana actual (0 si no está en periodo)
     */
    public Integer getNumeroSemanaActual() {
        SemanaSemana semanaActual = getSemanaActual();
        return semanaActual != null ? semanaActual.getNumero() : 0;
    }

}