package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "informes")
public class Informe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer numeroPractica;
    private String rutaArchivo; // Ahora acepta rutas de Word, PPT, JPG, etc.
    private String tipoDocumento; // Ej: "Planificación", "Guía de Aprendizaje"
    private LocalDateTime fechaSubida;

    // --- AQUÍ ESTÁ LA RELACIÓN FALTANTE QUE SOLUCIONA EL ERROR ---
    @ManyToOne
    @JoinColumn(name = "practica_id")
    private Practica practica;

    @ManyToOne
    @JoinColumn(name = "estudiante_id")
    private Estudiante estudiante;

    @ManyToOne
    @JoinColumn(name = "profesor_practica_id")
    private Profesor profesorPractica;

    @ManyToOne
    @JoinColumn(name = "profesor_colaborador_id")
    private Profesor profesorColaborador;

    // ==========================================
    // GETTERS Y SETTERS
    // ==========================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumeroPractica() {
        return numeroPractica;
    }

    public void setNumeroPractica(Integer numeroPractica) {
        this.numeroPractica = numeroPractica;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public LocalDateTime getFechaSubida() {
        return fechaSubida;
    }

    public void setFechaSubida(LocalDateTime fechaSubida) {
        this.fechaSubida = fechaSubida;
    }

    // --- SETTER Y GETTER DE PRACTICA AÑADIDOS ---
    public Practica getPractica() {
        return practica;
    }

    public void setPractica(Practica practica) {
        this.practica = practica;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    public Profesor getProfesorPractica() {
        return profesorPractica;
    }

    public void setProfesorPractica(Profesor profesorPractica) {
        this.profesorPractica = profesorPractica;
    }

    public Profesor getProfesorColaborador() {
        return profesorColaborador;
    }

    public void setProfesorColaborador(Profesor profesorColaborador) {
        this.profesorColaborador = profesorColaborador;
    }
}