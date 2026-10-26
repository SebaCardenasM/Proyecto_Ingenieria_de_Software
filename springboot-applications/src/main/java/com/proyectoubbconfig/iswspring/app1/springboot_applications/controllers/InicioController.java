package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Usuario;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    private String panelDe(String authority) {
        return switch (authority) {
            case "ROLE_ESTUDIANTE" -> "/estudiante/index";
            case "ROLE_PROFESOR_ASIGNATURA" -> "/profesor/index";
            case "ROLE_TUTOR" -> "/tutor/index";
            case "ROLE_COORDINADOR" -> "/index";
            default -> null;
        };
    }


    private Set<String> panelesDelUsuario(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(this::panelDe)
                .filter(p -> p != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

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

    @GetMapping("/")
    public String raiz(Authentication authentication) {
        return redirigirSegunRol(authentication);
    }

    @GetMapping("/seleccionar-rol")
    public String seleccionarRol(Model model, Authentication authentication) {
        if (!estaAutenticado(authentication)) {
            return "redirect:/login";
        }

        if (panelesDelUsuario(authentication).size() <= 1) {
            return redirigirSegunRol(authentication);
        }

        cargarDatosUsuario(model, authentication);
        return "seleccionarRol";
    }

    private String redirigirSegunRol(Authentication authentication) {
        if (!estaAutenticado(authentication)) {
            return "redirect:/login";
        }

        Set<String> paneles = panelesDelUsuario(authentication);

        if (paneles.size() > 1) {
            return "redirect:/seleccionar-rol";
        }
        if (paneles.size() == 1) {
            return "redirect:" + paneles.iterator().next();
        }

        return "redirect:/login?error";
    }

    @GetMapping("/index")
    public String inicio(Model model, Authentication authentication) {
        cargarDatosUsuario(model, authentication);
        return "Coordinador/index";
    }

    @GetMapping("/estudiante/index")
    public String inicioEstudiante(Model model, Authentication authentication) {
        cargarDatosUsuario(model, authentication);
        return "Estudiante/indexEstudiante";
    }

    @GetMapping("/profesor/index")
    public String inicioProfesor(Model model, Authentication authentication) {
        cargarDatosUsuario(model, authentication);
        return "Profesor/indexProfesor";
    }

    @GetMapping("/tutor/index")
    public String inicioTutor(Model model, Authentication authentication) {
        cargarDatosUsuario(model, authentication);
        return "Tutor/indexTutor";
    }
}