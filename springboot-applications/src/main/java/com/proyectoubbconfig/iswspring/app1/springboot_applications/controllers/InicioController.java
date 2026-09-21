package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Usuario;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InicioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Helper para cargar datos del usuario en la barra superior
    private void cargarDatosUsuario(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
            if (usuario != null) {
                model.addAttribute("usuarioActivo", usuario.getNombre() + " " + usuario.getApellido());
                model.addAttribute("rolUsuario", usuario.getRol().name());
            }
        }
    }

    // Index Coordinador / Administrador General
    @GetMapping({"/", "/index"})
    public String inicio(Model model, Authentication authentication) {
        cargarDatosUsuario(model, authentication);
        return "index"; // Retorna index.html
    }

    // Index Estudiante
    @GetMapping("/estudiante/index")
    public String inicioEstudiante(Model model, Authentication authentication) {
        cargarDatosUsuario(model, authentication);
        return "indexEstudiante"; // Retorna indexEstudiante.html
    }

    // Index Profesor
    @GetMapping("/profesor/index")
    public String inicioProfesor(Model model, Authentication authentication) {
        cargarDatosUsuario(model, authentication);
        return "indexProfesor"; // Retorna indexProfesor.html
    }
}