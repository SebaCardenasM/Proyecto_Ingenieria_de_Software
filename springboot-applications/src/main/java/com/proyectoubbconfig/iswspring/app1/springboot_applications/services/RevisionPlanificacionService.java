package com.proyectoubbconfig.iswspring.app1.springboot_applications.services;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.FilaPlanificacion;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.EnlaceEvaluacion;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Notificacion;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Planificacion;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Profesor;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.EnlaceEvaluacionRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.NotificacionRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PlanificacionRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PracticaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.ProfesorRepository;

/**
 * Revisión de planificaciones por el profesor UBB con dos ticks:
 *  - Tick 1: la planificación ya fue revisada (se avisa al estudiante).
 *  - Tick 2: la clase se evaluará con pautas (se genera el enlace temporal para el
 *    profesor colaborador y se avisa al estudiante). Solo se habilita después del tick 1.
 */
@Service
public class RevisionPlanificacionService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final SecureRandom RANDOM = new SecureRandom();

    // Cuánto dura el enlace (en días). Se puede cambiar en application.properties: evaluacion.enlace.dias=7
    @Value("${evaluacion.enlace.dias:7}")
    private int diasVigencia;

    @Autowired
    private PlanificacionRepository planificacionRepository;

    @Autowired
    private EnlaceEvaluacionRepository enlaceRepository;

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private ProfesorRepository profesorRepository;

    @Autowired
    private PracticaRepository practicaRepository;

    // ------------------------------------------------------------------ listado

    @Transactional(readOnly = true)
    public List<FilaPlanificacion> listar(String rutProfesor) {
        LocalDateTime ahora = LocalDateTime.now();
        return planificacionRepository.findParaRevisar(rutProfesor).stream()
                .map(p -> aFila(p, ahora))
                .toList();
    }

    private FilaPlanificacion aFila(Planificacion p, LocalDateTime ahora) {
        Practica pr = practicaDe(p);
        Estudiante e = estudianteDe(p, pr);

        String estado = "NINGUNO";
        LocalDateTime expira = null;
        boolean puedeReenviar = false;

        // El enlace existe solo cuando la clase se marcó para evaluarse con pautas (tick 2)
        if (p.isEvaluaConPautas()) {
            EnlaceEvaluacion ultimo = enlaceRepository
                    .findFirstByPlanificacionIdOrderByFechaCreacionDesc(p.getId()).orElse(null);
            if (ultimo != null) {
                expira = ultimo.getFechaExpiracion();
                if (ultimo.isUsado()) {
                    estado = "USADO";
                } else if (ahora.isAfter(expira)) {
                    estado = "EXPIRADO";
                    puedeReenviar = true;
                } else {
                    estado = "VIGENTE";
                    puedeReenviar = true;
                }
            }
        }

        String nombre = e == null ? "Desconocido"
                : ((e.getNombre() == null ? "" : e.getNombre()) + " " + (e.getApellido() == null ? "" : e.getApellido())).trim();

        return new FilaPlanificacion(
                p.getId(),
                p.getNombreArchivo(),
                p.getRutaServidor(),
                p.getFechaSubida(),
                nombre,
                e == null ? null : e.getRut(),
                p.getNumeroPractica() != null ? p.getNumeroPractica() : (pr != null ? pr.getNumeroPractica() : null),
                pr != null ? pr.getAno() : null,
                pr != null ? pr.getSemestre() : null,
                pr != null && pr.getCentroPractica() != null ? pr.getCentroPractica().getNombre() : null,
                p.isRevisada(),
                p.getFechaRevision(),
                p.isEvaluaConPautas(),
                estado,
                expira,
                puedeReenviar);
    }

    // ------------------------------------------------------------------ TICK 1: planificación revisada

    @Transactional(rollbackFor = Exception.class)
    public void marcarRevisada(Long idPlanificacion, String rutProfesor) throws Exception {
        Planificacion p = planificacionRepository.findById(idPlanificacion)
                .orElseThrow(() -> new Exception("No se encontró la planificación."));

        if (!puedeRevisar(p, rutProfesor)) {
            throw new Exception("No tienes permiso para revisar esta planificación.");
        }
        if (p.isRevisada()) {
            throw new Exception("Esta planificación ya fue marcada como revisada.");
        }
        Profesor profesor = profesorRepository.findById(rutProfesor)
                .orElseThrow(() -> new Exception("No se encontró al profesor."));

        p.setRevisada(true);
        p.setFechaRevision(LocalDateTime.now());
        p.setProfesorRevisor(profesor);
        planificacionRepository.save(p);

        // Aviso simple al estudiante (sin enlace)
        Estudiante e = estudianteDe(p, practicaDe(p));
        if (e != null) {
            String quien = (profesor.getNombre() + " " + profesor.getApellido()).trim();
            notificacionRepository.save(new Notificacion(
                    e,
                    "Tu planificación fue revisada",
                    "Tu profesor " + quien + " revisó la planificación «" + p.getNombreArchivo() + "».",
                    null));
        }
    }

    // ------------------------------------------------------------------ TICK 2: la clase se evaluará con pautas

    @Transactional(rollbackFor = Exception.class)
    public EnlaceEvaluacion marcarEvaluaConPautas(Long idPlanificacion, String rutProfesor) throws Exception {
        Planificacion p = planificacionRepository.findById(idPlanificacion)
                .orElseThrow(() -> new Exception("No se encontró la planificación."));

        if (!puedeRevisar(p, rutProfesor)) {
            throw new Exception("No tienes permiso para revisar esta planificación.");
        }
        if (!p.isRevisada()) {
            throw new Exception("Primero debes marcar la planificación como revisada.");
        }
        if (p.isEvaluaConPautas()) {
            throw new Exception("Esta clase ya fue marcada para evaluarse con pautas.");
        }

        Practica pr = practicaDe(p);
        Estudiante e = estudianteDe(p, pr);
        if (pr == null || e == null) {
            throw new Exception("La planificación no está asociada a una práctica del estudiante, "
                    + "por eso no se puede generar el enlace. Pide a coordinación que asigne la práctica.");
        }
        Profesor profesor = profesorRepository.findById(rutProfesor)
                .orElseThrow(() -> new Exception("No se encontró al profesor."));

        p.setPractica(pr);
        p.setEvaluaConPautas(true);
        p.setFechaMarcaPautas(LocalDateTime.now());
        planificacionRepository.save(p);

        return crearEnlaceYAviso(p, pr, e, profesor, false);
    }

    /** Genera un enlace nuevo (el anterior deja de servir) mientras el colaborador no haya enviado su pauta. */
    @Transactional(rollbackFor = Exception.class)
    public EnlaceEvaluacion reenviar(Long idPlanificacion, String rutProfesor) throws Exception {
        Planificacion p = planificacionRepository.findById(idPlanificacion)
                .orElseThrow(() -> new Exception("No se encontró la planificación."));

        if (!puedeRevisar(p, rutProfesor)) {
            throw new Exception("No tienes permiso para revisar esta planificación.");
        }
        if (!p.isEvaluaConPautas()) {
            throw new Exception("Primero debes marcar la clase para evaluarse con pautas.");
        }

        List<EnlaceEvaluacion> previos = enlaceRepository.findByPlanificacionId(idPlanificacion);
        if (previos.stream().anyMatch(EnlaceEvaluacion::isUsado)) {
            throw new Exception("El profesor colaborador ya envió su pauta; no se puede generar otro enlace.");
        }

        Practica pr = practicaDe(p);
        Estudiante e = estudianteDe(p, pr);
        if (pr == null || e == null) {
            throw new Exception("La planificación no está asociada a una práctica del estudiante.");
        }
        Profesor profesor = profesorRepository.findById(rutProfesor)
                .orElseThrow(() -> new Exception("No se encontró al profesor."));

        LocalDateTime ahora = LocalDateTime.now();
        for (EnlaceEvaluacion previo : previos) {
            if (previo.getFechaExpiracion().isAfter(ahora)) {
                previo.setFechaExpiracion(ahora);   // el enlace anterior queda invalidado
            }
        }
        enlaceRepository.saveAll(previos);

        return crearEnlaceYAviso(p, pr, e, profesor, true);
    }

    // ------------------------------------------------------------------ internos

    private EnlaceEvaluacion crearEnlaceYAviso(Planificacion p, Practica pr, Estudiante e,
                                               Profesor profesor, boolean reenvio) {
        EnlaceEvaluacion enlace = new EnlaceEvaluacion();
        enlace.setToken(generarToken());
        enlace.setPlanificacionId(p.getId());
        enlace.setPractica(pr);
        enlace.setFechaCreacion(LocalDateTime.now());
        enlace.setFechaExpiracion(LocalDateTime.now().plusDays(Math.max(1, diasVigencia)));
        enlace = enlaceRepository.save(enlace);

        String quien = (profesor.getNombre() + " " + profesor.getApellido()).trim();
        String titulo = reenvio
                ? "Nuevo enlace para tu profesor colaborador"
                : "Tu clase será evaluada con pautas";
        String mensaje = (reenvio
                ? "Tu profesor " + quien + " generó un nuevo enlace para la planificación «" + p.getNombreArchivo() + "». El enlace anterior ya no funciona. "
                : "Tu profesor " + quien + " indicó que la clase de la planificación «" + p.getNombreArchivo()
                    + "» (Práctica N° " + pr.getNumeroPractica() + ") se evaluará con pautas. Ya puedes dar tu clase. ")
                + "Entrega el enlace de abajo a tu profesor colaborador para que complete su pauta de evaluación en línea. "
                + "Es de un solo uso y vence el " + FMT.format(enlace.getFechaExpiracion()) + ".";

        notificacionRepository.save(new Notificacion(e, titulo, mensaje, enlace));
        return enlace;
    }

    // Solo el profesor de la práctica (o, si la práctica no tiene, el coordinador del estudiante)
    private boolean puedeRevisar(Planificacion p, String rut) {
        Practica pr = p.getPractica();
        if (pr != null && pr.getProfesor() != null) {
            return rut.equals(pr.getProfesor().getRut());
        }
        Estudiante e = p.getEstudiante();
        return e != null && e.getProfesorCoordinador() != null && rut.equals(e.getProfesorCoordinador().getRut());
    }

    // La práctica del documento; si no la tiene, la más reciente del estudiante con ese número
    private Practica practicaDe(Planificacion p) {
        if (p.getPractica() != null) return p.getPractica();
        Estudiante e = p.getEstudiante();
        Integer n = p.getNumeroPractica();
        if (e == null || n == null) return null;
        return practicaRepository.findByEstudiante(e).stream()
                .filter(x -> n.equals(x.getNumeroPractica()))
                .max(Comparator
                        .comparing(Practica::getAno, Comparator.nullsFirst(Comparator.<Integer>naturalOrder()))
                        .thenComparing(Practica::getSemestre, Comparator.nullsFirst(Comparator.<Integer>naturalOrder())))
                .orElse(null);
    }

    private Estudiante estudianteDe(Planificacion p, Practica pr) {
        if (p.getEstudiante() != null) return p.getEstudiante();
        return pr != null ? pr.getEstudiante() : null;
    }

    private String generarToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
