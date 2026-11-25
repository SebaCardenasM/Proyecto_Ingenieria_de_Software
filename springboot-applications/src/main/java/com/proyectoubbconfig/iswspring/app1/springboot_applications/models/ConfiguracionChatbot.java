package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "configuracion_chatbot")
public class ConfiguracionChatbot {

    @Id
    @Column(name = "clave", length = 100, nullable = false)
    private String clave;

    @Lob
    @Column(name = "contenido", columnDefinition = "MEDIUMTEXT", nullable = false)
    private String contenido;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "actualizado_por", length = 100)
    private String actualizadoPor;

    public ConfiguracionChatbot() {}

    public ConfiguracionChatbot(String clave, String contenido, String descripcion, String actualizadoPor) {
        this.clave = clave;
        this.contenido = contenido;
        this.descripcion = descripcion;
        this.actualizadoPor = actualizadoPor;
        this.fechaActualizacion = LocalDateTime.now();
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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
