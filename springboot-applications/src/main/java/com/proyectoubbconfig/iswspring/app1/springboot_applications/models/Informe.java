package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "informes")
public class Informe extends Documento {

    public Informe() {
        super();
        setTipoDocumento(TipoDocumento.INFORME_FINAL);
    }

    // Aquí puedes agregar campos específicos de un Informe si los necesitas a futuro
}