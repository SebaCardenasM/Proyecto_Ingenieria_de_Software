package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import java.security.Principal;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.CamposPlanificacion;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.InfoPracticaEstudianteDTO;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.*;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.*;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.DocumentoService;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.PlanificacionOnlineService;

@Controller
public class PlanificacionOnlineController {

    private static final String VISTA = "Estudiante/planificacion-formulario";

    @Autowired private PlanificacionOnlineService service;
    @Autowired private PlanificacionRepository planificacionRepository;
    @Autowired private EstudianteRepository estudianteRepository;
    @Autowired private PracticaRepository practicaRepository;
    @Autowired private DocumentoService documentoService;
    @Autowired private com.proyectoubbconfig.iswspring.app1.springboot_applications.services.DuplaService duplaService;

    // ------------------------------------------------------------ estudiante: crear / editar / guardar

    @GetMapping("/estudiante/planificacion/nueva")
    public String nueva(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        Estudiante e = estudianteRepository.findById(principal.getName()).orElse(null);
        if (e == null) {
            return "redirect:/estudiante/index";
        }
        List<Practica> misPracticas = practicaRepository.findByEstudiante(e);
        Practica practicaActiva = misPracticas.stream()
                .filter(p -> p.getEstado() == EstadoPractica.PENDIENTE)
                .findFirst()
                .orElse(null);

        if (practicaActiva == null || practicaActiva.getNumeroPractica() == null) {
            redirectAttributes.addFlashAttribute("errorMensaje",
                    "No tienes una práctica activa actualmente para crear planificaciones.");
            return "redirect:/estudiante/index";
        }
        Map<String, String> valores = new HashMap<>();
        valores.put("asignatura", "Matemática");
        preparar(model, principal.getName(), valores, null, practicaActiva.getNumeroPractica(), false, "/estudiante/planificaciones");
        return VISTA;
    }

    @GetMapping("/estudiante/planificacion/{id}/editar")
    @Transactional(readOnly = true)
    public String editar(@PathVariable Long id, Model model, Principal principal) {
        Planificacion p = planificacionRepository.findById(id).orElse(null);
        if (p == null || !p.isOnline() || p.getEstudiante() == null
                || !p.getEstudiante().getRut().equals(principal.getName())) {
            return "redirect:/estudiante/planificaciones";
        }
        if (!p.isBorrador() && p.getEstado() != EstadoDocumento.CON_OBSERVACIONES) {
            return "redirect:/planificaciones/" + id;   // en revisión o autorizada: solo lectura
        }
        preparar(model, principal.getName(), new HashMap<>(p.getCampos()), p.getId(), p.getNumeroPractica(),
                false, "/estudiante/planificaciones");
        if (p.getComentarioProfesor() != null) {
            model.addAttribute("comentarioProfesor", p.getComentarioProfesor());
        }
        return VISTA;
    }

    @PostMapping("/estudiante/planificacion/guardar")
    public String guardar(@RequestParam Map<String, String> params,
                          @RequestParam(required = false) Long idPlanificacion,
                          @RequestParam(required = false) Integer numeroPractica,
                          @RequestParam(defaultValue = "BORRADOR") String accion,
                          Principal principal, Model model, RedirectAttributes ra) {
        boolean enviar = "ENVIAR".equalsIgnoreCase(accion);
        try {
            service.guardar(principal.getName(), idPlanificacion, numeroPractica, params, enviar);
            ra.addFlashAttribute("tituloExito", enviar ? "¡Planificación enviada!" : "¡Borrador guardado!");
            ra.addFlashAttribute("mensajeExito", enviar
                    ? "Tu planificación fue enviada a tu profesor para su revisión."
                    : "Tu planificación quedó guardada como borrador. Puedes revisarla con el asistente o seguir editándola.");
            return "redirect:/estudiante/planificaciones";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            preparar(model, principal.getName(), new HashMap<>(params), idPlanificacion, numeroPractica,
                    false, "/estudiante/planificaciones");
            return VISTA;
        }
    }

