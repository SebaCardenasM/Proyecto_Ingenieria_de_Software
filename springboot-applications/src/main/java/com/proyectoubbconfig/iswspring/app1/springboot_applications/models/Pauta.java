package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Pauta")
public class Pauta extends Documento {

    private Double puntajeObtenido;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evaluador")
    private TipoEvaluador tipoEvaluador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rut_profesor_evaluador", referencedColumnName = "rut")
    private Profesor profesorEvaluador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rut_colaborador_evaluador", referencedColumnName = "rut")
    private Colaborador colaboradorEvaluador;

    public Pauta() {
        super();
        setTipoDocumento(TipoDocumento.PAUTA);
    }

    public String getRutEvaluador() {
        if (this.tipoEvaluador == TipoEvaluador.TUTOR_UNIVERSIDAD && this.profesorEvaluador != null) {
            return this.profesorEvaluador.getRut();
        } else if (this.tipoEvaluador == TipoEvaluador.PROFESOR_COLABORADOR && this.colaboradorEvaluador != null) {
            return this.colaboradorEvaluador.getRut();
        }
        return null;
    }

    public String getNombreEvaluador() {
        if (this.tipoEvaluador == TipoEvaluador.TUTOR_UNIVERSIDAD && this.profesorEvaluador != null) {
            return this.profesorEvaluador.getNombre() + " " + this.profesorEvaluador.getApellido();
        } else if (this.tipoEvaluador == TipoEvaluador.PROFESOR_COLABORADOR && this.colaboradorEvaluador != null) {
            return this.colaboradorEvaluador.getNombre() + " " + this.colaboradorEvaluador.getApellido();
        }
        return "Desconocido";
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

    public TipoEvaluador getTipoEvaluador() {
        return tipoEvaluador;
    }
    public void setTipoEvaluador(TipoEvaluador tipoEvaluador) {
        this.tipoEvaluador = tipoEvaluador;
    }

    public Profesor getProfesorEvaluador() {
        return profesorEvaluador;
    }
    public void setProfesorEvaluador(Profesor profesorEvaluador) {
        this.profesorEvaluador = profesorEvaluador;
    }

    public Colaborador getColaboradorEvaluador() {
        return colaboradorEvaluador;
    }
    public void setColaboradorEvaluador(Colaborador colaboradorEvaluador) {
        this.colaboradorEvaluador = colaboradorEvaluador;
    }
}