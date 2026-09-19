package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "estudiantes")
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer anoIngreso;
    private Integer practicaActual;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL)
    private List<Practica> practicas;

    @ManyToOne
    @JoinColumn(name = "profesor_coordinador_id")
    private Profesor profesorCoordinador;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getAnoIngreso() { return anoIngreso; }
    public void setAnoIngreso(Integer anoIngreso) { this.anoIngreso = anoIngreso; }

    public Integer getPracticaActual() { return practicaActual; }
    public void setPracticaActual(Integer practicaActual) { this.practicaActual = practicaActual; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public List<Practica> getPracticas() { return practicas; }
    public void setPracticas(List<Practica> practicas) { this.practicas = practicas; }

    public Profesor getProfesorCoordinador() { return profesorCoordinador; }
    public void setProfesorCoordinador(Profesor profesorCoordinador) { this.profesorCoordinador = profesorCoordinador; }
}