package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Dupla;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Usuario;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.UsuarioRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.DuplaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/profesor/duplas")
public class DuplaProfesorController {

    @Autowired
    private DuplaService duplaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping({"", "/"})
    public String verDuplas(@RequestParam(value = "practica", required = false) Integer numPractica,
                            Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        String rutProfesor = principal.getName();
        List<Integer> practicasConDuplas = duplaService.obtenerNumerosPracticaConDuplasDeProfesor(rutProfesor);

        // Si se especificó una práctica y es válida
        Integer seleccionada = null;
        if (numPractica != null && duplaService.permiteDuplas(numPractica)) {
            seleccionada = numPractica;
        } else if (!practicasConDuplas.isEmpty()) {
            seleccionada = practicasConDuplas.get(0);
        } else {
            // Valor por defecto si aún no tiene asignada ninguna
            seleccionada = 3;
        }

        List<Dupla> duplas = duplaService.listarDuplasPorProfesorYPractica(rutProfesor, seleccionada);
        List<Practica> estudiantesDisponibles = duplaService.listarEstudiantesDisponibles(rutProfesor, seleccionada);
        List<Practica> todosEstudiantes = duplaService.listarTodosEstudiantesDeProfesor(rutProfesor, seleccionada);

        model.addAttribute("practicasConDuplas", practicasConDuplas);
        model.addAttribute("practicaSeleccionada", seleccionada);
        model.addAttribute("duplas", duplas);
        model.addAttribute("estudiantesDisponibles", estudiantesDisponibles);
        model.addAttribute("todosEstudiantes", todosEstudiantes);
        model.addAttribute("totalEstudiantes", todosEstudiantes.size());
        model.addAttribute("totalDuplas", duplas.size());
        model.addAttribute("pendientesDupla", estudiantesDisponibles.size());

        usuarioRepository.findByRut(rutProfesor).ifPresent(u -> {
            model.addAttribute("usuarioActivo", u);
            model.addAttribute("rolesUsuario", u.getRoles().stream().map(Enum::name).toList());
        });

        return "Profesor/duplas";
    }

    @PostMapping("/crear")
    public String crearDupla(@RequestParam Long practica1Id,
                             @RequestParam Long practica2Id,
                             @RequestParam Integer numeroPractica,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        try {
            Dupla d = duplaService.crearDupla(principal.getName(), practica1Id, practica2Id, numeroPractica);
            redirectAttributes.addFlashAttribute("tituloExito", "¡Dupla conformada!");
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Se ha conformado exitosamente la dupla entre " +
                    d.getEstudiante1().getNombre() + " " + d.getEstudiante1().getApellido() + " y " +
                    d.getEstudiante2().getNombre() + " " + d.getEstudiante2().getApellido() +
                    " para la Práctica " + numeroPractica + ".");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMensaje", e.getMessage());
        }
        return "redirect:/profesor/duplas?practica=" + numeroPractica;
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarDupla(@PathVariable Long id,
                                @RequestParam Integer numeroPractica,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            duplaService.eliminarDupla(principal.getName(), id);
            redirectAttributes.addFlashAttribute("tituloExito", "¡Dupla deshecha!");
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "La dupla fue deshecha correctamente. Los estudiantes quedaron disponibles para asignarse a una nueva pareja.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMensaje", e.getMessage());
        }
        return "redirect:/profesor/duplas?practica=" + numeroPractica;
    }
}