    // ------------------------------------------------------------ vista de solo lectura (estudiante, profesor, tutor, coordinación)

    @GetMapping("/planificaciones/{id}")
    @Transactional(readOnly = true)
    public String ver(@PathVariable Long id, Model model, Authentication auth) {
        Planificacion p = planificacionRepository.findById(id).orElse(null);
        if (p == null || !documentoService.puedeVer(p)) return "redirect:/";

        // Si ya fue autorizada y tiene PDF permanente en disco, redirige al archivo
        if (p.isRevisada() && p.getRutaServidor() != null && !Planificacion.RUTA_ONLINE.equals(p.getRutaServidor())) {
            return "redirect:/" + p.getRutaServidor();
        }

        String volver = "/";
        if (p.getEstudiante() != null && p.getEstudiante().getRut().equals(auth.getName())) volver = "/estudiante/planificaciones";
        else if (tiene(auth, "ROLE_PROFESOR_ASIGNATURA")) volver = "/profesor/planificaciones";
        else if (tiene(auth, "ROLE_COORDINADOR")) volver = "/gestor-archivos";
        else if (tiene(auth, "ROLE_TUTOR")) volver = "/profesor/archivos";

        model.addAttribute("secciones", CamposPlanificacion.SECCIONES);
        model.addAttribute("valores", new HashMap<>(p.getCampos()));
        model.addAttribute("idPlanificacion", p.getId());
        model.addAttribute("numeroPractica", p.getNumeroPractica());
        model.addAttribute("misPracticas", List.of());
        model.addAttribute("estudianteNombre", p.getEstudiante() != null
                ? p.getEstudiante().getNombre() + " " + p.getEstudiante().getApellido() : "");
        model.addAttribute("comentarioProfesor", p.getComentarioProfesor());
        model.addAttribute("estadoPlanificacion", p.getEstado() != null ? p.getEstado().name() : "");
        model.addAttribute("modoLectura", true);
        model.addAttribute("volver", volver);
        return VISTA;
    }

    // ------------------------------------------------------------ internos

    private boolean tiene(Authentication a, String rol) {
        return a.getAuthorities().stream().anyMatch(x -> x.getAuthority().equals(rol));
    }

    private void preparar(Model model, String rut, Map<String, String> valores, Long id, Integer numero,
                          boolean lectura, String volver) {
        Estudiante e = estudianteRepository.findById(rut).orElseThrow();
        // una opción por número de práctica
        Map<Integer, Practica> porNumero = new TreeMap<>();
        for (Practica p : practicaRepository.findByEstudiante(e)) {
            if (p.getNumeroPractica() != null) porNumero.merge(p.getNumeroPractica(), p,
                    (a, b) -> (b.getAno() != null && (a.getAno() == null || b.getAno() >= a.getAno())) ? b : a);
        }
        model.addAttribute("secciones", CamposPlanificacion.SECCIONES);
        model.addAttribute("valores", valores);
        model.addAttribute("idPlanificacion", id);
        model.addAttribute("numeroPractica", numero);
        model.addAttribute("misPracticas", new ArrayList<>(porNumero.values()));
        model.addAttribute("estudianteNombre", e.getNombre() + " " + e.getApellido());
        model.addAttribute("usuarioActivo", e);
        model.addAttribute("modoLectura", lectura);
        model.addAttribute("volver", volver);
    }

