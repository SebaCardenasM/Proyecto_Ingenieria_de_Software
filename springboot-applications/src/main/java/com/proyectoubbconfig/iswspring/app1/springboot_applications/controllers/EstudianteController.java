package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.EstudianteRepository; 

import java.util.List; // ¡No olvides esta importación!

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
        estudianteRepository.save(estudiante); 
        return "redirect:/"; 
    }

    // 3. NUEVO: Mostrar la lista de estudiantes
    @GetMapping("/estudiantes")
    public String listarEstudiantes(Model model) {
        // Obtenemos todos los estudiantes de la base de datos
        List<Estudiante> listaEstudiantes = estudianteRepository.findAll();
        
        // Enviamos la lista al HTML usando el mismo nombre que pusimos en el th:each
        model.addAttribute("estudiantes", listaEstudiantes);
        
        return "estudiantes"; // Apunta al archivo estudiantes.html
    }
}