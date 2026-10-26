package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Usuario;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class DocumentoViewController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/gestor-archivos")
    public String mostrarGestorArchivos() {
        return "Coordinador/gestor-archivos";
    }

    // Ruta para el Gestor de Archivos del Profesor / Tutor
    @GetMapping("/profesor/archivos")
    public String gestorArchivosProfesor(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByRut(authentication.getName());
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                model.addAttribute("usuarioActivo", usuario);
                List<String> rolesString = usuario.getRoles().stream()
                        .map(Enum::name)
                        .collect(Collectors.toList());
                model.addAttribute("rolesUsuario", rolesString);
            }
        }
        return "Profesor/gestor-archivos-profesor";
    }
}