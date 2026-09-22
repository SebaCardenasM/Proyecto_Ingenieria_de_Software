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
    private UsuarioRepository usuarioRepository; // <-- Inyectamos UsuarioRepository para buscar por correo en la jerarquía

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
        
        if (principal != null) {
            String identificador = principal.getName(); // Puede ser el RUT o el correo según la configuración de Login
            
            Estudiante estudianteActual = null;
            
            // 1. Intentamos buscar directamente por su RUT (ya que el RUT es la llave primaria de Estudiante)
            estudianteActual = estudianteRepository.findById(identificador).orElse(null);
            
            // 2. Si no se encontró por RUT, intentamos buscarlo a través del repositorio de usuarios por correo
            if (estudianteActual == null) {
                Usuario usuario = usuarioRepository.findByCorreo(identificador).orElse(null);
                if (usuario instanceof Estudiante) {
                    estudianteActual = (Estudiante) usuario;
                }
            }
            
            // 3. Si encontramos al estudiante, cargamos sus prácticas asignadas
            if (estudianteActual != null) {
                List<Practica> misPracticas = practicaRepository.findByEstudiante(estudianteActual);
                model.addAttribute("misPracticas", misPracticas);
            }
        }
        
        return "gestor-archivos-estudiante"; 
    }
}
