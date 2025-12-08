package com.proyecto.fundaciondeportiva.dto.request;

public class EventoRequest {

    private String tipo;
    private String detalles; // lo mandamos como String (JSON en texto)

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDetalles() {
        return detalles;
    }

    public void setDetalles(String detalles) {
        this.detalles = detalles;
    }
}
