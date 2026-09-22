package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.EstudianteRepository;

import java.util.List;

@Controller
public class EstudianteController {

    @Autowired
    private EstudianteRepository estudianteRepository;

    // Mostrar la lista de estudiantes
    @GetMapping("/estudiantes")
    public String listarEstudiantes(Model model) {
        List<Estudiante> listaEstudiantes = estudianteRepository.findAll();
        model.addAttribute("estudiantes", listaEstudiantes);
        return "estudiantes";
    }
}