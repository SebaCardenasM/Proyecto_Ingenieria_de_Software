package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Informe")
public class Informe extends Documento {

    private Double nota;                       // null = aún no evaluado

    @Column(columnDefinition = "TEXT")
    private String retroalimentacion;          // opcional

    private LocalDateTime fechaEvaluacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rut_profesor_evaluador", referencedColumnName = "rut")
    private Profesor profesorEvaluador;

    public Informe() {
        super();
        setTipoDocumento(TipoDocumento.INFORME_FINAL);
    }

    public boolean isEvaluado() { return nota != null; }

    public Double getNota() { return nota; }
    public void setNota(Double nota) { this.nota = nota; }

    public String getRetroalimentacion() { return retroalimentacion; }
    public void setRetroalimentacion(String retroalimentacion) { this.retroalimentacion = retroalimentacion; }

    public LocalDateTime getFechaEvaluacion() { return fechaEvaluacion; }
    public void setFechaEvaluacion(LocalDateTime fechaEvaluacion) { this.fechaEvaluacion = fechaEvaluacion; }

    public Profesor getProfesorEvaluador() { return profesorEvaluador; }
    public void setProfesorEvaluador(Profesor profesorEvaluador) { this.profesorEvaluador = profesorEvaluador; }
}