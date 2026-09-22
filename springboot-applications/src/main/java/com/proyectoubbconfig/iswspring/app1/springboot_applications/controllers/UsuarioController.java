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
    private PasswordEncoder passwordEncoder;

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        // Se envía el DTO plano al formulario
        model.addAttribute("usuario", new UsuarioRegistroDTO());
        return "registroUsuario";
    }

    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute("usuario") UsuarioRegistroDTO dto) {
        String passwordEncriptada = passwordEncoder.encode(dto.getPassword());

        // Según el rol seleccionado en el DTO, se instancia la entidad concreta adecuada
        if (dto.getRol() == Rol.ESTUDIANTE) {
            Estudiante estudiante = new Estudiante();
            copiarDatosBase(dto, estudiante, passwordEncriptada);
            estudianteRepository.save(estudiante);
            return "redirect:/estudiantes";

        } else if (dto.getRol() == Rol.PROFESOR) {
            Profesor profesor = new Profesor();
            copiarDatosBase(dto, profesor, passwordEncriptada);
            profesorRepository.save(profesor);
            return "redirect:/profesores";

        } else if (dto.getRol() == Rol.COORDINADOR && coordinadorRepository != null) {
            Coordinador coordinador = new Coordinador();
            copiarDatosBase(dto, coordinador, passwordEncriptada);
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