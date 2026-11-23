package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.EstadoPractica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.EvaluacionPractica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Profesor;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PracticaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.ProfesorRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.UsuarioRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.EvaluacionPracticaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/evaluacion")
public class EvaluacionPracticaController {

    @Autowired
    private EvaluacionPracticaService evaluacionService;

    @Autowired
    private PracticaRepository practicaRepository;

    @Autowired
    private ProfesorRepository profesorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ------------------------------------------------------------------
    // 1. Configuración de componentes y pesos (%) por Práctica (1 a 6)
    //    Accesible para COORDINADOR y PROFESOR_ASIGNATURA
    // ------------------------------------------------------------------

    @GetMapping("/configuracion")
    @Transactional
    public String verConfiguracion(
            @RequestParam(defaultValue = "5") Integer numeroPractica,
            Model model,
            Authentication authentication) {

        int num = (numeroPractica == null || numeroPractica < 1 || numeroPractica > 6) ? 5 : numeroPractica;
        EvaluacionPractica plantilla = evaluacionService.obtenerOAsegurarPlantilla(num);
        Set<String> uidsConNotas = evaluacionService.obtenerUidsConNotas(num);

        cargarUsuario(model, authentication);
        model.addAttribute("numeroPractica", num);
        model.addAttribute("asignaturas", evaluacionService.mapaAsignaturas());
        model.addAttribute("componentes", plantilla.getComponentes());
        model.addAttribute("uidsConNotas", uidsConNotas);
        return "Evaluacion/configuracion-evaluacion";
    }

    @PostMapping("/configuracion/guardar")
    @Transactional
    public String guardarConfiguracion(
            @RequestParam Integer numeroPractica,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        int num = (numeroPractica == null || numeroPractica < 1 || numeroPractica > 6) ? 5 : numeroPractica;
        String[] uids = request.getParameterValues("uid");
        String[] nombres = request.getParameterValues("nombre");
        String[] pesos = request.getParameterValues("peso");

        try {
            int recalculadas = evaluacionService.guardarEstructura(num, uids, nombres, pesos);
            String msg = "Estructura de " + evaluacionService.nombreAsignatura(num) + " guardada correctamente.";
            if (recalculadas > 0) {
                msg += " Se recalcularon " + recalculadas + " práctica(s) en curso.";
            }
            redirectAttributes.addFlashAttribute("mensajeExito", msg);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        }

        return "redirect:/evaluacion/configuracion?numeroPractica=" + num;
    }

    // ------------------------------------------------------------------
    // 2. Listado de prácticas para ingresar/ver notas ponderadas
    // ------------------------------------------------------------------

