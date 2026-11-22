package com.proyectoubbconfig.iswspring.app1.springboot_applications.dto;

import java.time.LocalDate;
import java.util.List;

/** Un panel (pestaña) de la página "Mi Práctica": una práctica con todos sus intentos. */
public record PracticaPanelDTO(
        int numero,
        String nombre,
        boolean existe,
        boolean conCentro,
        boolean conColaborador,
        boolean conTutor,
        List<IntentoDTO> intentos        // el vigente (más reciente) primero
) {
    public record IntentoDTO(
            Long id,
            int intento,                 // 1, 2... en orden cronológico
            int totalIntentos,
            boolean vigente,
            Integer ano,
            Integer semestre,
            String estado,               // PENDIENTE | APROBADA | REPROBADA
            Double nota,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            ContactoDTO profesor,
            ContactoDTO tutor,
            ContactoDTO colaborador,
            CentroDTO centro
    ) {}

    public record ContactoDTO(String rut, String nombre, String correo, String telefono) {}

    public record CentroDTO(String nombre, String direccion, String comuna, String region) {}
}