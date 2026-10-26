package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Usuario;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.EstudianteRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PracticaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class PortafolioEstudianteController {

    private static final int TOTAL_PRACTICAS = 6;

    private static final Comparator<Practica> CRONOLOGICO = Comparator
            .comparing(Practica::getAno, Comparator.nullsFirst(Comparator.<Integer>naturalOrder()))
            .thenComparing(Practica::getSemestre, Comparator.nullsFirst(Comparator.<Integer>naturalOrder()));

    // Una fila de la tabla: un intento de una práctica (o un hueco si aún no existe)
    public record FilaPractica(
            int numero,
            int intento,
            int intentos,
            boolean existe,
            boolean vigente, // último intento: el que cuenta para el promedio
            String estado,
            Integer ano,
            Integer semestre,
            String profesor,
            String tutor,
            String centro,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            Double nota
    ) {
        static FilaPractica vacia(int numero) {
            return new FilaPractica(numero, 1, 1, false, true, null,
                    null, null, null, null, null, null, null, null);
        }
    }

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private PracticaRepository practicaRepository;

    // Solo el estudiante logueado: el rut sale de la sesión, no de la URL
    @GetMapping("/estudiante/portafolio")
    public String verPortafolio(Model model, Authentication authentication) {
        Optional<Estudiante> estudianteOpt = estudianteRepository.findById(authentication.getName());
        if (estudianteOpt.isEmpty()) {
            return "redirect:/estudiante/index";
        }
        Estudiante estudiante = estudianteOpt.get();

        Map<Integer, List<Practica>> porNumero = practicaRepository
                .findPortafolioByRut(estudiante.getRut()).stream()
                .filter(p -> p.getNumeroPractica() != null)
                .collect(Collectors.groupingBy(Practica::getNumeroPractica));

        List<FilaPractica> filas = new ArrayList<>();
        List<Double> notasVigentes = new ArrayList<>();

        for (int n = 1; n <= TOTAL_PRACTICAS; n++) {
            List<Practica> intentos = porNumero.getOrDefault(n, List.of()).stream()
                    .sorted(CRONOLOGICO)
                    .toList();

            if (intentos.isEmpty()) {
                filas.add(FilaPractica.vacia(n));
                continue;
            }

            for (int i = 0; i < intentos.size(); i++) {
                Practica p = intentos.get(i);
                boolean vigente = (i == intentos.size() - 1);
                if (vigente && p.getNotaAprobacion() != null) {
                    notasVigentes.add(p.getNotaAprobacion());
                }
                filas.add(aFila(n, p, i + 1, intentos.size(), vigente));
            }
        }

        OptionalDouble promedio = notasVigentes.stream().mapToDouble(Double::doubleValue).average();

        model.addAttribute("estudiante", estudiante);
        model.addAttribute("nombreEstudiante", nombre(estudiante));
        model.addAttribute("coordinador", nombre(estudiante.getProfesorCoordinador()));
        model.addAttribute("filas", filas);
        model.addAttribute("promedio", promedio.isPresent() ? promedio.getAsDouble() : null);
        cargarDatosUsuario(model, estudiante);
        return "Estudiante/portafolioEstudiante";
    }

    private FilaPractica aFila(int numero, Practica p, int intento, int intentos, boolean vigente) {
        return new FilaPractica(
                numero, intento, intentos, true, vigente,
                p.getEstado() != null ? p.getEstado().name() : null,
                p.getAno(), p.getSemestre(),
                nombre(p.getProfesor()),
                nombre(p.getTutor()),
                p.getCentroPractica() != null ? p.getCentroPractica().getNombre() : null,
                p.getFechaInicio(), p.getFechaFin(),
                p.getNotaAprobacion());
    }

    private String nombre(Usuario u) {
        if (u == null) return null;
        return ((u.getNombre() == null ? "" : u.getNombre()) + " "
                + (u.getApellido() == null ? "" : u.getApellido())).trim();
    }

    // Datos para la barra de navegación
    private void cargarDatosUsuario(Model model, Estudiante estudiante) {
        model.addAttribute("usuarioActivo", estudiante);
        model.addAttribute("rolesUsuario", estudiante.getRoles().stream().map(Enum::name).toList());
    }
}