    // Página "Subir Documentos" (antes Mis Planificaciones, acceso: ROLE_ESTUDIANTE)
    @GetMapping("/estudiante/planificaciones")
    @Transactional(readOnly = true)
    public String misPlanificaciones(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        Estudiante e = estudianteRepository.findById(principal.getName()).orElseThrow();
        List<Practica> misPracticas = practicaRepository.findByEstudiante(e);

        // Buscar la práctica activa: aquella en estado PENDIENTE
        Practica practicaVigente = misPracticas.stream()
                .filter(p -> p.getEstado() == EstadoPractica.PENDIENTE)
                .findFirst()
                .orElse(null);

        // Si no tiene ninguna práctica activa actualmente, no puede acceder a esta pestaña
        // y se le redirige al inicio indicando que puede ver sus documentos anteriores en el gestor
        if (practicaVigente == null || practicaVigente.getNumeroPractica() == null) {
            redirectAttributes.addFlashAttribute("errorMensaje",
                    "No tienes una práctica activa actualmente para acceder a «Subir Documentos». Si deseas revisar tus documentos anteriores, puedes consultarlos en el Gestor de Documentos.");
            return "redirect:/estudiante/index";
        }

        Integer practicaActual = practicaVigente.getNumeroPractica();

        InfoPracticaEstudianteDTO infoPractica = construirInfoPractica(practicaActual, practicaVigente);

        if (duplaService.permiteDuplas(practicaActual)) {
            Estudiante comp = null;
            if (practicaVigente != null) {
                comp = duplaService.buscarDuplaPorPractica(practicaVigente.getId())
                        .map(d -> d.obtenerCompanero(e.getRut()))
                        .orElse(null);
            }
            if (comp == null) {
                comp = duplaService.buscarDuplaPorEstudianteYPractica(e.getRut(), practicaActual)
                        .map(d -> d.obtenerCompanero(e.getRut()))
                        .orElse(null);
            }
            if (comp != null) {
                model.addAttribute("duplaCompanero", comp);
            }
        }

        model.addAttribute("usuarioActivo", e);
        model.addAttribute("practicaActual", practicaActual);
        model.addAttribute("practicaVigente", practicaVigente);
        model.addAttribute("infoPractica", infoPractica);
        model.addAttribute("misPracticas", misPracticas);
        return "Estudiante/planificaciones";
    }

