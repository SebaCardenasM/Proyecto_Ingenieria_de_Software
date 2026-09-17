package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Evaluacion;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.EvaluacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/evaluaciones")
public class EvaluacionController {

    @Autowired
    private EvaluacionRepository evaluacionRepository;

    @PostMapping("/guardar")
    public String guardarEvaluacion(Evaluacion evaluacion) {
        // Estampar la fecha en la que el profesor completó la rúbrica
        evaluacion.setFechaEvaluacion(LocalDateTime.now());
        
        // Guardar puntaje y observaciones cualitativas en MySQL
        evaluacionRepository.save(evaluacion);
        
        // Redirigir al portafolio del estudiante o al panel del profesor
        return "redirect:/"; 
    }
}