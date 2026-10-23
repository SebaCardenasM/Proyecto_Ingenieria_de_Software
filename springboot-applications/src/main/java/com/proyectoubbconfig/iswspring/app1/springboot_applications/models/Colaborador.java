package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "colaborador")
public class Colaborador {

    @Id
    @Column(length = 12)
    private String rut;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    private String correo;
    private String telefono;

    @OneToMany(mappedBy = "colaborador")
    private List<Practica> practicas;

    public Colaborador() {}

    // Getters y Setters
    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    
    public List<Practica> getPracticas() { return practicas; }
    public void setPracticas(List<Practica> practicas) { this.practicas = practicas; }
}