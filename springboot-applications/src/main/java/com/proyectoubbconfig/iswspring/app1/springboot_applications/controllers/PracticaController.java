package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PracticaRepository; 

@Controller
public class PracticaController {

    @Autowired
    private PracticaRepository practicaRepository;

    // 1. Mostrar el formulario de asignación de práctica
    @GetMapping("/practicas/registro")
    public String mostrarFormulario(Model model) {
        model.addAttribute("practica", new Practica());
        return "registro_practica"; // Apunta al archivo registro_practica.html
    }

    // 2. Guardar la práctica en la base de datos
    @PostMapping("/practicas/guardar")
    public String guardarPractica(Practica practica) {
        practicaRepository.save(practica);
        return "redirect:/"; // Vuelve al panel principal tras guardar
    }
}