    private InfoPracticaEstudianteDTO construirInfoPractica(int num, Practica p) {
        String centro = null;
        String colaborador = null;
        String tutor = null;

        if (p != null) {
            if (p.getCentroPractica() != null) {
                centro = p.getCentroPractica().getNombre()
                        + (p.getCentroPractica().getComuna() != null ? " (" + p.getCentroPractica().getComuna() + ")" : "");
            }
            if (p.getColaborador() != null) {
                colaborador = (p.getColaborador().getNombre() + " " + p.getColaborador().getApellido()).trim();
            }
            if (p.getTutor() != null) {
                tutor = (p.getTutor().getNombre() + " " + p.getTutor().getApellido()).trim();
            }
        }

        return switch (num) {
            case 1 -> new InfoPracticaEstudianteDTO(
                    1,
                    "Aproximación al sistema escolar",
                    "Ciclo Inicial",
                    "bg-info text-dark",
                    "Simulación en aula universitaria",
                    "Semestre lectivo (Simulaciones)",
                    "Primer acercamiento a la cultura escolar. Análisis del marco normativo, PEI, reglamentos escolares y diseño de microclases simuladas en la universidad.",
                    "Sube aquí tus planificaciones de microclases simuladas y reportes de análisis institucional.",
                    "bi-building",
                    "inicial",
                    centro != null ? centro : "Campus La Castilla - Universidad del Bío-Bío",
                    null,
                    null
            );
            case 2 -> new InfoPracticaEstudianteDTO(
                    2,
                    "Ambientes de aprendizaje",
                    "Ciclo Inicial",
                    "bg-info text-dark",
                    "Simulación en aula universitaria",
                    "Semestre lectivo (Simulación didáctica)",
                    "Diseño y experimentación de secuencias didácticas orientadas a distintos ambientes, metodologías activas y estilos de aprendizaje en la enseñanza de la matemática.",
                    "Sube aquí las planificaciones de tus secuencias didácticas y reportes de simulación didáctica.",
                    "bi-easel2",
                    "inicial",
                    centro != null ? centro : "Campus La Castilla - Universidad del Bío-Bío",
                    null,
                    null
            );
            case 3 -> new InfoPracticaEstudianteDTO(
                    3,
                    "Trabajo colaborativo en el aula",
                    "Ciclo Intermedio",
                    "bg-warning text-dark",
                    "Docencia en duplas en centro educativo",
                    "4 semanas en colegio (3 clases diseñadas en dupla)",
                    "Primer acercamiento real al aula escolar. Trabajo en duplas de docentes en formación, enfocado en co-enseñanza, colaboración didáctica y docencia directa bajo guía de profesor/a colaborador/a.",
                    "Sube las planificaciones de tus clases colaborativas en dupla y tu informe final de avance de visitas.",
                    "bi-people-fill",
                    "intermedio",
                    centro != null ? centro : "Centro de práctica en asignación",
                    colaborador,
                    null
            );
            case 4 -> new InfoPracticaEstudianteDTO(
                    4,
                    "Investigación en el aula",
                    "Ciclo Intermedio",
                    "bg-warning text-dark",
                    "Docencia en duplas e investigación diagnóstica",
                    "4 semanas en colegio (2-3 clases + recogida de datos)",
                    "Acompañamiento docente y docencia directa en duplas enfocado en el diagnóstico y estudio de problemáticas de aprendizaje en matemática mediante instrumentos sistemáticos.",
                    "Sube tus planificaciones de docencia diagnóstica, instrumentos de recolección de datos e informe de investigación en el aula.",
                    "bi-search",
                    "intermedio",
                    centro != null ? centro : "Centro de práctica en asignación",
                    colaborador,
                    null
            );
            case 5 -> new InfoPracticaEstudianteDTO(
                    5,
                    "Práctica pedagógica",
                    "Ciclo Avanzado",
                    "bg-success text-white",
                    "Docencia directa individual y jefatura de curso",
                    "6 semanas en colegio (Docencia directa individual)",
                    "Conducción individual y autónoma del curso y rol de profesor jefe. Incorporación obligatoria del Diseño Universal para el Aprendizaje (DUA) y adaptaciones curriculares para la diversidad.",
                    "Las planificaciones deben incluir principios DUA. Al culminar el periodo lectivo debes subir tu Informe Final / TANEP.",
                    "bi-mortarboard-fill",
                    "avanzado-p5",
                    centro != null ? centro : "Centro de práctica en asignación",
                    colaborador,
                    null
            );
            case 6 -> new InfoPracticaEstudianteDTO(
                    6,
                    "Práctica profesional",
                    "Ciclo Avanzado (Residencia)",
                    "bg-primary text-white",
                    "Residencia docente (Tutor UBB + Profesor Colaborador)",
                    "13 semanas en colegio (Residencia docente continua)",
                    "Última etapa formativa previa al título docente. Desempeño profesional integral en el aula escolar, 8 pautas de evaluación y ejecución del Proyecto de Intervención Educativa.",
                    "Planificaciones continuas de unidad con DUA, evidencias del Proyecto de Intervención e Informe Final de Titulación.",
                    "bi-award-fill",
                    "avanzado-p6",
                    centro != null ? centro : "Centro de práctica en asignación",
                    colaborador,
                    tutor
            );
            default -> new InfoPracticaEstudianteDTO(
                    num,
                    "Práctica N° " + num,
                    "Línea de Prácticas",
                    "bg-secondary text-white",
                    "Docencia de formación matemática",
                    "Periodo académico ordinario",
                    "Práctica formativa del plan de estudios de Pedagogía en Educación Matemática.",
                    "Sube las planificaciones de tus clases y tu informe final de evaluación.",
                    "bi-journal-check",
                    "generica",
                    centro != null ? centro : "Centro escolar asignado",
                    colaborador,
                    tutor
            );
        };
    }
}