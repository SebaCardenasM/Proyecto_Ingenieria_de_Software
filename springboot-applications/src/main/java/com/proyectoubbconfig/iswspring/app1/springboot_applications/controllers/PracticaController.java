package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.EstudianteRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PracticaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.ProfesorRepository;

import java.util.List;

@Controller
public class PracticaController {

    @Autowired
    private PracticaRepository practicaRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private ProfesorRepository profesorRepository;

    @GetMapping("/practicas")
    public String listarPracticas(Model model) {
        List<Practica> listaPracticas = practicaRepository.findAll();
        model.addAttribute("practicas", listaPracticas);
        model.addAttribute("estudiantes", estudianteRepository.findAll());
        model.addAttribute("profesores", profesorRepository.findAll());
        return "practicas";
    }
    
    // 1. Cargar las listas en el modelo para que aparezcan en los selects
    @GetMapping("/practicas/registro")
    public String mostrarFormulario(Model model) {
        model.addAttribute("practica", new Practica());
        model.addAttribute("estudiantes", estudianteRepository.findAll());
        model.addAttribute("profesores", profesorRepository.findAll());
        return "registro_practica";
    }

    // 2. Recibir el objeto practica bindearlo con @ModelAttribute, validar fechas y guardar
    @PostMapping("/practicas/guardar")
    public String guardarPractica(@ModelAttribute("practica") Practica practica, Model model) {
        
        // Validación: La fecha de fin no puede ser anterior a la fecha de inicio
        if (practica.getFechaInicio() != null && practica.getFechaFin() != null) {
            if (practica.getFechaFin().isBefore(practica.getFechaInicio())) {
                model.addAttribute("error", "La fecha de fin no puede ser anterior a la fecha de inicio.");
                model.addAttribute("estudiantes", estudianteRepository.findAll());
                model.addAttribute("profesores", profesorRepository.findAll());
                return "registro_practica"; // Retorna al formulario mostrando la alerta de error
            }
        }

        practicaRepository.save(practica);
        
        return "redirect:/practicas";
    }
}