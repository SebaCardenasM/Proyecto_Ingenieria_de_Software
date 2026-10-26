package com.proyectoubbconfig.iswspring.app1.springboot_applications.dto;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Documento;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoDocumento;

import java.time.LocalDateTime;

public record DocumentoDTO(
        Long id,
        String nombreArchivo,
        String rutaServidor,
        LocalDateTime fechaSubida,
        TipoDocumento tipoDocumento,
        Integer numeroPractica,
        EstudianteResumen estudiante
) {

    // Solo lo mínimo para mostrar en la tabla: nada de correo, password ni roles
    public record EstudianteResumen(String rut, String nombre, String apellido) {}

    public static DocumentoDTO desde(Documento d) {
        Estudiante e = d.getEstudiante();
        if (e == null && d.getPractica() != null) {
            e = d.getPractica().getEstudiante();
        }

        Integer numero = d.getNumeroPractica();
        if (numero == null && d.getPractica() != null) {
            numero = d.getPractica().getNumeroPractica();
        }

        return new DocumentoDTO(
                d.getId(),
                d.getNombreArchivo(),
                d.getRutaServidor(),
                d.getFechaSubida(),
                d.getTipoDocumento(),
                numero,
                e == null ? null : new EstudianteResumen(e.getRut(), e.getNombre(), e.getApellido())
        );
    }
}