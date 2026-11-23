package com.proyectoubbconfig.iswspring.app1.springboot_applications.dto;

import java.time.LocalDateTime;
import java.util.Comparator;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Documento;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Informe;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Pauta;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Planificacion;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoDocumento;

public record DocumentoDTO(
        Long id,
        String nombreArchivo,
        String rutaServidor,
        LocalDateTime fechaSubida,
        TipoDocumento tipoDocumento,
        Integer numeroPractica,
        Integer anoPractica,
        Integer semestrePractica,
        String tipoEvaluador,

        String estado,              // BORRADOR | ENVIADO
        boolean revisadoIa,         // pasó por el asistente
        String estadoRevision ,   // PENDIENTE | REVISADA | CON_PAUTAS | EVALUADO (null en otros tipos)
        String comentarioProfesor,   
        Double nota,                // solo informes evaluados
        String retroalimentacion,   // solo informes evaluados (opcional)
        EstudianteResumen estudiante
) {

    // Solo lo mínimo para mostrar en la tabla: nada de correo, password ni roles
    public record EstudianteResumen(String rut, String nombre, String apellido, Integer anoIngreso) {}

    public static DocumentoDTO desde(Documento d) {
        Estudiante e = d.getEstudiante();
        if (e == null && d.getPractica() != null) {
            e = d.getPractica().getEstudiante();
        }

        Integer numero = d.getNumeroPractica();
        if (numero == null && d.getPractica() != null) {
            numero = d.getPractica().getNumeroPractica();
        }

        // Práctica del documento; si no la tiene, se busca entre las prácticas del estudiante
        // por número (si hay repetidas, la más reciente)
        Practica practica = d.getPractica();
        if (practica == null && e != null && numero != null && e.getPracticas() != null) {
            final Integer n = numero;
            practica = e.getPracticas().stream()
                    .filter(p -> n.equals(p.getNumeroPractica()))
                    .max(Comparator
                            .comparing(Practica::getAno, Comparator.nullsFirst(Comparator.<Integer>naturalOrder()))
                            .thenComparing(Practica::getSemestre, Comparator.nullsFirst(Comparator.<Integer>naturalOrder())))
                    .orElse(null);
        }
        Integer anoPractica = practica != null ? practica.getAno() : null;
        Integer semestrePractica = practica != null ? practica.getSemestre() : null;

        String evaluadorStr = null;
        if (d instanceof Pauta pauta) {
            if (pauta.getTipoEvaluador() != null) {
                evaluadorStr = pauta.getTipoEvaluador().name();
            }
        }

        // Estado de revisión según el tipo de documento
        String estadoRevision = null;
        Double nota = null;
        String retroalimentacion = null;
        if (d instanceof Informe inf) {
            nota = inf.getNota();
            retroalimentacion = inf.getRetroalimentacion();
            estadoRevision = nota != null ? "EVALUADO" : "PENDIENTE";
        } else if (d instanceof Planificacion pl) {
            estadoRevision = pl.isEvaluaConPautas() ? "CON_PAUTAS"
                    : (pl.isRevisada() ? "REVISADA" : "PENDIENTE");
        }

        return new DocumentoDTO(
                d.getId(),
                d.getNombreArchivo(),
                d.getRutaServidor(),
                d.getFechaSubida(),
                d.getTipoDocumento(),
                numero,
                anoPractica,
                semestrePractica,
                evaluadorStr,
                d.getEstado().name(),
                d.isRevisadoIa(),
                d instanceof Planificacion pl ? pl.getComentarioProfesor() : null,
                estadoRevision,
                nota,
                retroalimentacion,
                e == null ? null : new EstudianteResumen(e.getRut(), e.getNombre(), e.getApellido(), e.getAnoIngreso())
        );
    }
}