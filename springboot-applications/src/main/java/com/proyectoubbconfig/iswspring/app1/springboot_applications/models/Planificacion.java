package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "planificaciones")
public class Planificacion extends Documento {

    public Planificacion() {
        super();
        setTipoDocumento(TipoDocumento.PLANIFICACION);
    }

    // Aquí puedes agregar campos específicos de una Planificación si los necesitas a futuro
}