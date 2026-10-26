package com.proyectoubbconfig.iswspring.app1.springboot_applications.services;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.NotaEstudianteFila;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.NotasFiltro;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.EstudianteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class NotasService {

    private static final int TOTAL = NotasFiltro.TOTAL_PRACTICAS;

    private static final Comparator<Practica> MAS_RECIENTE = Comparator
            .comparing(Practica::getAno, Comparator.nullsFirst(Comparator.<Integer>naturalOrder()))
            .thenComparing(Practica::getSemestre, Comparator.nullsFirst(Comparator.<Integer>naturalOrder()));

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Transactional(readOnly = true)
    public List<NotaEstudianteFila> obtenerFilas(NotasFiltro filtro) {
        return estudianteRepository.findAllConPracticas().stream()
                .sorted(Comparator.comparing(Estudiante::getApellido,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(this::aFila)
                .filter(filtro::cumple)
                .toList();
    }

    private NotaEstudianteFila aFila(Estudiante e) {
        Map<Integer, Practica> porNumero = new HashMap<>();

        if (e.getPracticas() != null) {
            for (Practica p : e.getPracticas()) {
                Integer n = p.getNumeroPractica();
                if (n == null || n < 1 || n > TOTAL || p.getNotaAprobacion() == null) continue;

                Practica actual = porNumero.get(n);
                if (actual == null || MAS_RECIENTE.compare(p, actual) > 0) {
                    porNumero.put(n, p);
                }
            }
        }

        List<Double> notas = new ArrayList<>(Collections.nCopies(TOTAL, (Double) null));
        porNumero.forEach((n, p) -> notas.set(n - 1, p.getNotaAprobacion()));

        String nombre = ((e.getNombre() == null ? "" : e.getNombre()) + " "
                + (e.getApellido() == null ? "" : e.getApellido())).trim();

        return new NotaEstudianteFila(e.getRut(), nombre, e.getAnoIngreso(), notas);
    }

    // Promedio por columna (null si nadie tiene nota en esa práctica)
    public List<Double> promedios(List<NotaEstudianteFila> filas) {
        List<Double> promedios = new ArrayList<>();
        for (int i = 0; i < TOTAL; i++) {
            final int col = i;
            OptionalDouble prom = filas.stream()
                    .map(f -> f.notas().get(col))
                    .filter(Objects::nonNull)
                    .mapToDouble(Double::doubleValue)
                    .average();
            promedios.add(prom.isPresent() ? prom.getAsDouble() : null);
        }
        return promedios;
    }

    // CSV con ';' y coma decimal para que Excel en español lo abra bien
    public String generarCsv(List<NotaEstudianteFila> filas) {
        StringBuilder sb = new StringBuilder();
        sb.append("Rut;Nombre;Año ingreso");
        for (int i = 1; i <= TOTAL; i++) sb.append(";Nota P").append(i);
        sb.append("\r\n");

        for (NotaEstudianteFila f : filas) {
            sb.append(csv(f.rut())).append(';')
              .append(csv(f.nombreCompleto())).append(';')
              .append(f.anoIngreso() == null ? "" : f.anoIngreso());
            for (Double n : f.notas()) sb.append(';').append(nota(n));
            sb.append("\r\n");
        }

        sb.append("Promedio;;");
        for (Double p : promedios(filas)) sb.append(';').append(nota(p));
        sb.append("\r\n");

        return sb.toString();
    }

    private String nota(Double n) {
        return n == null ? "" : String.format(Locale.US, "%.1f", n).replace('.', ',');
    }

    private String csv(String s) {
        if (s == null) return "";
        if (s.contains(";") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}