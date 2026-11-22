package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.UsuarioEdicionDTO;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Profesor;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Rol;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.ProfesorRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.UsuarioEdicionService;


import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.EstadoPractica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PracticaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.UsuarioRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.security.Principal;
import java.util.Comparator;

@Controller
public class ProfesorController {

    // Roles que se pueden asignar desde esta pantalla
    private static final Set<Rol> ROLES_ACADEMICOS =
            Set.of(Rol.PROFESOR_ASIGNATURA, Rol.TUTOR, Rol.COORDINADOR);

    @Autowired
    private ProfesorRepository profesorRepository;

    @Autowired
    private PracticaRepository practicaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private UsuarioEdicionService usuarioEdicionService;

    private static final Comparator<Practica> ORDEN_ESTUDIANTES = Comparator
            .comparing((Practica p) -> p.getEstudiante() == null || p.getEstudiante().getApellido() == null
                    ? "" : p.getEstudiante().getApellido(), String.CASE_INSENSITIVE_ORDER)
            .thenComparing(Practica::getNumeroPractica, Comparator.nullsLast(Comparator.<Integer>naturalOrder()))
            .thenComparing(Practica::getAno, Comparator.nullsLast(Comparator.<Integer>reverseOrder()))
            .thenComparing(Practica::getSemestre, Comparator.nullsLast(Comparator.<Integer>reverseOrder()));

    // "Mis Estudiantes" del profesor de asignatura (acceso por /profesor/** en SecurityConfig)
    @GetMapping("/profesor/estudiantes")
    @Transactional(readOnly = true)
    public String misEstudiantes(Model model, Principal principal) {
        Profesor profesor = profesorRepository.findById(principal.getName()).orElse(null);
        if (profesor == null) {
            return "redirect:/profesor/index";
        }

        List<Practica> practicas = practicaRepository.findByProfesor(profesor).stream()
                .sorted(ORDEN_ESTUDIANTES)
                .toList();

        model.addAttribute("practicas", practicas);
        model.addAttribute("total", practicas.size());
        model.addAttribute("pendientes", contarPracticas(practicas, EstadoPractica.PENDIENTE));
        model.addAttribute("aprobadas", contarPracticas(practicas, EstadoPractica.APROBADA));
        model.addAttribute("reprobadas", contarPracticas(practicas, EstadoPractica.REPROBADA));

        // Datos para la barra de navegación
        usuarioRepository.findByRut(principal.getName()).ifPresent(u -> {
            model.addAttribute("usuarioActivo", u);
            model.addAttribute("rolesUsuario", u.getRoles().stream().map(Enum::name).toList());
        });

        return "Profesor/mis-estudiantes";
    }

    private long contarPracticas(List<Practica> practicas, EstadoPractica estado) {
        return practicas.stream()
                .filter(p -> (p.getEstado() == null ? EstadoPractica.PENDIENTE : p.getEstado()) == estado)
                .count();
    }

    @GetMapping("/profesores")
    public String listarProfesores(Model model) {
        List<Profesor> listaProfesores = profesorRepository.findAll();
        model.addAttribute("profesores", listaProfesores);
        return "Coordinador/profesores";
    }

    @Transactional(readOnly = true)
    @GetMapping("/profesores/editar/{rut}")
    public String mostrarFormularioEditarProfesor(@PathVariable String rut, Model model) {
        Profesor p = profesorRepository.findById(rut)
                .orElseThrow(() -> new IllegalArgumentException("RUT de profesor inválido: " + rut));

        UsuarioEdicionDTO dto = new UsuarioEdicionDTO();
        dto.setNombre(p.getNombre());
        dto.setApellido(p.getApellido());
        dto.setCorreo(p.getCorreo());
        dto.setRoles(new HashSet<>(p.getRoles()));

        model.addAttribute("usuario", dto);
        model.addAttribute("rut", rut);
        return "Coordinador/editar_profesor";
    }

    @Transactional
    @PostMapping("/profesores/actualizar/{rut}")
    public String actualizarProfesor(@PathVariable String rut,
                                     @ModelAttribute("usuario") UsuarioEdicionDTO dto,
                                     Authentication authentication,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        Profesor p = profesorRepository.findById(rut)
                .orElseThrow(() -> new IllegalArgumentException("RUT de profesor inválido: " + rut));

        String error = usuarioEdicionService.validarDatosBase(rut, dto);

        if (error == null && (dto.getRoles() == null || dto.getRoles().isEmpty())) {
            error = "Error: Debe seleccionar al menos un rol.";
        }
        if (error == null && !ROLES_ACADEMICOS.containsAll(dto.getRoles())) {
            error = "Error: Alguno de los roles seleccionados no es válido.";
        }
        // Evita que el coordinador se quite a sí mismo el acceso al panel
        if (error == null && rut.equals(authentication.getName())
                && !dto.getRoles().contains(Rol.COORDINADOR)
                && p.getRoles().contains(Rol.COORDINADOR)) {
            error = "Error: No puedes quitarte a ti mismo el rol de Coordinador.";
        }

        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("rut", rut);
            return "Coordinador/editar_profesor";
        }

        usuarioEdicionService.aplicarDatosBase(p, dto);
        p.getRoles().clear();
        p.getRoles().addAll(dto.getRoles());
        profesorRepository.save(p);

        redirectAttributes.addFlashAttribute("mensajeExito",
                "Los datos de " + p.getNombre() + " " + p.getApellido() + " fueron actualizados exitosamente.");
        redirectAttributes.addFlashAttribute("tituloExito", "¡Datos actualizados!");
        return "redirect:/profesores";
    }
}