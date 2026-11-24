package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dupla")
public class Dupla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer numeroPractica;

    @ManyToOne(optional = false)
    @JoinColumn(name = "rut_estudiante1", referencedColumnName = "rut")
    private Estudiante estudiante1;

    @ManyToOne(optional = false)
    @JoinColumn(name = "rut_estudiante2", referencedColumnName = "rut")
    private Estudiante estudiante2;

    @ManyToOne
    @JoinColumn(name = "practica1_id", referencedColumnName = "id")
    private Practica practica1;

    @ManyToOne
    @JoinColumn(name = "practica2_id", referencedColumnName = "id")
    private Practica practica2;

    @ManyToOne
    @JoinColumn(name = "rut_profesor", referencedColumnName = "rut")
    private Profesor profesor;

    private Integer ano;
    private Integer semestre;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public Dupla() {}

    public Dupla(Integer numeroPractica, Estudiante estudiante1, Estudiante estudiante2,
                 Practica practica1, Practica practica2, Profesor profesor,
                 Integer ano, Integer semestre) {
        this.numeroPractica = numeroPractica;
        this.estudiante1 = estudiante1;
        this.estudiante2 = estudiante2;
        this.practica1 = practica1;
        this.practica2 = practica2;
        this.profesor = profesor;
        this.ano = ano;
        this.semestre = semestre;
        this.fechaCreacion = LocalDateTime.now();
    }

    public boolean contieneEstudiante(String rut) {
        if (rut == null) return false;
        return (estudiante1 != null && rut.equalsIgnoreCase(estudiante1.getRut())) ||
               (estudiante2 != null && rut.equalsIgnoreCase(estudiante2.getRut()));
    }

    public Estudiante obtenerCompanero(String rut) {
        if (rut == null) return null;
        if (estudiante1 != null && rut.equalsIgnoreCase(estudiante1.getRut())) {
            return estudiante2;
        }
        if (estudiante2 != null && rut.equalsIgnoreCase(estudiante2.getRut())) {
            return estudiante1;
        }
        return null;
    }

    public boolean contienePractica(Long practicaId) {
        if (practicaId == null) return false;
        return (practica1 != null && practicaId.equals(practica1.getId())) ||
               (practica2 != null && practicaId.equals(practica2.getId()));
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getNumeroPractica() { return numeroPractica; }
    public void setNumeroPractica(Integer numeroPractica) { this.numeroPractica = numeroPractica; }

    public Estudiante getEstudiante1() { return estudiante1; }
    public void setEstudiante1(Estudiante estudiante1) { this.estudiante1 = estudiante1; }

    public Estudiante getEstudiante2() { return estudiante2; }
    public void setEstudiante2(Estudiante estudiante2) { this.estudiante2 = estudiante2; }

    public Practica getPractica1() { return practica1; }
    public void setPractica1(Practica practica1) { this.practica1 = practica1; }

    public Practica getPractica2() { return practica2; }
    public void setPractica2(Practica practica2) { this.practica2 = practica2; }

    public Profesor getProfesor() { return profesor; }
    public void setProfesor(Profesor profesor) { this.profesor = profesor; }

    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }

    public Integer getSemestre() { return semestre; }
    public void setSemestre(Integer semestre) { this.semestre = semestre; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
