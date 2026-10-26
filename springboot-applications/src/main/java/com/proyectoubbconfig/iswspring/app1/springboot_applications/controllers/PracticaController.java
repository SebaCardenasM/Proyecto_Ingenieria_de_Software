package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.CentroPractica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Colaborador;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Profesor;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.CentroPracticaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.ColaboradorRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.EstudianteRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PracticaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.ProfesorRepository;

import java.util.List;
import java.util.Optional;

@Controller
public class PracticaController {

    @Autowired
    private PracticaRepository practicaRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private ProfesorRepository profesorRepository;

    @Autowired
    private CentroPracticaRepository centroPracticaRepository;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @GetMapping("/practicas")
    public String listarPracticas(Model model) {
        List<Practica> listaPracticas = practicaRepository.findAll();
        model.addAttribute("practicas", listaPracticas);
        model.addAttribute("estudiantes", estudianteRepository.findAll());
        model.addAttribute("profesores", profesorRepository.findAll());
        return "Coordinador/practicas";
    }
    
    @GetMapping("/practicas/registro")
    public String mostrarFormulario(Model model) {
        model.addAttribute("practica", new Practica());
        model.addAttribute("estudiantes", estudianteRepository.findAll());
        model.addAttribute("profesores", profesorRepository.findAll());
        model.addAttribute("centros", centroPracticaRepository.findAll());
        model.addAttribute("colaboradores", colaboradorRepository.findAll());
        return "Coordinador/registro_practica";
    }

    @PostMapping("/practicas/guardar")
    public String guardarPractica(@ModelAttribute("practica") Practica practica, Model model) {
        
        // 1. Validación de Rango de Fechas
        if (practica.getFechaInicio() != null && practica.getFechaFin() != null) {
            if (practica.getFechaFin().isBefore(practica.getFechaInicio())) {
                model.addAttribute("error", "La fecha de fin no puede ser anterior a la fecha de inicio.");
                cargarAtributosFormulario(model);
                return "Coordinador/registro_practica";
            }
        }

        // 2. Manejo de Estudiante (Obligatorio)
        if (practica.getEstudiante() != null && practica.getEstudiante().getRut() != null && !practica.getEstudiante().getRut().isBlank()) {
            Optional<Estudiante> estOpt = estudianteRepository.findById(practica.getEstudiante().getRut());
            practica.setEstudiante(estOpt.orElse(null));
        } else {
            practica.setEstudiante(null);
        }

        // 3. Manejo de Profesor / Tutor Supervisor (Opcional)
        if (practica.getProfesor() != null && practica.getProfesor().getRut() != null && !practica.getProfesor().getRut().isBlank()) {
            Optional<Profesor> profOpt = profesorRepository.findById(practica.getProfesor().getRut());
            practica.setProfesor(profOpt.orElse(null));
        } else {
            practica.setProfesor(null);
        }

        // 4. Manejo de Centro de Práctica (Opcional) - Previene TransientPropertyValueException
        if (practica.getCentroPractica() != null && practica.getCentroPractica().getId() != null) {
            Optional<CentroPractica> centroOpt = centroPracticaRepository.findById(practica.getCentroPractica().getId());
            practica.setCentroPractica(centroOpt.orElse(null));
        } else {
            practica.setCentroPractica(null);
        }

        // 5. Manejo de Profesor Colaborador (Opcional) - Previene TransientPropertyValueException
        if (practica.getColaborador() != null && practica.getColaborador().getRut() != null && !practica.getColaborador().getRut().isBlank()) {
            Optional<Colaborador> colOpt = colaboradorRepository.findById(practica.getColaborador().getRut());
            practica.setColaborador(colOpt.orElse(null));
        } else {
            practica.setColaborador(null);
        }

        // 6. Guardar en Base de Datos
        practicaRepository.save(practica);
        return "redirect:/practicas";
    }

    private void cargarAtributosFormulario(Model model) {
        model.addAttribute("estudiantes", estudianteRepository.findAll());
        model.addAttribute("profesores", profesorRepository.findAll());
        model.addAttribute("centros", centroPracticaRepository.findAll());
        model.addAttribute("colaboradores", colaboradorRepository.findAll());
    }
}