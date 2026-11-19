package com.proyectoubbconfig.iswspring.app1.springboot_applications.dto;

import java.time.LocalDateTime;

/** Un aviso del estudiante, listo para mostrar. */
public record AvisoVista(
        Long id,
        String titulo,
        String mensaje,
        LocalDateTime fecha,
        boolean nuevo,
        String token,                 // null si el aviso no trae enlace
        String estadoEnlace,          // VIGENTE | EXPIRADO | USADO (null sin enlace)
        LocalDateTime expira
) {}
