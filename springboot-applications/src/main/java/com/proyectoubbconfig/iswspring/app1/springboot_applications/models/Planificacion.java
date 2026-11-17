package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Planificacion")
public class Planificacion extends Documento {

    // Boolean (no boolean): las planificaciones ya existentes quedan en null = "pendiente"
    private Boolean revisada = false;

    private LocalDateTime fechaRevision;

    // Profesor UBB que confirmó (hizo el "tick")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rut_profesor_revisor", referencedColumnName = "rut")
    private Profesor profesorRevisor;

    public Planificacion() {
        super();
        setTipoDocumento(TipoDocumento.PLANIFICACION);
    }

    public boolean isRevisada() { return Boolean.TRUE.equals(revisada); }
    public void setRevisada(Boolean revisada) { this.revisada = revisada; }

    public LocalDateTime getFechaRevision() { return fechaRevision; }
    public void setFechaRevision(LocalDateTime fechaRevision) { this.fechaRevision = fechaRevision; }

    public Profesor getProfesorRevisor() { return profesorRevisor; }
    public void setProfesorRevisor(Profesor profesorRevisor) { this.profesorRevisor = profesorRevisor; }
}
