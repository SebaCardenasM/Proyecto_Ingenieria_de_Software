package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.FilaPlanificacion;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.EnlaceEvaluacion;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.RevisionPlanificacionService;

/** Revisión de planificaciones por el profesor UBB (acceso: ROLE_PROFESOR_ASIGNATURA, ver SecurityConfig). */
@Controller
@RequestMapping("/profesor/planificaciones")
public class RevisionPlanificacionController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired
    private RevisionPlanificacionService revisionService;

    @GetMapping
    public String listar(Model model, Authentication authentication) {
        List<FilaPlanificacion> filas = revisionService.listar(authentication.getName());
        long pendientes = filas.stream().filter(f -> !f.revisada()).count();

        model.addAttribute("filas", filas);
        model.addAttribute("pendientes", pendientes);
        model.addAttribute("confirmadas", filas.size() - pendientes);
        return "Profesor/planificaciones-revision";
    }

    // El "tick": confirma la planificación, genera el enlace y avisa al estudiante
    @PostMapping("/{id}/confirmar")
    public String confirmar(@PathVariable Long id, Authentication authentication, RedirectAttributes ra) {
        try {
            EnlaceEvaluacion enlace = revisionService.confirmar(id, authentication.getName());
            ra.addFlashAttribute("tituloExito", "¡Planificación confirmada!");
            ra.addFlashAttribute("mensajeExito",
                    "Se avisó al estudiante con el enlace para su profesor colaborador (vigente hasta "
                            + FMT.format(enlace.getFechaExpiracion()) + ").");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMensaje", e.getMessage());
        }
        return "redirect:/profesor/planificaciones";
    }

    @PostMapping("/{id}/reenviar")
    public String reenviar(@PathVariable Long id, Authentication authentication, RedirectAttributes ra) {
        try {
            EnlaceEvaluacion enlace = revisionService.reenviar(id, authentication.getName());
            ra.addFlashAttribute("tituloExito", "¡Nuevo enlace generado!");
            ra.addFlashAttribute("mensajeExito",
                    "El estudiante recibió un nuevo aviso. El enlace anterior ya no funciona y el nuevo vence el "
                            + FMT.format(enlace.getFechaExpiracion()) + ".");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMensaje", e.getMessage());
        }
        return "redirect:/profesor/planificaciones";
    }
}
