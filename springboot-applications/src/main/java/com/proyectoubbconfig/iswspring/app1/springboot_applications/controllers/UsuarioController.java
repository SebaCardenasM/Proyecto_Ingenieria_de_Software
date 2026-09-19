package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Profesor;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Usuario;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.EstudianteRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.ProfesorRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private ProfesorRepository profesorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registroUsuario";
    }

   @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute("usuario") Usuario usuario, Model model) {
        // 1. Encriptar la contraseña recibida del formulario antes de guardar
        String passwordEncriptada = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(passwordEncriptada);

        // 2. Guardar el usuario base en la base de datos
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // 3. Crear el perfil específico según el rol elegido
        if ("ROLE_ESTUDIANTE".equals(usuario.getRol())) {
            Estudiante estudiante = new Estudiante();
            estudiante.setUsuario(usuarioGuardado);
            estudianteRepository.save(estudiante);
        } else if ("ROLE_PROFESOR".equals(usuario.getRol())) {
            Profesor profesor = new Profesor();
            profesor.setUsuario(usuarioGuardado);
            profesorRepository.save(profesor);
        }

        // Resetear el objeto en el modelo para limpiar las cajas de texto del formulario
        model.addAttribute("usuario", new Usuario());

        // Retornar directamente la plantilla actual sin hacer redirección al menú principal
        return "registroUsuario";
    }
}