package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Documento;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Pauta;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoDocumento;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoEvaluador;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.DocumentoRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PracticaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/tutor")
public class TutorController {

    @Autowired
    private PracticaRepository practicaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DocumentoRepository documentoRepository;

    /* 1. Mostrar la lista de alumnos asignados al tutor */
    @GetMapping("/alumnos")
    @Transactional(readOnly = true)
    public String verAlumnosAsignados(Model model, Authentication authentication) {
        String rutTutor = authentication.getName();
        cargarDatosUsuario(model, rutTutor);

        List<Practica> practicasAsignadas = practicaRepository.findByTutorRut(rutTutor);

        for (Practica p : practicasAsignadas) {
            if (p.getEstudiante() != null) {
                p.getEstudiante().getNombre();
            }
            if (p.getCentroPractica() != null) {
                p.getCentroPractica().getNombre();
            }
        }

        model.addAttribute("practicas", practicasAsignadas);
        return "Tutor/alumnos-asignados";
    }

    /* 2. Mostrar el perfil detallado del estudiante para cargar y ver pautas */
    @GetMapping("/estudiante/{idPractica}")
    @Transactional(readOnly = true)
    public String verPerfilEstudiante(@PathVariable Long idPractica, Model model, Authentication authentication) {
        String rutTutor = authentication.getName();
        cargarDatosUsuario(model, rutTutor);

        Optional<Practica> practicaOpt = practicaRepository.findById(idPractica);

        if (practicaOpt.isPresent()) {
            Practica practica = practicaOpt.get();

            if (practica.getTutor() != null && practica.getTutor().getRut().equals(rutTutor)) {
                practica.getArchivos().size();
                if (practica.getEstudiante() != null) {
                    practica.getEstudiante().getNombre();
                }
                if (practica.getCentroPractica() != null) {
                    practica.getCentroPractica().getNombre();
                }
                if (practica.getProfesor() != null) {
                    practica.getProfesor().getNombre();
                }
                if (practica.getColaborador() != null) {
                    practica.getColaborador().getNombre();
                }

                List<Pauta> pautasOnline = new ArrayList<>();
                for (Documento doc : practica.getArchivos()) {
                    if (doc instanceof Pauta && doc.getRutaServidor() != null && doc.getRutaServidor().startsWith("ONLINE_")) {
                        pautasOnline.add((Pauta) doc);
                    }
                }

                model.addAttribute("practica", practica);
                model.addAttribute("estudiante", practica.getEstudiante());
                model.addAttribute("pautasOnline", pautasOnline);
                return "Tutor/perfil-estudiante-tutor";
            }
        }

        return "redirect:/tutor/alumnos";
    }

    /* 3. Mostrar la pauta de evaluación interactiva en blanco */
    @GetMapping("/estudiante/{idPractica}/pauta-interactiva")
    @Transactional(readOnly = true)
    public String mostrarPautaInteractiva(@PathVariable Long idPractica, Model model, Authentication authentication) {
        String rutTutor = authentication.getName();
        cargarDatosUsuario(model, rutTutor);

        Optional<Practica> practicaOpt = practicaRepository.findById(idPractica);

        if (practicaOpt.isPresent()) {
            Practica practica = practicaOpt.get();

            if (practica.getTutor() != null && practica.getTutor().getRut().equals(rutTutor)) {
                if (practica.getEstudiante() != null) {
                    practica.getEstudiante().getNombre();
                }
                if (practica.getCentroPractica() != null) {
                    practica.getCentroPractica().getNombre();
                }
                practica.getTutor().getNombre();

                model.addAttribute("practica", practica);
                model.addAttribute("estudiante", practica.getEstudiante());
                model.addAttribute("modoLectura", false);
                return "Tutor/pauta_observacion_interactiva";
            }
        }

        return "redirect:/tutor/alumnos";
    }

