package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.EstudianteRepository; 


@Controller
public class EstudianteController {

    @Autowired
    private EstudianteRepository estudianteRepository;

    // 1. Mostrar la página web del formulario
    @GetMapping("/registro")
    public String mostrarFormulario(Model model) {
        model.addAttribute("estudiante", new Estudiante());
        return "registro"; // Apunta al archivo registro.html
    }

    // 2. Recibir los datos del formulario y guardarlos en BD
    @PostMapping("/guardar")
    public String guardarEstudiante(Estudiante estudiante) {
        estudianteRepository.save(estudiante); // ¡La magia de Spring Boot!
        return "redirect:/"; // Redirige a la página de inicio al terminar
    }
}