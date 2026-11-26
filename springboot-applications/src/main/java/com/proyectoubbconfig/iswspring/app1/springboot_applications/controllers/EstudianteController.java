package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.DatosPautaColaborador;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.NotasFiltro;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.PracticaPanelDTO;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.UsuarioEdicionDTO;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.CentroPractica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoPauta;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Colaborador;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Documento;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.EvaluacionPractica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Pauta;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Profesor;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoEvaluador;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Usuario;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.DocumentoRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.EstudianteRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PracticaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.ProfesorRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.EvaluacionPracticaService;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.UsuarioEdicionService;

@Controller
public class EstudianteController {

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private PracticaRepository practicaRepository;

    @Autowired
    private DocumentoRepository documentoRepository;

    @Autowired
    private ProfesorRepository profesorRepository;

    @Autowired
    private UsuarioEdicionService usuarioEdicionService;

    @Autowired
    private EvaluacionPracticaService evaluacionPracticaService;

    @Autowired
    private com.proyectoubbconfig.iswspring.app1.springboot_applications.services.DuplaService duplaService;

    @Autowired
    private com.proyectoubbconfig.iswspring.app1.springboot_applications.services.PlantillaPautaService plantillaPautaService;

    @GetMapping({"/estudiantes", "/portafolios"})
    public String listarEstudiantes(Model model) {
        model.addAttribute("estudiantes", estudianteRepository.findAll());
        return "Coordinador/estudiantes";
    }

    // Orden cronológico: el último de la lista es el intento vigente
    private static final Comparator<Practica> CRONOLOGICO = Comparator
            .comparing(Practica::getAno, Comparator.nullsFirst(Comparator.<Integer>naturalOrder()))
            .thenComparing(Practica::getSemestre, Comparator.nullsFirst(Comparator.<Integer>naturalOrder()));

    // Orden del plan de estudios (P1 a P6)
    private static final String[] NOMBRES_PRACTICA = {
            "Aproximación al sistema escolar",
            "Ambientes de aprendizaje",
            "Trabajo colaborativo en el aula",
            "Investigación en el aula",
            "Práctica pedagógica",
            "Práctica profesional"
    };

    @GetMapping("/estudiante/practica")
    @Transactional(readOnly = true)
    public String miPractica(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        // El username de sesión es el RUT (CustomUserDetailsService)
        Estudiante estudiante = estudianteRepository.findById(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado: " + principal.getName()));

        Map<Integer, List<Practica>> porNumero = practicaRepository.findPortafolioByRut(estudiante.getRut()).stream()
                .filter(p -> p.getNumeroPractica() != null
                        && p.getNumeroPractica() >= 1
                        && p.getNumeroPractica() <= NotasFiltro.TOTAL_PRACTICAS)
                .collect(Collectors.groupingBy(Practica::getNumeroPractica));

        // Sin ninguna práctica asignada no hay nada que mostrar: vuelve al panel con un aviso
        if (porNumero.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMensaje",
                    "Aún no tienes una práctica asignada. Consulta con coordinación.");
            return "redirect:/estudiante/index";
        }

        List<PracticaPanelDTO> paneles = new ArrayList<>();
        Map<String, EvaluacionPractica> evaluacionesPorIntento = new HashMap<>();
        Map<String, PracticaPanelDTO.ContactoDTO> duplasPorIntento = new HashMap<>();
        int ultimaExistente = 1;

        for (int n = 1; n <= NotasFiltro.TOTAL_PRACTICAS; n++) {
            List<Practica> cronologicas = porNumero.getOrDefault(n, List.of()).stream()
                    .sorted(CRONOLOGICO)
                    .toList();

            List<PracticaPanelDTO.IntentoDTO> intentos = new ArrayList<>();
            for (int i = 0; i < cronologicas.size(); i++) {
                Practica pr = cronologicas.get(i);
                int numIntento = i + 1;
                intentos.add(aIntento(pr, numIntento, cronologicas.size(), i == cronologicas.size() - 1));
                evaluacionesPorIntento.put(n + "_" + numIntento, evaluacionPracticaService.obtenerVistaParaPractica(pr));

                if (duplaService.permiteDuplas(n)) {
                    final String intentoKey = n + "_" + numIntento;
                    duplaService.buscarDuplaPorPractica(pr.getId()).ifPresent(d -> {
                        Estudiante comp = d.obtenerCompanero(estudiante.getRut());
                        if (comp != null) {
                            duplasPorIntento.put(intentoKey, contactoDe(comp));
                        }
                    });
                    if (!duplasPorIntento.containsKey(intentoKey)) {
                        duplaService.buscarDuplaPorEstudianteYPractica(estudiante.getRut(), n).ifPresent(d -> {
                            Estudiante comp = d.obtenerCompanero(estudiante.getRut());
                            if (comp != null) {
                                duplasPorIntento.put(intentoKey, contactoDe(comp));
                            }
                        });
                    }
                }
            }
            Collections.reverse(intentos);          // el vigente se muestra primero

            if (!intentos.isEmpty()) ultimaExistente = n;
            paneles.add(armarPanel(n, intentos));
        }

        // Pestaña abierta: la práctica actual del estudiante, o la más avanzada que exista
        Integer actual = estudiante.getPracticaActual();
        int tabActiva = (actual != null && porNumero.containsKey(actual)) ? actual : ultimaExistente;

        model.addAttribute("paneles", paneles);
        model.addAttribute("evaluacionesPorIntento", evaluacionesPorIntento);
        model.addAttribute("duplasPorIntento", duplasPorIntento);
        model.addAttribute("tabActiva", tabActiva);
        model.addAttribute("practicaActual", actual);
        model.addAttribute("usuarioActivo", estudiante);
        model.addAttribute("rolesUsuario", estudiante.getRoles().stream().map(Enum::name).toList());
        return "Estudiante/practica";
    }

