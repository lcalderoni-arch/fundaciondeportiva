package com.proyecto.fundaciondeportiva.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "perfiles_alumno")
public class PerfilAlumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_estudiante", length = 20)
    private String codigoEstudiante;

    @Column(length = 15)
    private String dni;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelAcademico nivel;

    @Column(length = 20, nullable = false)
    private String grado;

    @Column(name = "telefono_emergencia", length = 9)
    private String telefonoEmergencia;

    @OneToOne(mappedBy = "perfilAlumno")
    @JsonBackReference          // ⬅️ IMPORTANTE: rompe el ciclo
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Usuario usuario;
}
