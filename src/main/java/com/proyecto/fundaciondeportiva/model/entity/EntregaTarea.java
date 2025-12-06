package com.proyecto.fundaciondeportiva.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "entregas_tarea",
        uniqueConstraints = @UniqueConstraint(columnNames = {"recurso_id", "alumno_id"}))
@Getter
@Setter
public class EntregaTarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Recurso recurso;

    @ManyToOne(optional = false)
    private Usuario alumno;

    private String titulo;
    private String descripcion;

    private String archivoUrl;

    private LocalDateTime fechaEntrega;

    private Double nota;
    private String retroalimentacion;
}