package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import jakarta.persistence.*;

@Entity
@Table(name = "Coordinador")
@PrimaryKeyJoinColumn(name = "rut")
public class Coordinador extends Usuario {

    public Coordinador() {
        super(Rol.COORDINADOR);
    }

    // Getters y Setters
}