    private PracticaPanelDTO armarPanel(int n, List<PracticaPanelDTO.IntentoDTO> intentos) {
        boolean hayCentro = intentos.stream().anyMatch(i -> i.centro() != null);
        boolean hayColaborador = intentos.stream().anyMatch(i -> i.colaborador() != null);
        boolean hayTutor = intentos.stream().anyMatch(i -> i.tutor() != null);
        return new PracticaPanelDTO(
                n,
                NOMBRES_PRACTICA[n - 1],
                !intentos.isEmpty(),
                n >= 3 || hayCentro,            // P1 y P2 son simulaciones en la universidad
                n >= 3 || hayColaborador,
                n == 6 || hayTutor,             // el tutor supervisor solo en la práctica profesional
                intentos);
    }

    private PracticaPanelDTO.IntentoDTO aIntento(Practica p, int intento, int total, boolean vigente) {
        return new PracticaPanelDTO.IntentoDTO(
                p.getId(), intento, total, vigente,
                p.getAno(), p.getSemestre(),
                p.getEstado() != null ? p.getEstado().name() : "PENDIENTE",
                p.getNotaAprobacion(),
                p.getFechaInicio(), p.getFechaFin(),
                contactoDe(p.getProfesor()),
                contactoDe(p.getTutor()),
                contactoDe(p.getColaborador()),
                centroDe(p.getCentroPractica()));
    }

    private PracticaPanelDTO.ContactoDTO contactoDe(Usuario u) {
        if (u == null) return null;
        return new PracticaPanelDTO.ContactoDTO(u.getRut(),
                ((u.getNombre() == null ? "" : u.getNombre()) + " " + (u.getApellido() == null ? "" : u.getApellido())).trim(),
                u.getCorreo(), u.getTelefono());
    }

    private PracticaPanelDTO.ContactoDTO contactoDe(Colaborador c) {
        if (c == null) return null;
        return new PracticaPanelDTO.ContactoDTO(c.getRut(),
                ((c.getNombre() == null ? "" : c.getNombre()) + " " + (c.getApellido() == null ? "" : c.getApellido())).trim(),
                c.getCorreo(), c.getTelefono());
    }

    private PracticaPanelDTO.CentroDTO centroDe(CentroPractica c) {
        if (c == null) return null;
        String direccion = ((c.getCalle() == null ? "" : c.getCalle()) + " " + (c.getNumero() == null ? "" : c.getNumero())).trim();
        return new PracticaPanelDTO.CentroDTO(c.getNombre(), direccion, c.getComuna(), c.getRegion());
    }

