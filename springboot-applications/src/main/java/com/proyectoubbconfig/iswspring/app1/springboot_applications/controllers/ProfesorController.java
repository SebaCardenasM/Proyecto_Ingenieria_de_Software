package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Profesor;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.ProfesorRepository; 

@Controller
public class ProfesorController {

    @Autowired
    private ProfesorRepository profesorRepository;

    // 1. Mostrar el formulario del profesor
    @GetMapping("/profesores/registro")
    public String mostrarFormulario(Model model) {
        model.addAttribute("profesor", new Profesor());
        return "registro_profesor"; // Apunta al archivo registro_profesor.html
    }

    // 2. Guardar en la base de datos
    @PostMapping("/profesores/guardar")
    public String guardarProfesor(Profesor profesor) {
        profesorRepository.save(profesor);
        return "redirect:/"; // Vuelve al panel principal
    }
}