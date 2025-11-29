package com.proyecto.fundaciondeportiva.model.entity;

import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
import com.proyecto.fundaciondeportiva.model.enums.Turno;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@Data
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

    // ⭐ NUEVO CAMPO: Número de semanas académicas
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
    private Curso curso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesor_id", nullable = false)
    private Usuario profesor;

    @OneToMany(mappedBy = "seccion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Matricula> matriculas = new HashSet<>();

    @OneToMany(mappedBy = "seccion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Sesion> sesiones = new HashSet<>();

    // ⭐ NUEVA RELACIÓN: One-to-Many con SemanaSemana
    @OneToMany(mappedBy = "seccion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numero ASC")
    @Builder.Default
    private List<SemanaSemana> semanas = new ArrayList<>();

    // --- Métodos de utilidad existentes ---

    /**
     * Retorna el número actual de estudiantes matriculados
     */
    public int getNumeroEstudiantesMatriculados() {
        return matriculas != null ? matriculas.size() : 0;
    }

    /**
     * Verifica si la sección tiene cupo disponible
     */
    public boolean tieneCupoDisponible() {
        return getNumeroEstudiantesMatriculados() < capacidad;
    }

    /**
     * Verifica si la sección está en el periodo activo
     */
    public boolean estaEnPeriodoActivo() {
        LocalDate hoy = LocalDate.now();
        return !hoy.isBefore(fechaInicio) && !hoy.isAfter(fechaFin);
    }

    // ⭐ NUEVOS MÉTODOS: Para gestionar semanas

    /**
     * Agrega una semana a la sección
     */
    public void agregarSemana(SemanaSemana semana) {
        semanas.add(semana);
        semana.setSeccion(this);
    }

    /**
     * Genera automáticamente las semanas académicas basándose en:
     * - fechaInicio
     * - numeroSemanas
     */
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
            fechaActual = fechaActual.plusWeeks(1); // Avanzar a la siguiente semana
        }
    }

    /**
     * Obtiene la semana actual basándose en la fecha de hoy
     */
    public SemanaSemana getSemanaActual() {
        LocalDate hoy = LocalDate.now();
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