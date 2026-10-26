package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.EstudianteRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PracticaRepository;

import java.security.Principal;
import java.util.List;

@Controller
public class EstudianteController {

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private PracticaRepository practicaRepository;

    @GetMapping("/estudiantes")
    public String listarEstudiantes(Model model) {
        model.addAttribute("estudiantes", estudianteRepository.findAll());
        return "Coordinador/estudiantes";
    }

    @GetMapping("/estudiante/archivos")
    @Transactional(readOnly = true)
    public String mostrarGestorArchivosEstudiante(Model model, Principal principal) {
        // El username de sesión es el RUT (CustomUserDetailsService)
        estudianteRepository.findById(principal.getName()).ifPresent(estudiante -> {
            List<Practica> misPracticas = practicaRepository.findByEstudiante(estudiante);
            model.addAttribute("usuarioActivo", estudiante);
            model.addAttribute("misPracticas", misPracticas);
        });
        return "Estudiante/gestor-archivos-estudiante";
    }
}