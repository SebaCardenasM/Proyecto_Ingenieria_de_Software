package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "profesores")
@PrimaryKeyJoinColumn(name = "rut")
public class Profesor extends Usuario {

    @OneToMany(mappedBy = "profesorCoordinador")
    private List<Estudiante> estudiantesCoordinados;

    public Profesor() {
        super(Rol.PROFESOR);
    }

    // Getters y Setters
    public List<Estudiante> getEstudiantesCoordinados() { return estudiantesCoordinados; }
    public void setEstudiantesCoordinados(List<Estudiante> estudiantesCoordinados) { this.estudiantesCoordinados = estudiantesCoordinados; }
}