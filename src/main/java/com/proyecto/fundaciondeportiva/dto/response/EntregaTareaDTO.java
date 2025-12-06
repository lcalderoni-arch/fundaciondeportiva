package com.proyecto.fundaciondeportiva.dto.response;

import java.time.LocalDateTime;

public class EntregaTareaDTO {

    private Long id;
    private Long recursoId;
    private Long alumnoId;

    private String titulo;
    private String descripcion;
    private String archivoUrl;

    private LocalDateTime fechaEntrega;

    private Double nota;
    private String retroalimentacion;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Long getRecursoId() {
        return recursoId;
    }
    public void setRecursoId(Long recursoId) {
        this.recursoId = recursoId;
    }

    public Long getAlumnoId() {
        return alumnoId;
    }
    public void setAlumnoId(Long alumnoId) {
        this.alumnoId = alumnoId;
    }

    public String getTitulo() {
        return titulo;
    }
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getArchivoUrl() {
        return archivoUrl;
    }
    public void setArchivoUrl(String archivoUrl) {
        this.archivoUrl = archivoUrl;
    }

    public LocalDateTime getFechaEntrega() {
        return fechaEntrega;
    }
    public void setFechaEntrega(LocalDateTime fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public Double getNota() {
        return nota;
    }
    public void setNota(Double nota) {
        this.nota = nota;
    }

    public String getRetroalimentacion() {
        return retroalimentacion;
    }
    public void setRetroalimentacion(String retroalimentacion) {
        this.retroalimentacion = retroalimentacion;
    }
}