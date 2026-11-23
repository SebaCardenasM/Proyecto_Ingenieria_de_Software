package com.proyectoubbconfig.iswspring.app1.springboot_applications.dto;

import java.time.LocalDateTime;

/** Una fila del listado de planificaciones que revisa el profesor UBB. */
public record FilaPlanificacion(
        Long id,
        String nombreArchivo,
        String rutaServidor,
        LocalDateTime fechaSubida,
        String estudianteNombre,
        String estudianteRut,
        Integer numeroPractica,
        Integer ano,
        Integer semestre,
        String centro,
        boolean revisada,
        LocalDateTime fechaRevision,
        boolean evaluaConPautas,      // NUEVO: tick 2
        String estadoEnlace,          // NINGUNO | VIGENTE | EXPIRADO | USADO
        LocalDateTime enlaceExpira,
        boolean puedeReenviar,
        String estadoEnvio,        // EN_REVISION | CON_OBSERVACIONES | AUTORIZADO
        String comentario,
        boolean puedeAutorizar
) {}
