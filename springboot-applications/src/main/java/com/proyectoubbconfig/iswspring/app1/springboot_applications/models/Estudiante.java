package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "estudiantes")
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String rut;

    private String nombre;
    private String apellido;
    private Integer anoIngreso;
    private Integer practicaActual; // Guarda el número del 1 al 6


    // Relación 1:N con Practica
    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL)
    private List<Practica> practicas;

    // Relación N:1 con Profesor (Coordina)
    @ManyToOne
    @JoinColumn(name = "profesor_coordinador_id")
    private Profesor profesorCoordinador;

    // Relación 1:N con Planificacion y 1:N con Informe (Realiza)
    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL)
    private List<Planificacion> planificaciones;

    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL)
    private List<Informe> informes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public Integer getAnoIngreso() {
        return anoIngreso;
    }

    public void setAnoIngreso(Integer anoIngreso) {
        this.anoIngreso = anoIngreso;
    }

    public Integer getPracticaActual() {
        return practicaActual;
    }

    public void setPracticaActual(Integer practicaActual) {
        this.practicaActual = practicaActual;
    }

    public List<Practica> getPracticas() {
        return practicas;
    }

    public void setPracticas(List<Practica> practicas) {
        this.practicas = practicas;
    }

    public Profesor getProfesorCoordinador() {
        return profesorCoordinador;
    }

    public void setProfesorCoordinador(Profesor profesorCoordinador) {
        this.profesorCoordinador = profesorCoordinador;
    }

    public List<Planificacion> getPlanificaciones() {
        return planificaciones;
    }

    public void setPlanificaciones(List<Planificacion> planificaciones) {
        this.planificaciones = planificaciones;
    }

    public List<Informe> getInformes() {
        return informes;
    }

    public void setInformes(List<Informe> informes) {
        this.informes = informes;
    }

    

    
}