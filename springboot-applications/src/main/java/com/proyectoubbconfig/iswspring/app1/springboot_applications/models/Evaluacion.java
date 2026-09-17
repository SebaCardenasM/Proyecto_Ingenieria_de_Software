package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluaciones")
public class Evaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Funcionalidad 5: Puntaje de la rúbrica
    private Double puntajeObtenido;

    // Funcionalidad 6: Retroalimentación cualitativa (Textarea)
    @Column(columnDefinition = "TEXT")
    private String observaciones;

    private LocalDateTime fechaEvaluacion;

    @ManyToOne
    @JoinColumn(name = "practica_id")
    private Practica practica;

    @ManyToOne
    @JoinColumn(name = "profesor_evaluador_id")
    private Profesor profesorEvaluador;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getFechaEvaluacion() {
        return fechaEvaluacion;
    }

    public void setFechaEvaluacion(LocalDateTime fechaEvaluacion) {
        this.fechaEvaluacion = fechaEvaluacion;
    }

    public Practica getPractica() {
        return practica;
    }

    public void setPractica(Practica practica) {
        this.practica = practica;
    }

    public Profesor getProfesorEvaluador() {
        return profesorEvaluador;
    }

    public void setProfesorEvaluador(Profesor profesorEvaluador) {
        this.profesorEvaluador = profesorEvaluador;
    }

    

    
}