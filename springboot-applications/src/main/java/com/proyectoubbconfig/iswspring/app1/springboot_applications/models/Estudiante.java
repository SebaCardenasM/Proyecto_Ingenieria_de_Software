package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "estudiantes")
@PrimaryKeyJoinColumn(name = "rut")
public class Estudiante extends Usuario {

    private Integer anoIngreso;
    private Integer practicaActual;

    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL)
    private List<Practica> practicas;

    @ManyToOne
    @JoinColumn(name = "profesor_coordinador_rut")
    private Profesor profesorCoordinador;

    public Estudiante() {
        super(Rol.ESTUDIANTE);
    }

    // Getters y Setters
    public Integer getAnoIngreso() { return anoIngreso; }
    public void setAnoIngreso(Integer anoIngreso) { this.anoIngreso = anoIngreso; }

    public Integer getPracticaActual() { return practicaActual; }
    public void setPracticaActual(Integer practicaActual) { this.practicaActual = practicaActual; }

    public List<Practica> getPracticas() { return practicas; }
    public void setPracticas(List<Practica> practicas) { this.practicas = practicas; }

    public Profesor getProfesorCoordinador() { return profesorCoordinador; }
    public void setProfesorCoordinador(Profesor profesorCoordinador) { this.profesorCoordinador = profesorCoordinador; }
}