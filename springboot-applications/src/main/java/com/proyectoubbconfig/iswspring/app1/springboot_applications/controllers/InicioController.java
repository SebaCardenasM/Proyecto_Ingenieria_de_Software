package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Usuario;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
public class InicioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Carga los datos del usuario autenticado basándose en su RUT
    private void cargarDatosUsuario(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            // authentication.getName() retorna el RUT ingresado en el Login
            Optional<Usuario> usuarioOpt = usuarioRepository.findByRut(authentication.getName());
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                model.addAttribute("usuarioActivo", usuario);
                model.addAttribute("rolUsuario", usuario.getRol().name());
            }
        }
    }

    // Index Coordinador / Administrador General
    @GetMapping({"/", "/index"})
    public String inicio(Model model, Authentication authentication) {
        cargarDatosUsuario(model, authentication);
        return "index";
    }

    // Index Estudiante
    @GetMapping("/estudiante/index")
    public String inicioEstudiante(Model model, Authentication authentication) {
        cargarDatosUsuario(model, authentication);
        return "indexEstudiante";
    }

    // Index Profesor
    @GetMapping("/profesor/index")
    public String inicioProfesor(Model model, Authentication authentication) {
        cargarDatosUsuario(model, authentication);
        return "indexProfesor";
    }
}