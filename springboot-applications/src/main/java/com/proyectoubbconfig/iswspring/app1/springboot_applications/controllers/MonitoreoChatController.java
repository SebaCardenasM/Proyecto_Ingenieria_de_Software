package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.InteraccionIA;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.EstudianteRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.InteraccionIARepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.InteraccionIARepository.ResumenEstudianteDTO;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PracticaRepository;

/**
 * Monitoreo de los mensajes de los estudiantes con el asistente.
 * - Coordinador: /monitoreo-chat (todos los estudiantes)
 * - Profesor de asignatura: /profesor/monitoreo-chat (solo los estudiantes de sus prácticas)
 * El acceso a cada ruta lo filtra SecurityConfig; aquí se filtra además por estudiante.
 */
@Controller
public class MonitoreoChatController {

    public record FilaEstudiante(String rut, String nombre, Integer anoIngreso, long total, LocalDateTime ultima) {}

    @Autowired
    private InteraccionIARepository interaccionRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private PracticaRepository practicaRepository;

    // ---------------------------------------------------------------- coordinación

    @GetMapping("/monitoreo-chat")
    @Transactional(readOnly = true)
    public String listarCoordinador(Model model) {
        cargarLista(model, null);
        configurarVista(model, "/monitoreo-chat", "/index", "Coordinación");
        return "Coordinador/monitoreo-chat";
    }

    @GetMapping("/monitoreo-chat/{rut}")
    @Transactional(readOnly = true)
    public String detalleCoordinador(@PathVariable String rut, Model model) {
        if (!cargarDetalle(model, rut)) return "redirect:/monitoreo-chat";
        configurarVista(model, "/monitoreo-chat", "/index", "Coordinación");
        return "Coordinador/monitoreo-chat-detalle";
    }

    // ---------------------------------------------------------------- profesor de asignatura

    @GetMapping("/profesor/monitoreo-chat")
    @Transactional(readOnly = true)
    public String listarProfesor(Model model, Authentication authentication) {
        Set<String> permitidos = new HashSet<>(practicaRepository.findRutsEstudiantesDeProfesor(authentication.getName()));
        cargarLista(model, permitidos);
        configurarVista(model, "/profesor/monitoreo-chat", "/profesor/index", "Profesor de Asignatura");
        return "Coordinador/monitoreo-chat";
    }

    @GetMapping("/profesor/monitoreo-chat/{rut}")
    @Transactional(readOnly = true)
    public String detalleProfesor(@PathVariable String rut, Model model, Authentication authentication) {
        // Solo si el estudiante tiene una práctica con este profesor
        if (!practicaRepository.existsByProfesorRutAndEstudianteRut(authentication.getName(), rut)
                || !cargarDetalle(model, rut)) {
            return "redirect:/profesor/monitoreo-chat";
        }
        configurarVista(model, "/profesor/monitoreo-chat", "/profesor/index", "Profesor de Asignatura");
        return "Coordinador/monitoreo-chat-detalle";
    }

    // ---------------------------------------------------------------- internos

    private void configurarVista(Model model, String base, String volver, String rol) {
        model.addAttribute("base", base);       // ruta base de esta vista (para los enlaces)
        model.addAttribute("volver", volver);   // a dónde vuelve el botón "Volver al Panel"
        model.addAttribute("rolPanel", rol);
    }

    /** rutsPermitidos == null: sin restricción (coordinación). */
    private void cargarLista(Model model, Set<String> rutsPermitidos) {
        List<ResumenEstudianteDTO> resumenes = interaccionRepository.resumenPorEstudiante().stream()
                .filter(r -> rutsPermitidos == null || rutsPermitidos.contains(r.getRut()))
                .toList();

        Map<String, Estudiante> estudiantes = estudianteRepository
                .findAllById(resumenes.stream().map(ResumenEstudianteDTO::getRut).toList())
                .stream().collect(Collectors.toMap(Estudiante::getRut, e -> e));

        List<FilaEstudiante> filas = resumenes.stream()
                .filter(r -> estudiantes.containsKey(r.getRut()))
                .map(r -> {
                    Estudiante e = estudiantes.get(r.getRut());
                    String nombre = ((e.getNombre() == null ? "" : e.getNombre()) + " "
                            + (e.getApellido() == null ? "" : e.getApellido())).trim();
                    return new FilaEstudiante(e.getRut(), nombre, e.getAnoIngreso(), r.getTotal(), r.getUltima());
                })
                .toList();

        model.addAttribute("filas", filas);
        model.addAttribute("totalMensajes", filas.stream().mapToLong(FilaEstudiante::total).sum());
    }

    private boolean cargarDetalle(Model model, String rut) {
        Estudiante e = estudianteRepository.findById(rut).orElse(null);
        if (e == null) return false;

        List<InteraccionIA> mensajes = new ArrayList<>(
                interaccionRepository.findTop200ByRutEstudianteOrderByFechaDesc(rut));
        Collections.reverse(mensajes);   // cronológico

        model.addAttribute("estudiante", e);
        model.addAttribute("mensajes", mensajes);
        return true;
    }
}