package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "Usuario")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Usuario {

    @Id
    @Column(length = 12, nullable = false, updatable = false)
    private String rut;

    private String nombre;
    private String apellido;

    @Column(unique = true, nullable = false)
    private String correo;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    // Relación de un usuario con múltiples roles
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "usuario_rut"))
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private Set<Rol> roles = new HashSet<>();

    public Usuario() {}

    public Usuario(Set<Rol> roles) {
        if (roles != null) {
            this.roles = roles;
        }
    }

    public Usuario(Rol... roles) {
        if (roles != null) {
            for (Rol r : roles) {
                this.roles.add(r);
            }
        }
    }

    // Helpers de Roles
    public void addRol(Rol rol) {
        this.roles.add(rol);
    }

    public boolean hasRol(Rol rol) {
        return this.roles.contains(rol);
    }

    // Getters y Setters
    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Set<Rol> getRoles() { return roles; }
    public void setRoles(Set<Rol> roles) { this.roles = roles; }
}