package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import jakarta.persistence.*;

@Entity
@Table(name = "Coordinador")
@PrimaryKeyJoinColumn(name = "rut")
public class Coordinador extends Usuario {

    public Coordinador() {
        super(Rol.COORDINADOR);
    }

    public Coordinador(Rol... rolesAdicionales) {
        super(Rol.COORDINADOR);
        if (rolesAdicionales != null) {
            for (Rol r : rolesAdicionales) {
                addRol(r);
            }
        }
    }

}