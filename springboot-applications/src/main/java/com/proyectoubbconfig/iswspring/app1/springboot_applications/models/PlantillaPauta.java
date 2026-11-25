package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "plantilla_pauta")
public class PlantillaPauta {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pauta", length = 50, nullable = false)
    private TipoPauta tipoPauta;

    @Column(name = "titulo", length = 255, nullable = false)
    private String titulo;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Lob
    @Column(name = "esquema_json", columnDefinition = "MEDIUMTEXT", nullable = false)
    private String esquemaJson;

    @Lob
    @Column(name = "esquema_borrador_json", columnDefinition = "MEDIUMTEXT")
    private String esquemaBorradorJson;

    @Column(name = "tiene_borrador", nullable = false)
    private boolean tieneBorrador = false;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "actualizado_por", length = 100)
    private String actualizadoPor;

    public PlantillaPauta() {}

    public PlantillaPauta(TipoPauta tipoPauta, String titulo, String descripcion, String esquemaJson, String actualizadoPor) {
        this.tipoPauta = tipoPauta;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.esquemaJson = esquemaJson;
        this.actualizadoPor = actualizadoPor;
        this.fechaActualizacion = LocalDateTime.now();
        this.tieneBorrador = false;
    }

    public TipoPauta getTipoPauta() {
        return tipoPauta;
    }

    public void setTipoPauta(TipoPauta tipoPauta) {
        this.tipoPauta = tipoPauta;
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

    public String getEsquemaJson() {
        return esquemaJson;
    }

    public void setEsquemaJson(String esquemaJson) {
        this.esquemaJson = esquemaJson;
    }

    public String getEsquemaBorradorJson() {
        return esquemaBorradorJson;
    }

    public void setEsquemaBorradorJson(String esquemaBorradorJson) {
        this.esquemaBorradorJson = esquemaBorradorJson;
    }

    public boolean isTieneBorrador() {
        return tieneBorrador;
    }

    public void setTieneBorrador(boolean tieneBorrador) {
        this.tieneBorrador = tieneBorrador;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getActualizadoPor() {
        return actualizadoPor;
    }

    public void setActualizadoPor(String actualizadoPor) {
        this.actualizadoPor = actualizadoPor;
    }
}
