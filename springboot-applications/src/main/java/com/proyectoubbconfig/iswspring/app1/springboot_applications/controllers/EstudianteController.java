package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Usuario;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.EstudianteRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PracticaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.UsuarioRepository;

import java.security.Principal; 
import java.util.List; 

@Controller
public class EstudianteController {

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository; 

    @Autowired
    private PracticaRepository practicaRepository;

    @GetMapping("/registro")
    public String mostrarFormulario(Model model) {
        model.addAttribute("estudiante", new Estudiante());
        return "registro";
    }

    @PostMapping("/guardar")
    public String guardarEstudiante(Estudiante estudiante) {
        estudianteRepository.save(estudiante); 
        return "redirect:/"; 
    }

    @GetMapping("/estudiantes")
    public String listarEstudiantes(Model model) {
        List<Estudiante> listaEstudiantes = estudianteRepository.findAll();
        model.addAttribute("estudiantes", listaEstudiantes);
        return "Coordinador/estudiantes";
    }

    @GetMapping("/estudiante/archivos")
    public String mostrarGestorArchivosEstudiante(Model model, Principal principal) {
        
        if (principal != null) {
            String identificador = principal.getName(); 
            
            Estudiante estudianteActual = null;
            
            estudianteActual = estudianteRepository.findById(identificador).orElse(null);
            
            if (estudianteActual == null) {
                Usuario usuario = usuarioRepository.findByCorreo(identificador).orElse(null);
                if (usuario instanceof Estudiante) {
                    estudianteActual = (Estudiante) usuario;
                }
            }
            
            if (estudianteActual != null) {
                List<Practica> misPracticas = practicaRepository.findByEstudiante(estudianteActual);
                model.addAttribute("misPracticas", misPracticas);
            }
        }
        
        return "Estudiante/gestor-archivos-estudiante";
    }
}
