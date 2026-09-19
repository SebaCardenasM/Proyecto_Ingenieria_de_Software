package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "archivos_practica")
public class ArchivoPractica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombreArchivo;

    @Column(nullable = false)
    private String rutaServidor; // URL o ruta local donde se guardó el PDF/Docx

    private LocalDateTime fechaSubida;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_rut", nullable = false)
    private Usuario estudiante;

    // Constructores, Getters y Setters
    public ArchivoPractica() {}

    @PrePersist
    protected void onCreate() {
        this.fechaSubida = LocalDateTime.now();
    }
    
    public Usuario getEstudiante() {
        return estudiante;
    }
    
    public Long getId() {
        return id;
    }
    public String getNombreArchivo() {
        return nombreArchivo;
    }
    public String getRutaServidor() {
        return rutaServidor;
    }
    public LocalDateTime getFechaSubida() {
        return fechaSubida;
    }
    public void setEstudiante(Usuario estudiante) {
        this.estudiante = estudiante;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }
    public void setRutaServidor(String rutaServidor) {
        this.rutaServidor = rutaServidor;
    }

    // ... (Agregar getters y setters)
}