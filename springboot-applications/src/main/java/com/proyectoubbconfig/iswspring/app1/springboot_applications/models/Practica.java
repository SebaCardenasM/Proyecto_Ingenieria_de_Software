package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import jakarta.persistence.*;

@Entity
@Table(name = "practicas")
public class Practica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer ano;
    private Integer semestre; // 1 o 2
    private Integer numeroPractica;
    private Boolean aprobacion;
    private Double notaAprobacion;

    // Relación N:1 con Estudiante
    @ManyToOne
    @JoinColumn(name = "estudiante_id")
    private Estudiante estudiante;

    // Relación N:1 con Profesor
    @ManyToOne
    @JoinColumn(name = "profesor_id")
    private Profesor profesor;

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
  
    public Integer getAno() {
        return ano;
    }
    public void setAno(Integer ano) {
        this.ano = ano;
    }
    public Integer getSemestre() {
        return semestre;
    }
    public void setSemestre(Integer semestre) {
        this.semestre = semestre;
    }
    public Integer getNumeroPractica() {
        return numeroPractica;
    }
    public void setNumeroPractica(Integer numeroPractica) {
        this.numeroPractica = numeroPractica;
    }
    public Boolean getAprobacion() {
        return aprobacion;
    }
    public void setAprobacion(Boolean aprobacion) {
        this.aprobacion = aprobacion;
    }
    public Double getNotaAprobacion() {
        return notaAprobacion;
    }
    public void setNotaAprobacion(Double notaAprobacion) {
        this.notaAprobacion = notaAprobacion;
    }
    public Estudiante getEstudiante() {
        return estudiante;
    }
    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }
    public Profesor getProfesor() {
        return profesor;
    }
    public void setProfesor(Profesor profesor) {
        this.profesor = profesor;
    }

    
}