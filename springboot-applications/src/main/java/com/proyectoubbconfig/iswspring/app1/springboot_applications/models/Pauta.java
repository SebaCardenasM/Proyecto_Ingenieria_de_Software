package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "pautas")
public class Pauta extends Documento {

    private Double puntajeObtenido;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    public Pauta() {
        super();
        setTipoDocumento(TipoDocumento.PAUTA);
    }

    // Getters y Setters
    public Double getPuntajeObtenido() {
        return puntajeObtenido;
    }

    public void setPuntajeObtenido(Double puntajeObtenido) {
        this.puntajeObtenido = puntajeObtenido;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}