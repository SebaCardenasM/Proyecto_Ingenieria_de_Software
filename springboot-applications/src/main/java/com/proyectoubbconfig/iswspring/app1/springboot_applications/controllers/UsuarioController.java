package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.UsuarioRegistroDTO;
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
    private EstudianteRepository estudianteRepository;

    @Autowired
    private ProfesorRepository profesorRepository;

    @Autowired(required = false)
    private CoordinadorRepository coordinadorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new UsuarioRegistroDTO());
        return "Coordinador/registroUsuario";
    }

    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute("usuario") UsuarioRegistroDTO dto) {
        if (usuarioRepository.existsById(dto.getRut())) {
            return "redirect:/usuarios/registro?duplicado";
        }
        String passwordEncriptada = passwordEncoder.encode(dto.getPassword());
        
        if (dto.getRol() == Rol.ESTUDIANTE) {
            Estudiante estudiante = new Estudiante();
            copiarDatosBase(dto, estudiante, passwordEncriptada);
            estudiante.addRol(Rol.ESTUDIANTE);
            estudianteRepository.save(estudiante);
            return "redirect:/estudiantes";

        } else if (dto.getRol() == Rol.PROFESOR_ASIGNATURA) {
            Profesor profesor = new Profesor();
            copiarDatosBase(dto, profesor, passwordEncriptada);
            profesor.addRol(Rol.PROFESOR_ASIGNATURA);
            profesorRepository.save(profesor);
            return "redirect:/profesores";

        } else if (dto.getRol() == Rol.TUTOR) {

            Profesor tutor = new Profesor();
            copiarDatosBase(dto, tutor, passwordEncriptada);
            tutor.getRoles().clear();
            tutor.addRol(Rol.TUTOR);
            profesorRepository.save(tutor);
            return "redirect:/profesores";

        } else if (dto.getRol() == Rol.COORDINADOR && coordinadorRepository != null) {
            Coordinador coordinador = new Coordinador();
            copiarDatosBase(dto, coordinador, passwordEncriptada);
            coordinador.addRol(Rol.COORDINADOR);
            coordinadorRepository.save(coordinador);
            return "redirect:/";
        }

        return "redirect:/login?registrado";
    }

    private void copiarDatosBase(UsuarioRegistroDTO origen, Usuario destino, String passwordEncriptada) {
        destino.setRut(origen.getRut());
        destino.setNombre(origen.getNombre());
        destino.setApellido(origen.getApellido());
        destino.setCorreo(origen.getCorreo());
        destino.setPassword(passwordEncriptada);
    }
}