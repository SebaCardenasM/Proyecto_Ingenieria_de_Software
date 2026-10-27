package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PracticaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/tutor")
public class TutorController {

    @Autowired
    private PracticaRepository practicaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // 1. Mostrar la lista de alumnos asignados al tutor
    @GetMapping("/alumnos")
    public String verAlumnosAsignados(Model model, Authentication authentication) {
        String rutTutor = authentication.getName();
        
        // Cargar datos de sesión para la barra de navegación
        cargarDatosUsuario(model, rutTutor);

        // Buscar todas las prácticas donde este usuario es el tutor asignado
        List<Practica> practicasAsignadas = practicaRepository.findByTutorRut(rutTutor);
        
        model.addAttribute("practicas", practicasAsignadas);
        return "Tutor/alumnos-asignados";
    }

   // 2. Mostrar el perfil detallado del estudiante para cargar pautas
    @GetMapping("/estudiante/{idPractica}")
    @Transactional(readOnly = true) // <-- ESTO SOLUCIONA EL ERROR LAZY INITIALIZATION
    public String verPerfilEstudiante(@PathVariable Long idPractica, Model model, Authentication authentication) {
        String rutTutor = authentication.getName();
        cargarDatosUsuario(model, rutTutor);

        // Buscar la práctica específica
        Optional<Practica> practicaOpt = practicaRepository.findById(idPractica);
        
        if (practicaOpt.isPresent()) {
            Practica practica = practicaOpt.get();
            
            // Validar por seguridad que el tutor logueado realmente sea el tutor de esta práctica
            if (practica.getTutor() != null && practica.getTutor().getRut().equals(rutTutor)) {
                // Forzamos la carga inicial de los archivos antes de ir a la vista
                practica.getArchivos().size(); 

                model.addAttribute("practica", practica);
                model.addAttribute("estudiante", practica.getEstudiante());
                return "Tutor/perfil-estudiante-tutor";
            }
        }
        
        // Si no existe o no tiene permisos, lo devolvemos a la lista
        return "redirect:/tutor/alumnos";
    }

    // Método auxiliar para mantener la consistencia en la Navbar
    private void cargarDatosUsuario(Model model, String rut) {
        usuarioRepository.findByRut(rut).ifPresent(u -> {
            model.addAttribute("usuarioActivo", u);
            model.addAttribute("rolesUsuario", u.getRoles().stream().map(Enum::name).toList());
        });
    }
}