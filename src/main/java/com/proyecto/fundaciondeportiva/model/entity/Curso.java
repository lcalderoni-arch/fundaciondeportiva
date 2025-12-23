package com.proyecto.fundaciondeportiva.model.entity;

import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter // Cambio de @Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cursos", uniqueConstraints = { @UniqueConstraint(columnNames = "codigo") })
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String codigo;

    @Column(length = 100, nullable = false)
    private String titulo;

    @Lob
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_destino", nullable = false)
    private NivelAcademico nivelDestino;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false, nullable = false)
    private LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por", nullable = false)
    @ToString.Exclude // Evitar ciclo con Usuario
    private Usuario creadoPor;

    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude // Evitar ciclo con Seccion
    private Set<Seccion> secciones;
}
