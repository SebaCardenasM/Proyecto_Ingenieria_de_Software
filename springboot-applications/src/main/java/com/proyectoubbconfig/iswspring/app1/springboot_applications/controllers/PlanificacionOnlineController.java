package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import java.security.Principal;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.CamposPlanificacion;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.*;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.*;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.DocumentoService;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.PlanificacionOnlineService;

@Controller
public class PlanificacionOnlineController {

    private static final String VISTA = "Estudiante/planificacion-formulario";

    @Autowired private PlanificacionOnlineService service;
    @Autowired private PlanificacionRepository planificacionRepository;
    @Autowired private EstudianteRepository estudianteRepository;
    @Autowired private PracticaRepository practicaRepository;
    @Autowired private DocumentoService documentoService;

    // ------------------------------------------------------------ estudiante: crear / editar / guardar

    @GetMapping("/estudiante/planificacion/nueva")
    public String nueva(Model model, Principal principal) {
        Map<String, String> valores = new HashMap<>();
        valores.put("asignatura", "Matemática");
        preparar(model, principal.getName(), valores, null, null, false, "/estudiante/planificaciones");
        return VISTA;
    }

    @GetMapping("/estudiante/planificacion/{id}/editar")
    @Transactional(readOnly = true)
    public String editar(@PathVariable Long id, Model model, Principal principal) {
        Planificacion p = planificacionRepository.findById(id).orElse(null);
        if (p == null || !p.isOnline() || p.getEstudiante() == null
                || !p.getEstudiante().getRut().equals(principal.getName())) {
            return "redirect:/estudiante/planificaciones";
        }
        if (!p.isBorrador()) return "redirect:/planificaciones/" + id;   // enviada: solo lectura
        preparar(model, principal.getName(), new HashMap<>(p.getCampos()), p.getId(), p.getNumeroPractica(),
                false, "/estudiante/planificaciones");
        return VISTA;
    }

    @PostMapping("/estudiante/planificacion/guardar")
    public String guardar(@RequestParam Map<String, String> params,
                          @RequestParam(required = false) Long idPlanificacion,
                          @RequestParam(required = false) Integer numeroPractica,
                          @RequestParam(defaultValue = "BORRADOR") String accion,
                          Principal principal, Model model, RedirectAttributes ra) {
        boolean enviar = "ENVIAR".equalsIgnoreCase(accion);
        try {
            service.guardar(principal.getName(), idPlanificacion, numeroPractica, params, enviar);
            ra.addFlashAttribute("tituloExito", enviar ? "¡Planificación enviada!" : "¡Borrador guardado!");
            ra.addFlashAttribute("mensajeExito", enviar
                    ? "Tu planificación fue enviada al gestor y tu profesor ya puede revisarla."
                    : "Tu planificación quedó como borrador. Puedes revisarla con el asistente o seguir editándola.");
            return "redirect:/estudiante/planificaciones";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            preparar(model, principal.getName(), new HashMap<>(params), idPlanificacion, numeroPractica,
                    false, "/estudiante/planificaciones");
            return VISTA;
        }
    }

    // ------------------------------------------------------------ vista de solo lectura (estudiante, profesor, tutor, coordinación)

    @GetMapping("/planificaciones/{id}")
    @Transactional(readOnly = true)
    public String ver(@PathVariable Long id, Model model, Authentication auth) {
        Planificacion p = planificacionRepository.findById(id).filter(Planificacion::isOnline).orElse(null);
        if (p == null || !documentoService.puedeVer(p)) return "redirect:/";

        String volver = "/";
        if (p.getEstudiante() != null && p.getEstudiante().getRut().equals(auth.getName())) volver = "/estudiante/planificaciones";
        else if (tiene(auth, "ROLE_PROFESOR_ASIGNATURA")) volver = "/profesor/planificaciones";
        else if (tiene(auth, "ROLE_COORDINADOR")) volver = "/gestor-archivos";
        else if (tiene(auth, "ROLE_TUTOR")) volver = "/profesor/archivos";

        model.addAttribute("secciones", CamposPlanificacion.SECCIONES);
        model.addAttribute("valores", new HashMap<>(p.getCampos()));
        model.addAttribute("idPlanificacion", p.getId());
        model.addAttribute("numeroPractica", p.getNumeroPractica());
        model.addAttribute("misPracticas", List.of());
        model.addAttribute("estudianteNombre", p.getEstudiante() != null
                ? p.getEstudiante().getNombre() + " " + p.getEstudiante().getApellido() : "");
        model.addAttribute("modoLectura", true);
        model.addAttribute("volver", volver);
        return VISTA;
    }

    // ------------------------------------------------------------ internos

    private boolean tiene(Authentication a, String rol) {
        return a.getAuthorities().stream().anyMatch(x -> x.getAuthority().equals(rol));
    }

    private void preparar(Model model, String rut, Map<String, String> valores, Long id, Integer numero,
                          boolean lectura, String volver) {
        Estudiante e = estudianteRepository.findById(rut).orElseThrow();
        // una opción por número de práctica
        Map<Integer, Practica> porNumero = new TreeMap<>();
        for (Practica p : practicaRepository.findByEstudiante(e)) {
            if (p.getNumeroPractica() != null) porNumero.merge(p.getNumeroPractica(), p,
                    (a, b) -> (b.getAno() != null && (a.getAno() == null || b.getAno() >= a.getAno())) ? b : a);
        }
        model.addAttribute("secciones", CamposPlanificacion.SECCIONES);
        model.addAttribute("valores", valores);
        model.addAttribute("idPlanificacion", id);
        model.addAttribute("numeroPractica", numero);
        model.addAttribute("misPracticas", new ArrayList<>(porNumero.values()));
        model.addAttribute("estudianteNombre", e.getNombre() + " " + e.getApellido());
        model.addAttribute("usuarioActivo", e);
        model.addAttribute("modoLectura", lectura);
        model.addAttribute("volver", volver);
    }

    // Página "Mis Planificaciones" (acceso: ROLE_ESTUDIANTE por /estudiante/** en SecurityConfig)
    @GetMapping("/estudiante/planificaciones")
    @Transactional(readOnly = true)
    public String misPlanificaciones(Model model, Principal principal) {
        Estudiante e = estudianteRepository.findById(principal.getName()).orElseThrow();
        model.addAttribute("usuarioActivo", e);
        model.addAttribute("misPracticas", practicaRepository.findByEstudiante(e));
        return "Estudiante/planificaciones";
    }
}