package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Coordinador;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Profesor;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Rol;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Usuario;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.CoordinadorRepository;
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

    @Autowired(required = false)
    private CoordinadorRepository coordinadorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registroUsuario";
    }

    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute("usuario") Usuario usuario, Model model) {
        // 1. Encriptar la contraseña
        String passwordEncriptada = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(passwordEncriptada);

        // 2. Guardar el usuario base
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // 3. Crear el perfil específico evaluando el Enum Rol
        if (usuario.getRol() == Rol.ESTUDIANTE) {
            Estudiante estudiante = new Estudiante();
            estudiante.setUsuario(usuarioGuardado);
            estudianteRepository.save(estudiante);
        } else if (usuario.getRol() == Rol.PROFESOR) {
            Profesor profesor = new Profesor();
            profesor.setUsuario(usuarioGuardado);
            profesorRepository.save(profesor);
        } else if (usuario.getRol() == Rol.COORDINADOR && coordinadorRepository != null) {
            Coordinador coordinador = new Coordinador();
            coordinador.setUsuario(usuarioGuardado);
            coordinadorRepository.save(coordinador);
        }

        // Resetear el formulario
        model.addAttribute("usuario", new Usuario());
        return "registroUsuario";
    }
}