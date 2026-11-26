package com.proyectoubbconfig.iswspring.app1.springboot_applications.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.DatosPautaColaborador;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Colaborador;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.EnlaceEvaluacion;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Notificacion;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Pauta;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Planificacion;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoDocumento;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoEvaluador;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoPauta;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.ColaboradorRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.EnlaceEvaluacionRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.NotificacionRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PautaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PlanificacionRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PracticaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.util.RutUtil;

/**
 * Pauta en línea del profesor colaborador. No hay cuenta ni login: el acceso es solo
 * por el enlace temporal (token) que le entrega el estudiante.
 */
@Service
public class EvaluacionColaboradorService {

    public static final int TOTAL_CRITERIOS = 20;
    private static final Pattern CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public enum Estado { OK, INVALIDO, EXPIRADO, USADO }

    public record Validacion(Estado estado, DatosPautaColaborador datos) {}

    /** El enlace no se puede usar (no existe, venció o ya se usó). */
    public static class EnlaceNoDisponibleException extends RuntimeException {
        private final Estado estado;
        public EnlaceNoDisponibleException(Estado estado) {
            super("Enlace no disponible: " + estado);
            this.estado = estado;
        }
        public Estado getEstado() { return estado; }
    }

    @Autowired
    private EnlaceEvaluacionRepository enlaceRepository;

    @Autowired
    private PlanificacionRepository planificacionRepository;

    @Autowired
    private PautaRepository pautaRepository;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private PracticaRepository practicaRepository;

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private EvaluacionPracticaService evaluacionPracticaService;

    @Autowired
    private PlantillaPautaService plantillaPautaService;

    // ------------------------------------------------------------------ validar enlace

    @Transactional(readOnly = true)
    public Practica obtenerPracticaDeToken(String token) {
        if (token == null || token.isBlank()) return null;
        EnlaceEvaluacion enlace = enlaceRepository.findByToken(token).orElse(null);
        if (enlace == null || enlace.getPractica() == null) return null;
        Long id = enlace.getPractica().getId();
        return (id != null) ? practicaRepository.findById(id).orElse(null) : null;
    }

    @Transactional(readOnly = true)
    public Validacion validar(String token) {
        if (token == null || token.isBlank() || token.length() > 64) {
            return new Validacion(Estado.INVALIDO, null);
        }
        EnlaceEvaluacion enlace = enlaceRepository.findByToken(token).orElse(null);
        if (enlace == null) {
            return new Validacion(Estado.INVALIDO, null);
        }
        Estado estado = estadoDe(enlace, LocalDateTime.now());
        if (estado != Estado.OK) {
            return new Validacion(estado, null);
        }
        return new Validacion(Estado.OK, armarDatos(enlace));
    }

    private Estado estadoDe(EnlaceEvaluacion enlace, LocalDateTime ahora) {
        if (enlace.isUsado() || enlace.getPautaId() != null) return Estado.USADO;
        if (ahora.isAfter(enlace.getFechaExpiracion())) return Estado.EXPIRADO;

        Planificacion plan = planificacionRepository.findById(enlace.getPlanificacionId()).orElse(null);
        Practica pr = enlace.getPractica();
        if (plan == null || !plan.isRevisada() || !plan.isEvaluaConPautas()
                || pr == null || pr.getEstudiante() == null) {
            return Estado.INVALIDO;
        }
        return Estado.OK;
    }

    private DatosPautaColaborador armarDatos(EnlaceEvaluacion enlace) {
        Practica pr = enlace.getPractica();
        Estudiante e = pr.getEstudiante();
        Colaborador c = pr.getColaborador();
        String centro = pr.getCentroPractica() != null ? pr.getCentroPractica().getNombre() : "Sin Centro de Práctica asignado";
        return new DatosPautaColaborador(
                (e.getNombre() + " " + e.getApellido()).trim(),
                e.getRut(),
                centro,
                pr.getNumeroPractica(),
                c != null ? (c.getNombre() + " " + c.getApellido()).trim() : null,
                c != null,
                enlace.getFechaExpiracion());
    }

    // ------------------------------------------------------------------ guardar la pauta

