package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "Profesor")
@PrimaryKeyJoinColumn(name = "rut")
public class Profesor extends Usuario {

    @OneToMany(mappedBy = "profesorCoordinador")
    private List<Estudiante> estudiantesCoordinados;

    private String departamento = "Departamento de Ciencias de la Educación";
    private String gradoAcademico;
    private String oficina;
    private String horarioAtencion;

    public Profesor() {
        super(Rol.PROFESOR_ASIGNATURA);
    }

    public Profesor(Rol... rolesAdicionales) {
        super(Rol.PROFESOR_ASIGNATURA);
        if (rolesAdicionales != null) {
            for (Rol r : rolesAdicionales) {
                addRol(r);
            }
        }
    }

    // Getters y Setters
    public List<Estudiante> getEstudiantesCoordinados() { return estudiantesCoordinados; }
    public void setEstudiantesCoordinados(List<Estudiante> estudiantesCoordinados) { this.estudiantesCoordinados = estudiantesCoordinados; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public String getGradoAcademico() { return gradoAcademico; }
    public void setGradoAcademico(String gradoAcademico) { this.gradoAcademico = gradoAcademico; }

    public String getOficina() { return oficina; }
    public void setOficina(String oficina) { this.oficina = oficina; }

    public String getHorarioAtencion() { return horarioAtencion; }
    public void setHorarioAtencion(String horarioAtencion) { this.horarioAtencion = horarioAtencion; }
}