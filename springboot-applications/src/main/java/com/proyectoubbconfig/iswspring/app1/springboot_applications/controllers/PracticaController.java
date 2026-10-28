package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

import org.springframework.transaction.annotation.Transactional;

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


    // =========================================================================
    // SECCIÓN: GESTIÓN DE PRÁCTICAS (Listar, Registrar, Guardar, Editar)
    // =========================================================================

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
    public String guardarPractica(@ModelAttribute("practica") Practica practica, Model model, RedirectAttributes redirectAttributes) {
        
        // 1. VALIDACIONES ESTRICTAS DE CAMPOS VACÍOS
        if (practica.getEstudiante() == null || practica.getEstudiante().getRut() == null || practica.getEstudiante().getRut().isBlank()) {
            model.addAttribute("error", "Error: Es obligatorio seleccionar un estudiante.");
            cargarAtributosFormulario(model);
            return "Coordinador/registro_practica";
        }
        
        // Validación segura sin isBlank() en caso de que semestre sea numérico
        if (practica.getNumeroPractica() == null || practica.getAno() == null || practica.getSemestre() == null) {
            model.addAttribute("error", "Error: Los campos N° Práctica, Año y Semestre no pueden estar vacíos.");
            cargarAtributosFormulario(model);
            return "Coordinador/registro_practica";
        }

        // 2. VALIDACIONES ESTRICTAS DE FECHAS
        if (practica.getFechaInicio() == null || practica.getFechaFin() == null) {
            model.addAttribute("error", "Error: Debe ingresar de manera obligatoria la fecha de inicio y la fecha de fin.");
            cargarAtributosFormulario(model);
            return "Coordinador/registro_practica";
        }
        if (practica.getFechaFin().isBefore(practica.getFechaInicio())) {
            model.addAttribute("error", "Error lógico de fechas: La fecha de fin es imposible porque es anterior a la fecha de inicio.");
            cargarAtributosFormulario(model);
            return "Coordinador/registro_practica";
        }

        // 3. Manejo de Estudiante
        Optional<Estudiante> estOpt = estudianteRepository.findById(practica.getEstudiante().getRut());
        practica.setEstudiante(estOpt.orElse(null));

        // 4. Manejo de Profesor de Asignatura (Opcional)
        if (practica.getProfesor() != null && practica.getProfesor().getRut() != null && !practica.getProfesor().getRut().isBlank()) {
            Optional<Profesor> profOpt = profesorRepository.findById(practica.getProfesor().getRut());
            practica.setProfesor(profOpt.orElse(null));
        } else {
            practica.setProfesor(null);
        }

        // 5. Manejo de Tutor Supervisor (Opcional)
        if (practica.getTutor() != null && practica.getTutor().getRut() != null && !practica.getTutor().getRut().isBlank()) {
            Optional<Profesor> tutorOpt = profesorRepository.findById(practica.getTutor().getRut());
            practica.setTutor(tutorOpt.orElse(null));
        } else {
            practica.setTutor(null);
        }

        // 6. Manejo de Centro de Práctica (Opcional)
        if (practica.getCentroPractica() != null && practica.getCentroPractica().getId() != null) {
            Optional<CentroPractica> centroOpt = centroPracticaRepository.findById(practica.getCentroPractica().getId());
            practica.setCentroPractica(centroOpt.orElse(null));
        } else {
            practica.setCentroPractica(null);
        }

        // 7. Manejo de Profesor Colaborador (Opcional)
        if (practica.getColaborador() != null && practica.getColaborador().getRut() != null && !practica.getColaborador().getRut().isBlank()) {
            Optional<Colaborador> colOpt = colaboradorRepository.findById(practica.getColaborador().getRut());
            practica.setColaborador(colOpt.orElse(null));
        } else {
            practica.setColaborador(null);
        }

        // 8. Guardar en Base de Datos
        practicaRepository.save(practica);
        redirectAttributes.addFlashAttribute("mensajeExito", "Práctica asignada y guardada correctamente.");
        return "redirect:/index"; // Redirección actualizada al Dashboard
    }
    @Transactional(readOnly = true)
    @GetMapping("/practicas/editar/{id}")
    public String mostrarFormularioEditarPractica(@PathVariable Long id, Model model) {
        Practica practica = practicaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID de práctica inválido: " + id));
        model.addAttribute("practica", practica);
        cargarAtributosFormulario(model);
        return "Coordinador/editar_practica";
    }

    @PostMapping("/practicas/actualizar/{id}")
    public String actualizarPractica(@PathVariable Long id, @ModelAttribute("practica") Practica practica, Model model, RedirectAttributes redirectAttributes) {
        practica.setId(id);
        
        // 1. VALIDACIONES ESTRICTAS DE CAMPOS VACÍOS
        if (practica.getEstudiante() == null || practica.getEstudiante().getRut() == null || practica.getEstudiante().getRut().isBlank()) {
            model.addAttribute("error", "Error: Es obligatorio mantener asignado a un estudiante.");
            cargarAtributosFormulario(model);
            return "Coordinador/editar_practica";
        }
        
        // Validación segura sin isBlank() en caso de que semestre sea numérico
        if (practica.getNumeroPractica() == null || practica.getAno() == null || practica.getSemestre() == null) {
            model.addAttribute("error", "Error: Los campos N° Práctica, Año y Semestre no pueden quedar vacíos.");
            cargarAtributosFormulario(model);
            return "Coordinador/editar_practica";
        }

        // 2. VALIDACIONES ESTRICTAS DE FECHAS
        if (practica.getFechaInicio() == null || practica.getFechaFin() == null) {
            model.addAttribute("error", "Error: No puede dejar las fechas de inicio o fin en blanco.");
            cargarAtributosFormulario(model);
            return "Coordinador/editar_practica";
        }
        if (practica.getFechaFin().isBefore(practica.getFechaInicio())) {
            model.addAttribute("error", "Error lógico de fechas: La fecha de término ingresada es anterior a la fecha de inicio.");
            cargarAtributosFormulario(model);
            return "Coordinador/editar_practica";
        }

        // 3. Manejo de Estudiante
        Optional<Estudiante> estOpt = estudianteRepository.findById(practica.getEstudiante().getRut());
        practica.setEstudiante(estOpt.orElse(null));
        
        // 4. Manejo de Profesor de Asignatura (Opcional)
        if (practica.getProfesor() != null && practica.getProfesor().getRut() != null && !practica.getProfesor().getRut().isBlank()) {
            Optional<Profesor> profOpt = profesorRepository.findById(practica.getProfesor().getRut());
            practica.setProfesor(profOpt.orElse(null));
        } else {
            practica.setProfesor(null);
        }

        // 5. Manejo de Tutor Supervisor (Opcional)
        if (practica.getTutor() != null && practica.getTutor().getRut() != null && !practica.getTutor().getRut().isBlank()) {
            Optional<Profesor> tutorOpt = profesorRepository.findById(practica.getTutor().getRut());
            practica.setTutor(tutorOpt.orElse(null));
        } else {
            practica.setTutor(null);
        }

        // 6. Manejo de Centro de Práctica (Opcional)
        if (practica.getCentroPractica() != null && practica.getCentroPractica().getId() != null) {
            Optional<CentroPractica> centroOpt = centroPracticaRepository.findById(practica.getCentroPractica().getId());
            practica.setCentroPractica(centroOpt.orElse(null));
        } else {
            practica.setCentroPractica(null);
        }

        // 7. Manejo de Profesor Colaborador (Opcional)
        if (practica.getColaborador() != null && practica.getColaborador().getRut() != null && !practica.getColaborador().getRut().isBlank()) {
            Optional<Colaborador> colOpt = colaboradorRepository.findById(practica.getColaborador().getRut());
            practica.setColaborador(colOpt.orElse(null));
        } else {
            practica.setColaborador(null);
        }

        practicaRepository.save(practica);
        redirectAttributes.addFlashAttribute("mensajeExito", "Los datos de la práctica han sido actualizados correctamente.");
        return "redirect:/practicas";
    }


    // =========================================================================
    // SECCIÓN: GESTIÓN DE CENTROS DE PRÁCTICA (Listar, Nuevo, Guardar, Editar, Estudiantes)
    // =========================================================================

    @Transactional
    @GetMapping("/centros-practica")
    public String listarCentrosPractica(Model model) {
        List<CentroPractica> centros = centroPracticaRepository.findAll();
        centros.forEach(c -> {
            if (c.getPracticas() != null) {
                c.getPracticas().size(); 
            }
        });
        model.addAttribute("centros", centros);
        return "Coordinador/centros_practica";
    }

    @GetMapping("/centros-practica/nuevo")
    public String mostrarFormularioCentro(Model model) {
        model.addAttribute("centroPractica", new CentroPractica());
        return "Coordinador/registro_centro_practica";
    }

    @PostMapping("/centros-practica/guardar")
    public String guardarCentroPractica(@ModelAttribute("centroPractica") CentroPractica centroPractica, RedirectAttributes redirectAttributes) {
        CentroPractica guardado = centroPracticaRepository.save(centroPractica);
        redirectAttributes.addFlashAttribute("mensajeExito", "Centro de práctica '" + guardado.getNombre() + "' añadido correctamente.");
        return "redirect:/index";
    }

    @Transactional(readOnly = true)
    @GetMapping("/centros-practica/editar/{id}")
    public String mostrarFormularioEditarCentro(@PathVariable Long id, Model model) {
        CentroPractica centro = centroPracticaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID de centro inválido: " + id));
        model.addAttribute("centroPractica", centro);
        return "Coordinador/editar_centro_practica";
    }

    @PostMapping("/centros-practica/actualizar/{id}")
    public String actualizarCentroPractica(@PathVariable Long id, @ModelAttribute("centroPractica") CentroPractica centroPractica, RedirectAttributes redirectAttributes) {
        centroPractica.setId(id);
        CentroPractica guardado = centroPracticaRepository.save(centroPractica);
        redirectAttributes.addFlashAttribute("mensajeExito", "Centro de práctica '" + guardado.getNombre() + "' actualizado correctamente.");
        return "redirect:/centros-practica";
    }

    @Transactional(readOnly = true)
    @GetMapping("/centros-practica/{id}/estudiantes")
    public String verEstudiantesCentro(@PathVariable Long id, Model model) {
        CentroPractica centro = centroPracticaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID de centro inválido: " + id));
        if (centro.getPracticas() != null) {
            centro.getPracticas().size();
        }
        model.addAttribute("centro", centro);
        return "Coordinador/estudiantes_centro_practica";
    }


    // =========================================================================
    // MÉTODOS AUXILIARES
    // =========================================================================

    private void cargarAtributosFormulario(Model model) {
        model.addAttribute("estudiantes", estudianteRepository.findAll());
        model.addAttribute("profesores", profesorRepository.findAll());
        model.addAttribute("centros", centroPracticaRepository.findAll());
        model.addAttribute("colaboradores", colaboradorRepository.findAll());
    }
}
