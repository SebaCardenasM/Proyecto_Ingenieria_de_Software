package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

public enum EstadoDocumento {
    BORRADOR,           // solo lo ve el estudiante
    EN_REVISION,        // enviada al profesor de la asignatura
    CON_OBSERVACIONES,  // el profesor comentó; el estudiante debe corregir
    ENVIADO,            // en el gestor (informes)
    AUTORIZADO          // planificación autorizada: bloqueada y permanente
}