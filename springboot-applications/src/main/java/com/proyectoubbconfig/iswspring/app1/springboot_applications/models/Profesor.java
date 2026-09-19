package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "profesores")
public class Profesor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "profesorCoordinador")
    private List<Estudiante> estudiantesCoordinados;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public List<Estudiante> getEstudiantesCoordinados() { return estudiantesCoordinados; }
    public void setEstudiantesCoordinados(List<Estudiante> estudiantesCoordinados) { this.estudiantesCoordinados = estudiantesCoordinados; }
}