package com.proyectoubbconfig.iswspring.app1.springboot_applications.dto;

import java.util.List;

public record NotaEstudianteFila(
        String rut,
        String nombreCompleto,
        Integer anoIngreso,
        List<Double> notas // 6 posiciones, null si la práctica no tiene nota
) {}