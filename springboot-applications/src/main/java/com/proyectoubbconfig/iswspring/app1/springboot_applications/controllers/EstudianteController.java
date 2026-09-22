package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

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

    // Mostrar la página web del formulario de registro
    @GetMapping("/registro")
    public String mostrarFormulario(Model model) {
        model.addAttribute("estudiante", new Estudiante());
        return "registro";
    }

    // Recibir los datos del formulario y guardarlos en BD
    @PostMapping("/guardar")
    public String guardarEstudiante(Estudiante estudiante) {
        estudianteRepository.save(estudiante); 
        return "redirect:/"; 
    }

    // Mostrar la lista de estudiantes
    @GetMapping("/estudiantes")
    public String listarEstudiantes(Model model) {
        List<Estudiante> listaEstudiantes = estudianteRepository.findAll();
        model.addAttribute("estudiantes", listaEstudiantes);
        return "estudiantes"; 
    }

    // Gestor de archivos inteligente del estudiante
    @GetMapping("/estudiante/archivos")
    public String mostrarGestorArchivosEstudiante(Model model, Principal principal) {
        
        // Verificamos que alguien haya iniciado sesión
        if (principal != null) {
            String correo = principal.getName(); 
            
            // Buscamos al estudiante por su correo (corregido por la herencia)
            Estudiante estudianteActual = estudianteRepository.findByCorreo(correo);
            
            if (estudianteActual != null) {
                // Buscamos todas las prácticas que tiene asignadas
                List<Practica> misPracticas = practicaRepository.findByEstudiante(estudianteActual);
                
                // Se las enviamos a la vista HTML
                model.addAttribute("misPracticas", misPracticas);
            }
        }
        
        return "gestor-archivos-estudiante"; 
    }
}