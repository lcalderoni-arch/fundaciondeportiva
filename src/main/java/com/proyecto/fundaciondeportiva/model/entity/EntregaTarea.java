package com.proyecto.fundaciondeportiva.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "entregas_tarea",
        uniqueConstraints = @UniqueConstraint(columnNames = {"recurso_id", "alumno_id"}))
public class EntregaTarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recurso_id")
    private Recurso recurso;

    @ManyToOne(optional = false)
    @JoinColumn(name = "alumno_id")
    private Usuario alumno;

    private String titulo;
    private String descripcion;
    private String archivoUrl;
    private LocalDateTime fechaEntrega;

    private Double nota;
    private String retroalimentacion;

    // getters/setters...
}