    /**
     * Valida y guarda la pauta. Lanza IllegalArgumentException (mensaje para el usuario) si faltan datos
     * y EnlaceNoDisponibleException si el enlace ya no sirve.
     */
    @Transactional
    public Pauta guardarPauta(String token, Map<String, String> p) {
        if (token == null || token.length() > 64) {
            throw new EnlaceNoDisponibleException(Estado.INVALIDO);
        }
        EnlaceEvaluacion enlace = enlaceRepository.findByTokenParaActualizar(token)
                .orElseThrow(() -> new EnlaceNoDisponibleException(Estado.INVALIDO));
        Estado estado = estadoDe(enlace, LocalDateTime.now());
        if (estado != Estado.OK) {
            throw new EnlaceNoDisponibleException(estado);
        }

        Practica pr = enlace.getPractica();
        Estudiante estudiante = pr.getEstudiante();

        // ---- pauta seleccionada (identificador)
        String identificadorPauta = requerido(p, "identificadorPauta", 50, "Pauta a evaluar");
        TipoPauta tipoPauta = identificadorPauta.toUpperCase().contains("ORIENTACION")
                ? TipoPauta.ORIENTACION
                : TipoPauta.ESPECIALIDAD;
        int numObs = identificadorPauta.contains("2") ? 2 : 1;
        int totalCriterios = plantillaPautaService != null ? plantillaPautaService.obtenerTotalCriterios(tipoPauta) : tipoPauta.getTotalCriterios();

        // Validar que esta pauta no haya sido enviada ya para este estudiante
        final TipoPauta tpFinal = tipoPauta;
        final int noFinal = numObs;
        boolean yaEnviada = pautaRepository.findByPracticaId(pr.getId()).stream()
                .filter(pa -> pa.getTipoEvaluador() == TipoEvaluador.PROFESOR_COLABORADOR)
                .filter(pa -> "ONLINE_ENVIADA".equalsIgnoreCase(pa.getRutaServidor()))
                .anyMatch(pa -> pa.getTipoPauta() == tpFinal && pa.getNumeroObservacion() != null && pa.getNumeroObservacion() == noFinal);

        if (yaEnviada) {
            throw new IllegalArgumentException("La pauta seleccionada ya fue completada y enviada previamente para este estudiante.");
        }

        // ---- campos de la clase observada
        String curso = requerido(p, "curso", 100, "Curso");
        String objetivo = requerido(p, "objetivoClase", 2000, "Objetivo de la clase");
        String comentario = requerido(p, "comentarioObservaciones", 5000, "Observaciones generales");

        LocalDate fecha;
        LocalTime inicio;
        LocalTime termino;
        try {
            fecha = LocalDate.parse(requerido(p, "fechaObservacion", 10, "Fecha"));
            inicio = LocalTime.parse(requerido(p, "horaInicio", 5, "Hora de inicio"));
            termino = LocalTime.parse(requerido(p, "horaTermino", 5, "Hora de término"));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("La fecha o las horas ingresadas no son válidas.");
        }
        if (fecha.isAfter(LocalDate.now(ZoneId.of("America/Santiago")))) {
            throw new IllegalArgumentException("La fecha de la observación no puede ser futura.");
        }
        if (!termino.isAfter(inicio)) {
            throw new IllegalArgumentException("La hora de término debe ser posterior a la hora de inicio.");
        }

        // ---- criterios (1 a 7)
        int suma = 0;
        for (int i = 1; i <= totalCriterios; i++) {
            int valor;
            try {
                valor = Integer.parseInt(p.getOrDefault("criterion" + i, "").trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Debes evaluar todos los criterios (falta el criterio " + i + ").");
            }
            if (valor < 1 || valor > 7) {
                throw new IllegalArgumentException("El criterio " + i + " debe evaluarse entre 1 y 7.");
            }
            suma += valor;
        }

        // ---- profesor colaborador (sin cuenta): el de la práctica, o el que se identifica aquí
        Colaborador colaborador = pr.getColaborador();
        if (colaborador == null) {
            String rut = RutUtil.conPuntos(p.get("rutColaborador"));   // formato canónico: con puntos
            if (rut == null || !RutUtil.esValido(rut)) {
                throw new IllegalArgumentException("El RUT ingresado no es válido.");
            }
            String nombre = requerido(p, "nombreColaborador", 100, "Nombre");
            String apellido = requerido(p, "apellidoColaborador", 100, "Apellido");
            String correo = requerido(p, "correoColaborador", 150, "Correo");
            if (!CORREO.matcher(correo).matches()) {
                throw new IllegalArgumentException("El correo ingresado no es válido.");
            }

            colaborador = buscarColaborador(rut);
            if (colaborador == null) {
                colaborador = new Colaborador();
                colaborador.setRut(rut);
                colaborador.setNombre(nombre);
                colaborador.setApellido(apellido);
                colaborador.setCorreo(correo);
                colaborador = colaboradorRepository.save(colaborador);
            } else if (colaborador.getCorreo() == null
                    || !colaborador.getCorreo().trim().equalsIgnoreCase(correo)) {
                // RUT ya registrado: solo se asocia si el correo coincide; los datos guardados nunca se pisan
                throw new IllegalArgumentException(
                        "Los datos ingresados no coinciden con los de un profesor colaborador ya registrado con ese RUT. "
                        + "Revisa el correo o pide a coordinación que te asigne a esta práctica.");
            }
            pr.setColaborador(colaborador);     // queda ligado a la práctica
            practicaRepository.save(pr);
        }

        // ---- la pauta
        Pauta pauta = new Pauta();
        pauta.setPractica(pr);
        pauta.setEstudiante(estudiante);
        pauta.setNumeroPractica(pr.getNumeroPractica());
        pauta.setTipoDocumento(TipoDocumento.PAUTA);
        pauta.setTipoPauta(tipoPauta);
        pauta.setNumeroObservacion(numObs);

        String nombreTipo = (tipoPauta == TipoPauta.ORIENTACION ? "Orientación" : "Especialidad") + " (Obs. " + numObs + ")";
        pauta.setNombreArchivo("Pauta Colaborador " + nombreTipo + " - " + curso + " (" + fecha + ")");
        pauta.setFechaSubida(LocalDateTime.now());
        pauta.setRutaServidor("ONLINE_ENVIADA");
        pauta.setPuntajeObtenido((double) suma);
        pauta.setObservaciones(armarJson(p));
        pauta.setTipoEvaluador(TipoEvaluador.PROFESOR_COLABORADOR);
        pauta.setColaboradorEvaluador(colaborador);
        pauta = pautaRepository.save(pauta);

        // ---- el enlace queda usado (un solo uso)
        enlace.setUsado(true);
        enlace.setFechaUso(LocalDateTime.now());
        enlace.setPautaId(pauta.getId());
        enlaceRepository.save(enlace);

        // Sincronizar automáticamente la nota en componentes de evaluación
        evaluacionPracticaService.obtenerOAsegurarEvaluacionPractica(pr);

        // ---- aviso al estudiante
        notificacionRepository.save(new Notificacion(
                estudiante,
                "Tu profesor colaborador envió su pauta",
                "Tu profesor colaborador " + (colaborador.getNombre() + " " + colaborador.getApellido()).trim()
                        + " completó la pauta de evaluación de tu clase. Puedes revisarla en «Mis Pautas».",
                null));

        return pauta;
    }

    // ------------------------------------------------------------------ JSON de la pauta

    /** Mismo formato que la pauta del tutor, para reutilizar la vista de solo lectura. Tolera datos incompletos. */
    public String armarJson(Map<String, String> p) {
        StringBuilder respuestas = new StringBuilder("[");
        for (int i = 1; i <= TOTAL_CRITERIOS; i++) {
            int valor = 0;
            try {
                valor = Integer.parseInt(p.getOrDefault("criterion" + i, "").trim());
            } catch (NumberFormatException ignorado) {
                // sin respuesta
            }
            if (valor < 1 || valor > 7) valor = 0;
            respuestas.append(valor);
            if (i < TOTAL_CRITERIOS) respuestas.append(',');
        }
        respuestas.append(']');

        return "{"
                + "\"curso\":\"" + esc(p.get("curso")) + "\","
                + "\"fecha\":\"" + esc(p.get("fechaObservacion")) + "\","
                + "\"horaInicio\":\"" + esc(p.get("horaInicio")) + "\","
                + "\"horaTermino\":\"" + esc(p.get("horaTermino")) + "\","
                + "\"objetivo\":\"" + esc(p.get("objetivoClase")) + "\","
                + "\"comentario\":\"" + esc(p.get("comentarioObservaciones")) + "\","
                + "\"respuestas\":" + respuestas
                + "}";
    }

    private static String esc(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"':  sb.append("\\\""); break;
                case '\n': sb.append("\\n"); break;
                case '\t': sb.append("\\t"); break;
                case '\r': break;
                default:   sb.append(c < 0x20 ? ' ' : c);
            }
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------ utilidades

    private static String requerido(Map<String, String> p, String clave, int max, String campo) {
        String valor = p.get(clave);
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo \"" + campo + "\" es obligatorio.");
        }
        valor = valor.trim();
        if (valor.length() > max) {
            throw new IllegalArgumentException("El campo \"" + campo + "\" es demasiado largo (máximo " + max + " caracteres).");
        }
        return valor;
    }

    // Busca tolerando RUT guardados con o sin puntos (registros anteriores a RutUtil)
    private Colaborador buscarColaborador(String rut) {
        for (String clave : new String[] { rut, RutUtil.normalizar(rut) }) {
            if (clave == null) continue;
            Colaborador c = colaboradorRepository.findById(clave).orElse(null);
            if (c != null) return c;
        }
        return null;
    }
}