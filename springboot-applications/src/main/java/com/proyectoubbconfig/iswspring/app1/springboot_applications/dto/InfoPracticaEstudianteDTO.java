package com.proyectoubbconfig.iswspring.app1.springboot_applications.dto;

/**
 * Información contextual y pedagógica de la práctica inscrita por un estudiante,
 * utilizada para adaptar dinámicamente la vista "Subir Documentos".
 */
public record InfoPracticaEstudianteDTO(
        int numero,
        String nombre,
        String ciclo,
        String cicloBadgeClass,
        String modalidad,
        String duracion,
        String descripcionPedagogica,
        String requisitoEspecial,
        String icono,
        String temaColor,
        String centroNombre,
        String colaboradorNombre,
        String tutorNombre
) {}
