package com.proyectoubbconfig.iswspring.app1.springboot_applications.dto;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Rol;

import java.util.HashSet;
import java.util.Set;

public class UsuarioEdicionDTO {

    private String nombre;
    private String apellido;
    private String correo;
    private String password; // opcional: vacío = no se cambia

    // Solo estudiantes
    private Integer anoIngreso;
    private Integer practicaActual;
    private String profesorCoordinadorRut;

    // Solo profesores
    private Set<Rol> roles = new HashSet<>();

    public UsuarioEdicionDTO() {}

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Integer getAnoIngreso() { return anoIngreso; }
    public void setAnoIngreso(Integer anoIngreso) { this.anoIngreso = anoIngreso; }

    public Integer getPracticaActual() { return practicaActual; }
    public void setPracticaActual(Integer practicaActual) { this.practicaActual = practicaActual; }

    public String getProfesorCoordinadorRut() { return profesorCoordinadorRut; }
    public void setProfesorCoordinadorRut(String profesorCoordinadorRut) { this.profesorCoordinadorRut = profesorCoordinadorRut; }

    public Set<Rol> getRoles() { return roles; }
    public void setRoles(Set<Rol> roles) { this.roles = roles; }
}