package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Documento;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Pauta;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.DocumentoRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.EstudianteRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PracticaRepository;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.NotasFiltro;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.UsuarioEdicionDTO;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Profesor;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.ProfesorRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.UsuarioEdicionService;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class EstudianteController {

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private PracticaRepository practicaRepository;

    @Autowired
    private DocumentoRepository documentoRepository;

    @GetMapping("/estudiantes")
    public String listarEstudiantes(Model model) {
        model.addAttribute("estudiantes", estudianteRepository.findAll());
        return "Coordinador/estudiantes";
    }

    // Gestor de evidencias del estudiante (planificaciones e informes)
    @GetMapping("/estudiante/archivos")
    @Transactional(readOnly = true)
    public String mostrarGestorArchivosEstudiante(Model model, Principal principal) {
        // El username de sesión es el RUT (CustomUserDetailsService)
        estudianteRepository.findById(principal.getName()).ifPresent(estudiante -> {
            List<Practica> misPracticas = practicaRepository.findByEstudiante(estudiante);
            model.addAttribute("usuarioActivo", estudiante);
            model.addAttribute("misPracticas", misPracticas);
        });
        return "Estudiante/gestor-archivos-estudiante";
    }

    // Página "Mis Pautas": las pautas del tutor / colaborador (la tabla se carga con
    // /api/documentos/pautas-estudiante, que ya filtra por el estudiante logueado)
    @GetMapping("/estudiante/pautas")
    public String mostrarPautasEstudiante() {
        return "Estudiante/pautas-estudiante";
    }

    // El estudiante ve la pauta web que le envió su tutor, siempre bloqueada
    @GetMapping("/estudiante/pauta-online/{idPauta}")
    @Transactional(readOnly = true)
    public String verPautaOnline(@PathVariable Long idPauta, Model model, Principal principal) {
        Optional<Documento> docOpt = documentoRepository.findById(idPauta);

        if (docOpt.isPresent() && docOpt.get() instanceof Pauta pauta) {
            Practica practica = pauta.getPractica();

            // Dueño de la pauta: el estudiante ligado al documento (o a su práctica)
            Estudiante dueno = pauta.getEstudiante() != null
                    ? pauta.getEstudiante()
                    : (practica != null ? practica.getEstudiante() : null);

            boolean esSuya = dueno != null && dueno.getRut().equals(principal.getName());
            boolean enviada = "ONLINE_ENVIADA".equals(pauta.getRutaServidor());

            if (esSuya && enviada && practica != null) {
                if (practica.getCentroPractica() != null) practica.getCentroPractica().getNombre();
                if (practica.getTutor() != null) practica.getTutor().getNombre();

                model.addAttribute("pauta", pauta);
                model.addAttribute("practica", practica);
                model.addAttribute("estudiante", dueno);
                model.addAttribute("usuarioActivo", dueno);
                model.addAttribute("modoLectura", true);
                model.addAttribute("vistaEstudiante", true);
                return "Tutor/pauta_observacion_interactiva";
            }
        }
        return "redirect:/estudiante/pautas";
    }

    @Autowired
    private ProfesorRepository profesorRepository;

    @Autowired
    private UsuarioEdicionService usuarioEdicionService;

    @Transactional(readOnly = true)
    @GetMapping("/estudiantes/editar/{rut}")
    public String mostrarFormularioEditarEstudiante(@PathVariable String rut, Model model) {
        Estudiante e = estudianteRepository.findById(rut)
                .orElseThrow(() -> new IllegalArgumentException("RUT de estudiante inválido: " + rut));

        UsuarioEdicionDTO dto = new UsuarioEdicionDTO();
        dto.setNombre(e.getNombre());
        dto.setApellido(e.getApellido());
        dto.setCorreo(e.getCorreo());
        dto.setAnoIngreso(e.getAnoIngreso());
        dto.setPracticaActual(e.getPracticaActual());
        dto.setProfesorCoordinadorRut(
                e.getProfesorCoordinador() != null ? e.getProfesorCoordinador().getRut() : null);

        model.addAttribute("usuario", dto);
        cargarAtributosEdicionEstudiante(model, rut);
        return "Coordinador/editar_estudiante";
    }

    @Transactional
    @PostMapping("/estudiantes/actualizar/{rut}")
    public String actualizarEstudiante(@PathVariable String rut,
                                       @ModelAttribute("usuario") UsuarioEdicionDTO dto,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        Estudiante e = estudianteRepository.findById(rut)
                .orElseThrow(() -> new IllegalArgumentException("RUT de estudiante inválido: " + rut));

        String error = usuarioEdicionService.validarDatosBase(rut, dto);

        if (error == null && dto.getPracticaActual() != null
                && (dto.getPracticaActual() < 1 || dto.getPracticaActual() > NotasFiltro.TOTAL_PRACTICAS)) {
            error = "Error: La práctica actual debe estar entre 1 y " + NotasFiltro.TOTAL_PRACTICAS + ".";
        }
        if (error == null && dto.getAnoIngreso() != null
                && (dto.getAnoIngreso() < 1950 || dto.getAnoIngreso() > 2100)) {
            error = "Error: El año de ingreso no es válido.";
        }

        Profesor coordinador = null;
        String rutCoord = dto.getProfesorCoordinadorRut();
        if (error == null && rutCoord != null && !rutCoord.isBlank()) {
            coordinador = profesorRepository.findById(rutCoord).orElse(null);
            if (coordinador == null) {
                error = "Error: El profesor coordinador seleccionado no existe.";
            }
        }

        if (error != null) {
            model.addAttribute("error", error);
            cargarAtributosEdicionEstudiante(model, rut);
            return "Coordinador/editar_estudiante";
        }

        usuarioEdicionService.aplicarDatosBase(e, dto);
        e.setAnoIngreso(dto.getAnoIngreso());
        e.setPracticaActual(dto.getPracticaActual());
        e.setProfesorCoordinador(coordinador);
        estudianteRepository.save(e);

        redirectAttributes.addFlashAttribute("mensajeExito",
                "Los datos de " + e.getNombre() + " " + e.getApellido() + " fueron actualizados correctamente.");
        return "redirect:/estudiantes";
    }

    private void cargarAtributosEdicionEstudiante(Model model, String rut) {
        model.addAttribute("rut", rut);
        model.addAttribute("profesores", profesorRepository.findAll());
    }
}
