package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Profesor;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.ProfesorRepository;

import java.util.List;

@Controller
public class ProfesorController {

    @Autowired
    private ProfesorRepository profesorRepository;

    // Mostrar la lista de profesores
    @GetMapping("/profesores")
    public String listarProfesores(Model model) {
        List<Profesor> listaProfesores = profesorRepository.findAll();
        model.addAttribute("profesores", listaProfesores);
        return "profesores";
    }
}