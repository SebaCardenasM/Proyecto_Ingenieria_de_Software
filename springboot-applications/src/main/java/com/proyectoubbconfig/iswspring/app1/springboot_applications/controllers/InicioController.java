package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Rol;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Usuario;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class InicioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private boolean estaAutenticado(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    // Panel de destino según el rol (sin default: si agregas un rol al enum, el compilador te avisa)
    private String panelDe(Rol rol) {
        return switch (rol) {
            case ESTUDIANTE -> "/estudiante/index";
            case PROFESOR_ASIGNATURA -> "/profesor/index";
            case TUTOR -> "/tutor/index";
            case COORDINADOR -> "/index";
        };
    }

    // Carga los datos del usuario autenticado y su colección de roles
    private void cargarDatosUsuario(Model model, Authentication authentication) {
        if (estaAutenticado(authentication)) {
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
    }

    // Raíz: 1 rol -> directo a su panel; varios roles -> selector
    @GetMapping("/")
    public String raiz(Authentication authentication) {
        if (!estaAutenticado(authentication)) {
            return "redirect:/login";
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByRut(authentication.getName());
        if (usuarioOpt.isEmpty() || usuarioOpt.get().getRoles().isEmpty()) {
            return "redirect:/login?error";
        }

        Collection<Rol> roles = usuarioOpt.get().getRoles();

        if (roles.size() > 1) {
            return "redirect:/seleccionar-rol";
        }

        return "redirect:" + panelDe(roles.iterator().next());
    }

    // Selector de Perfiles / Roles
    @GetMapping("/seleccionar-rol")
    public String seleccionarRol(Model model, Authentication authentication) {
        cargarDatosUsuario(model, authentication);
        return "seleccionarRol";
    }

    // Index Coordinador / Administrador General
    @GetMapping("/index")
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

    // Index Profesor de Asignatura
    @GetMapping("/profesor/index")
    public String inicioProfesor(Model model, Authentication authentication) {
        cargarDatosUsuario(model, authentication);
        return "indexProfesor";
    }

    // Index Tutor Supervisor
    @GetMapping("/tutor/index")
    public String inicioTutor(Model model, Authentication authentication) {
        cargarDatosUsuario(model, authentication);
        return "indexTutor";
    }
}