    // Gestor de evidencias del estudiante (planificaciones e informes)
    @GetMapping("/estudiante/archivos")
    @Transactional(readOnly = true)
    public String mostrarGestorArchivosEstudiante(Model model, Principal principal) {
        estudianteRepository.findById(principal.getName()).ifPresent(estudiante -> {
            List<Practica> misPracticas = practicaRepository.findByEstudiante(estudiante);
            model.addAttribute("usuarioActivo", estudiante);
            model.addAttribute("misPracticas", misPracticas);
        });
        return "Estudiante/gestor-archivos-estudiante";
    }

    // Página "Mis Pautas": las pautas del tutor / colaborador
    @GetMapping("/estudiante/pautas")
    public String mostrarPautasEstudiante() {
        return "Estudiante/pautas-estudiante";
    }

    // Página "Mis Calificaciones y Progreso Ponderado": visualización en vivo de componentes, notas y promedios
    @GetMapping({"/estudiante/calificaciones", "/estudiante/notas"})
    @Transactional(readOnly = true)
    public String verMisCalificaciones(
            @RequestParam(required = false) Long idPractica,
            Model model,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        Estudiante estudiante = estudianteRepository.findById(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado: " + principal.getName()));

        List<Practica> portafolio = practicaRepository.findPortafolioByRut(estudiante.getRut());
        if (portafolio == null || portafolio.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMensaje",
                    "Aún no tienes asignaturas de práctica registradas en el sistema.");
            return "redirect:/estudiante/index";
        }

        // Orden cronológico por número de práctica y periodo
        List<Practica> practicasOrdenadas = portafolio.stream()
                .filter(p -> p.getNumeroPractica() != null)
                .sorted(Comparator.comparing(Practica::getNumeroPractica, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Practica::getAno, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Practica::getSemestre, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        // Determinar la práctica seleccionada
        Practica practicaSeleccionada = null;
        if (idPractica != null) {
            practicaSeleccionada = practicasOrdenadas.stream()
                    .filter(p -> idPractica.equals(p.getId()))
                    .findFirst()
                    .orElse(null);
        }

        if (practicaSeleccionada == null) {
            Integer actual = estudiante.getPracticaActual();
            if (actual != null) {
                practicaSeleccionada = practicasOrdenadas.stream()
                        .filter(p -> actual.equals(p.getNumeroPractica()))
                        .findFirst()
                        .orElse(null);
            }
        }

        if (practicaSeleccionada == null && !practicasOrdenadas.isEmpty()) {
            practicaSeleccionada = practicasOrdenadas.get(practicasOrdenadas.size() - 1);
        }

        // Obtener la evaluación con pautas e informes sincronizados en modo lectura
        EvaluacionPractica evaluacion = evaluacionPracticaService.obtenerVistaParaPractica(practicaSeleccionada);

        // Resumen para selector de prácticas pasadas y actual
        record PracticaItemDTO(
                Long id,
                Integer numeroPractica,
                String nombreAsignatura,
                Integer ano,
                Integer semestre,
                String centroNombre,
                String estado,
                Double notaFinal,
                Double porcentajeEvaluado
        ) {}

        List<PracticaItemDTO> todasPracticas = new ArrayList<>();
        for (Practica p : practicasOrdenadas) {
            EvaluacionPractica ev = evaluacionPracticaService.obtenerVistaParaPractica(p);
            String nomAsig = evaluacionPracticaService.nombreAsignatura(
                    p.getNumeroPractica() != null ? p.getNumeroPractica() : 1);
            String centro = (p.getCentroPractica() != null) ? p.getCentroPractica().getNombre() : "Sin centro asignado";
            String est = (p.getEstado() != null) ? p.getEstado().name() : "PENDIENTE";
            Double nota = (ev != null && ev.getNotaFinal() != null) ? ev.getNotaFinal() : p.getNotaAprobacion();
            Double porc = (ev != null) ? ev.getPorcentajeEvaluado() : 0.0;

            todasPracticas.add(new PracticaItemDTO(
                    p.getId(),
                    p.getNumeroPractica(),
                    nomAsig,
                    p.getAno(),
                    p.getSemestre(),
                    centro,
                    est,
                    nota,
                    porc
            ));
        }

        model.addAttribute("practica", practicaSeleccionada);
        model.addAttribute("evaluacion", evaluacion);
        model.addAttribute("componentes", evaluacion != null ? evaluacion.getComponentes() : Collections.emptyList());
        model.addAttribute("nombreAsignatura", evaluacionPracticaService.nombreAsignatura(
                practicaSeleccionada.getNumeroPractica() != null ? practicaSeleccionada.getNumeroPractica() : 1));
        model.addAttribute("todasPracticas", todasPracticas);
        model.addAttribute("estudiante", estudiante);
        model.addAttribute("usuarioActivo", estudiante);
        model.addAttribute("rolesUsuario", estudiante.getRoles().stream().map(Enum::name).toList());

        return "Estudiante/notas-estudiante";
    }

    // El estudiante ve la pauta web que le envió su tutor, siempre bloqueada
    @GetMapping("/estudiante/pauta-online/{idPauta}")
    @Transactional(readOnly = true)
    public String verPautaOnline(@PathVariable Long idPauta, Model model, Principal principal) {
        Optional<Documento> docOpt = documentoRepository.findById(idPauta);

        if (docOpt.isPresent() && docOpt.get() instanceof Pauta pauta) {
            Practica practica = pauta.getPractica();

            Estudiante dueno = pauta.getEstudiante() != null
                    ? pauta.getEstudiante()
                    : (practica != null ? practica.getEstudiante() : null);

            boolean esSuya = dueno != null && dueno.getRut().equals(principal.getName());
            boolean enviada = "ONLINE_ENVIADA".equals(pauta.getRutaServidor());

            if (esSuya && enviada && practica != null) {
                if (pauta.getTipoEvaluador() == TipoEvaluador.PROFESOR_COLABORADOR) {
                    String centro = practica.getCentroPractica() != null
                            ? practica.getCentroPractica().getNombre()
                            : "Sin Centro de Práctica asignado";
                    model.addAttribute("datos", new DatosPautaColaborador(
                            (dueno.getNombre() + " " + dueno.getApellido()).trim(),
                            dueno.getRut(),
                            centro,
                            practica.getNumeroPractica(),
                            pauta.getNombreEvaluador(),
                            true,
                            null));
                    model.addAttribute("practica", practica);
                    model.addAttribute("nombreAsignatura", evaluacionPracticaService.nombreAsignatura(practica.getNumeroPractica() != null ? practica.getNumeroPractica() : 1));
                    model.addAttribute("pauta", pauta);
                    model.addAttribute("tipoPauta", pauta.getTipoPauta() != null ? pauta.getTipoPauta().name() : "ESPECIALIDAD");
                    model.addAttribute("pautaJson", pauta.getObservaciones());
                    model.addAttribute("modoLectura", true);
                    model.addAttribute("todasCompletadas", false);
                    model.addAttribute("vistaEstudiante", true);
                    model.addAttribute("esquemaPautaJson", plantillaPautaService.obtenerEsquemaActivoJson(pauta.getTipoPauta()));
                    return "Colaborador/evaluacion-colaborador";
                }

                if (practica.getCentroPractica() != null) practica.getCentroPractica().getNombre();
                if (practica.getTutor() != null) practica.getTutor().getNombre();

                model.addAttribute("pauta", pauta);
                model.addAttribute("practica", practica);
                model.addAttribute("estudiante", dueno);
                model.addAttribute("usuarioActivo", dueno);
                model.addAttribute("modoLectura", true);
                model.addAttribute("vistaEstudiante", true);
                model.addAttribute("tipoPauta", pauta.getTipoPauta());
                model.addAttribute("esquemaPautaJson", plantillaPautaService.obtenerEsquemaActivoJson(pauta.getTipoPauta()));
                return "Tutor/pauta_observacion_interactiva";
            }
        }
        return "redirect:/estudiante/pautas";
    }

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
        // La práctica actual no se modifica desde aquí; se gestiona desde el módulo de Prácticas
        e.setProfesorCoordinador(coordinador);
        estudianteRepository.save(e);

        redirectAttributes.addFlashAttribute("mensajeExito",
                "Los datos de " + e.getNombre() + " " + e.getApellido() + " fueron actualizados exitosamente.");
        redirectAttributes.addFlashAttribute("tituloExito", "¡Estudiante actualizado!");
        return "redirect:/estudiantes";
    }

    private void cargarAtributosEdicionEstudiante(Model model, String rut) {
        model.addAttribute("rut", rut);
        model.addAttribute("profesores", profesorRepository.findAll());
    }
}