    @GetMapping("/listado")
    @Transactional(readOnly = true)
    public String listarPracticasParaEvaluar(
            @RequestParam(required = false) Integer numeroPractica,
            @RequestParam(required = false) String q,
            Model model,
            Authentication authentication) {

        boolean esCoord = tieneRol(authentication, "ROLE_COORDINADOR");
        List<Practica> practicas;

        if (esCoord) {
            practicas = practicaRepository.findAll();
        } else {
            Profesor prof = profesorRepository.findById(authentication.getName()).orElse(null);
            practicas = prof != null ? practicaRepository.findByProfesor(prof) : List.of();
        }

        String filtroTexto = q != null ? q.trim().toLowerCase() : "";
        List<Practica> filtradas = new ArrayList<>();
        Map<Long, EvaluacionPractica> evaluaciones = new HashMap<>();

        for (Practica p : practicas) {
            if (numeroPractica != null && !numeroPractica.equals(p.getNumeroPractica())) {
                continue;
            }
            if (p.getEstudiante() != null) {
                p.getEstudiante().getNombre();
            }
            if (p.getCentroPractica() != null) {
                p.getCentroPractica().getNombre();
            }
            if (p.getProfesor() != null) {
                p.getProfesor().getNombre();
            }

            if (!filtroTexto.isEmpty()) {
                String est = p.getEstudiante() != null
                        ? (p.getEstudiante().getNombre() + " " + p.getEstudiante().getApellido() + " " + p.getEstudiante().getRut()).toLowerCase()
                        : "";
                if (!est.contains(filtroTexto)) {
                    continue;
                }
            }

            filtradas.add(p);
            evaluaciones.put(p.getId(), evaluacionService.obtenerVistaParaPractica(p));
        }

        filtradas.sort(Comparator
                .comparing(Practica::getNumeroPractica, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(p -> p.getEstudiante() != null && p.getEstudiante().getApellido() != null
                        ? p.getEstudiante().getApellido() : "", String.CASE_INSENSITIVE_ORDER));

        cargarUsuario(model, authentication);
        model.addAttribute("practicas", filtradas);
        model.addAttribute("evaluaciones", evaluaciones);
        model.addAttribute("asignaturas", evaluacionService.mapaAsignaturas());
        model.addAttribute("filtroNumero", numeroPractica);
        model.addAttribute("filtroQ", q != null ? q : "");
        return "Evaluacion/listado-evaluaciones";
    }

    // ------------------------------------------------------------------
    // 3. Evaluar una práctica específica (Calculadora Ponderada + Personalización)
    // ------------------------------------------------------------------

    @GetMapping("/practica/{idPractica}")
    @Transactional
    public String verEvaluarPractica(
            @PathVariable Long idPractica,
            Model model,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        Practica practica = practicaRepository.findById(idPractica).orElse(null);
        if (practica == null || !puedeGestionarPractica(practica, authentication)) {
            redirectAttributes.addFlashAttribute("mensajeError", "No tienes permisos para evaluar esa práctica.");
            return "redirect:/evaluacion/listado";
        }

        if (practica.getEstudiante() != null) practica.getEstudiante().getNombre();
        if (practica.getCentroPractica() != null) practica.getCentroPractica().getNombre();
        if (practica.getProfesor() != null) practica.getProfesor().getNombre();

        EvaluacionPractica evaluacion = evaluacionService.obtenerOAsegurarEvaluacionPractica(practica);

        cargarUsuario(model, authentication);
        model.addAttribute("practica", practica);
        model.addAttribute("nombreAsignatura", evaluacionService.nombreAsignatura(
                practica.getNumeroPractica() != null ? practica.getNumeroPractica() : 1));
        model.addAttribute("evaluacion", evaluacion);
        model.addAttribute("componentes", evaluacion.getComponentes());
        model.addAttribute("estados", EstadoPractica.values());
        return "Evaluacion/evaluar-practica";
    }

    @PostMapping("/practica/{idPractica}/guardar")
    @Transactional
    public String guardarEvaluacionPractica(
            @PathVariable Long idPractica,
            @RequestParam(defaultValue = "false") boolean personalizada,
            @RequestParam(required = false) EstadoPractica estadoPractica,
            HttpServletRequest request,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        Practica practica = practicaRepository.findById(idPractica).orElse(null);
        if (practica == null || !puedeGestionarPractica(practica, authentication)) {
            redirectAttributes.addFlashAttribute("mensajeError", "No tienes permisos para modificar esta práctica.");
            return "redirect:/evaluacion/listado";
        }

        String[] uids = request.getParameterValues("uid");
        String[] nombres = request.getParameterValues("nombre");
        String[] pesos = request.getParameterValues("peso");
        String[] notas = request.getParameterValues("nota");

        try {
            EvaluacionPractica guardada = evaluacionService.guardarEvaluacionPractica(
                    practica, uids, nombres, pesos, notas, personalizada, estadoPractica);
            String notaTxt = guardada.getNotaFinal() != null
                    ? String.format(Locale.US, "%.1f", guardada.getNotaFinal()).replace('.', ',')
                    : "sin notas aún";
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Evaluación guardada correctamente. Nota ponderada actual: " + notaTxt + ".");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        }

        return "redirect:/evaluacion/practica/" + idPractica;
    }

    @PostMapping("/practica/{idPractica}/restaurar")
    @Transactional
    public String restaurarEstructuraPractica(
            @PathVariable Long idPractica,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        Practica practica = practicaRepository.findById(idPractica).orElse(null);
        if (practica == null || !puedeGestionarPractica(practica, authentication)) {
            return "redirect:/evaluacion/listado";
        }

        evaluacionService.restaurarDesdePlantilla(practica);
        redirectAttributes.addFlashAttribute("mensajeExito",
                "Se restauró la estructura base de componentes de la asignatura.");
        return "redirect:/evaluacion/practica/" + idPractica;
    }

    // ------------------------------------------------------------------
    // Utilidades de sesión y permisos
    // ------------------------------------------------------------------

    private boolean puedeGestionarPractica(Practica practica, Authentication auth) {
        if (tieneRol(auth, "ROLE_COORDINADOR")) {
            return true;
        }
        return practica.getProfesor() != null
                && practica.getProfesor().getRut() != null
                && practica.getProfesor().getRut().equals(auth.getName());
    }

    private boolean tieneRol(Authentication auth, String roleName) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> roleName.equals(a.getAuthority()));
    }

    private void cargarUsuario(Model model, Authentication authentication) {
        boolean esCoord = tieneRol(authentication, "ROLE_COORDINADOR");
        model.addAttribute("esCoordinador", esCoord);
        if (authentication != null) {
            usuarioRepository.findByRut(authentication.getName()).ifPresent(u -> {
                model.addAttribute("usuarioActivo", u);
                model.addAttribute("rolesUsuario", u.getRoles().stream().map(Enum::name).toList());
            });
        }
    }
}
