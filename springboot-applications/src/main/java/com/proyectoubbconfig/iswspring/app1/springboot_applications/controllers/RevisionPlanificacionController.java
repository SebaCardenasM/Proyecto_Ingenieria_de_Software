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
import org.springframework.web.bind.annotation.RequestParam;
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
        model.addAttribute("filas", filas);
        model.addAttribute("total", filas.size());
        model.addAttribute("pendientes", filas.stream().filter(f -> "EN_REVISION".equals(f.estadoEnvio())).count());
        model.addAttribute("conObservaciones", filas.stream().filter(f -> "CON_OBSERVACIONES".equals(f.estadoEnvio())).count());
        model.addAttribute("confirmadas", filas.stream().filter(f -> "AUTORIZADO".equals(f.estadoEnvio())).count());
        model.addAttribute("conPautas", filas.stream().filter(FilaPlanificacion::evaluaConPautas).count());
        return "Profesor/planificaciones-revision";
    }

    @PostMapping("/{id}/comentar")
    public String comentar(@PathVariable Long id, @RequestParam String comentario,
                        Authentication authentication, RedirectAttributes ra) {
        try {
            revisionService.comentar(id, authentication.getName(), comentario);
            ra.addFlashAttribute("tituloExito", "¡Comentario enviado!");
            ra.addFlashAttribute("mensajeExito", "El estudiante verá tu comentario y podrá corregir y reenviar su planificación.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMensaje", e.getMessage());
        }
        return "redirect:/profesor/planificaciones";
    }

    // TICK 1: Autorizar planificación (bloquea, congela como PDF permanente y avisa al estudiante)
    @PostMapping({"/{id}/autorizar", "/{id}/revisar", "/{id}/confirmar"})
    public String autorizar(@PathVariable Long id, Authentication authentication, RedirectAttributes ra) {
        try {
            revisionService.autorizar(id, authentication.getName());
            ra.addFlashAttribute("tituloExito", "¡Planificación autorizada!");
            ra.addFlashAttribute("mensajeExito",
                    "La planificación quedó bloqueada y guardada como PDF permanente. Se avisó al estudiante que ya puede dar su clase.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMensaje", e.getMessage());
        }
        return "redirect:/profesor/planificaciones";
    }

    // TICK 2: Marcar que la clase se evaluará con pautas (genera enlace de evaluación para el colaborador)
    @PostMapping("/{id}/pautas")
    public String evaluarConPautas(@PathVariable Long id, Authentication authentication, RedirectAttributes ra) {
        try {
            EnlaceEvaluacion enlace = revisionService.marcarEvaluaConPautas(id, authentication.getName());
            ra.addFlashAttribute("tituloExito", "¡Clase marcada para evaluarse con pautas!");
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
