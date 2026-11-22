package com.proyectoubbconfig.iswspring.app1.springboot_applications.dto;

import java.time.LocalDateTime;

/** Datos ya resueltos (sin proxies lazy) que necesita el formulario del profesor colaborador. */
public record DatosPautaColaborador(
        String estudianteNombre,
        String estudianteRut,
        String establecimiento,
        Integer numeroPractica,
        String evaluadorNombre,       // null si la práctica aún no tiene colaborador asignado
        boolean evaluadorConocido,
        LocalDateTime enlaceExpira    // null en modo solo lectura
) {}
