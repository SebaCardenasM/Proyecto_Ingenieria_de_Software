package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import jakarta.persistence.*;

@Entity
@Table(name = "informes")
public class Informe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer numeroPractica;
    private String rutaPdf;

    @ManyToOne
    @JoinColumn(name = "estudiante_id")
    private Estudiante estudiante;

    @ManyToOne
    @JoinColumn(name = "profesor_practica_id")
    private Profesor profesorPractica;

    @ManyToOne
    @JoinColumn(name = "profesor_colaborador_id")
    private Profesor profesorColaborador;

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

    public String getRutaPdf() {
        return rutaPdf;
    }

    public void setRutaPdf(String rutaPdf) {
        this.rutaPdf = rutaPdf;
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