    /* 4. Guardar Pauta Online como BORRADOR o ENVIADA usando Pauta.java sin librerías externas */
    @PostMapping("/estudiante/{idPractica}/pauta-interactiva/guardar")
    @Transactional
    public String guardarPautaInteractiva(
            @PathVariable Long idPractica,
            @RequestParam(required = false) Long idPauta,
            @RequestParam(required = false) String curso,
            @RequestParam(required = false) String fechaObservacion,
            @RequestParam(required = false) String horaInicio,
            @RequestParam(required = false) String horaTermino,
            @RequestParam(required = false) String objetivoClase,
            @RequestParam(required = false) String comentarioObservaciones,
            @RequestParam(defaultValue = "BORRADOR") String accion,
            HttpServletRequest request,
            Authentication authentication) {

        String rutTutor = authentication.getName();
        Optional<Practica> practicaOpt = practicaRepository.findById(idPractica);

        if (practicaOpt.isPresent()) {
            Practica practica = practicaOpt.get();

            if (practica.getTutor() != null && practica.getTutor().getRut().equals(rutTutor)) {
                Pauta pauta = null;

                if (idPauta != null) {
                    Optional<Documento> docOpt = documentoRepository.findById(idPauta);
                    if (docOpt.isPresent() && docOpt.get() instanceof Pauta) {
                        pauta = (Pauta) docOpt.get();
                    }
                }

                if (pauta == null) {
                    pauta = new Pauta();
                }

                if ("ONLINE_ENVIADA".equals(pauta.getRutaServidor())) {
                    return "redirect:/tutor/estudiante/" + idPractica;
                }

                List<Integer> respuestas = new ArrayList<>();
                double sumaPuntaje = 0;

                for (int i = 1; i <= 20; i++) {
                    String val = request.getParameter("criterion" + i);
                    if (val != null && !val.isBlank()) {
                        int pts = Integer.parseInt(val);
                        respuestas.add(pts);
                        sumaPuntaje += pts;
                    } else {
                        respuestas.add(0);
                    }
                }

                String cursoTexto = (curso != null && !curso.isBlank()) ? curso.trim() : "Sin curso";
                String fechaTexto = (fechaObservacion != null && !fechaObservacion.isBlank()) ? fechaObservacion : "";

                String respuestasJsonArray = "[" + respuestas.stream().map(String::valueOf).collect(Collectors.joining(",")) + "]";
                String jsonObservaciones = "{"
                        + "\"curso\":\"" + escaparJson(cursoTexto) + "\","
                        + "\"fecha\":\"" + escaparJson(fechaTexto) + "\","
                        + "\"horaInicio\":\"" + escaparJson(horaInicio) + "\","
                        + "\"horaTermino\":\"" + escaparJson(horaTermino) + "\","
                        + "\"objetivo\":\"" + escaparJson(objetivoClase) + "\","
                        + "\"comentario\":\"" + escaparJson(comentarioObservaciones) + "\","
                        + "\"respuestas\":" + respuestasJsonArray
                        + "}";

                pauta.setPractica(practica);
                pauta.setEstudiante(practica.getEstudiante());
                pauta.setNumeroPractica(practica.getNumeroPractica());
                pauta.setTipoDocumento(TipoDocumento.PAUTA);
                pauta.setNombreArchivo("Pauta Online - " + cursoTexto + (fechaTexto.isEmpty() ? "" : " (" + fechaTexto + ")"));
                pauta.setFechaSubida(LocalDateTime.now());
                pauta.setRutaServidor("ENVIAR".equalsIgnoreCase(accion) ? "ONLINE_ENVIADA" : "ONLINE_BORRADOR");

                pauta.setPuntajeObtenido(sumaPuntaje);
                pauta.setObservaciones(jsonObservaciones);
                pauta.setTipoEvaluador(TipoEvaluador.TUTOR_UNIVERSIDAD);
                pauta.setProfesorEvaluador(practica.getTutor());

                documentoRepository.save(pauta);
                return "redirect:/tutor/estudiante/" + idPractica;
            }
        }

        return "redirect:/tutor/alumnos";
    }

    /* 5. Abrir una Pauta Online guardada (para seguir editando el borrador o verla enviada) */
    @GetMapping("/pauta-online/{idPauta}")
    @Transactional(readOnly = true)
    public String verOEditarPautaOnline(@PathVariable Long idPauta, Model model, Authentication authentication) {
        String rutUsuario = authentication.getName();
        cargarDatosUsuario(model, rutUsuario);

        Optional<Documento> docOpt = documentoRepository.findById(idPauta);

        if (docOpt.isPresent() && docOpt.get() instanceof Pauta) {
            Pauta pauta = (Pauta) docOpt.get();
            Practica practica = pauta.getPractica();

            if (practica.getEstudiante() != null) {
                practica.getEstudiante().getNombre();
            }
            if (practica.getCentroPractica() != null) {
                practica.getCentroPractica().getNombre();
            }
            if (practica.getTutor() != null) {
                practica.getTutor().getNombre();
            }

            boolean esModoLectura = "ONLINE_ENVIADA".equals(pauta.getRutaServidor());

            model.addAttribute("pauta", pauta);
            model.addAttribute("practica", practica);
            model.addAttribute("estudiante", practica.getEstudiante());
            model.addAttribute("modoLectura", esModoLectura);
            return "Tutor/pauta_observacion_interactiva";
        }

        return "redirect:/tutor/alumnos";
    }

    private String escaparJson(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\r", "")
                    .replace("\n", "\\n");
    }

    private void cargarDatosUsuario(Model model, String rut) {
        usuarioRepository.findByRut(rut).ifPresent(u -> {
            model.addAttribute("usuarioActivo", u);
            model.addAttribute("rolesUsuario", u.getRoles().stream().map(Enum::name).toList());
        });
    }
}