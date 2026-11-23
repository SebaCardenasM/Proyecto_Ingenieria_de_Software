package com.proyectoubbconfig.iswspring.app1.springboot_applications.services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.CamposPlanificacion;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Documento;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.EstadoDocumento;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Informe;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Notificacion;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Pauta;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Planificacion;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Profesor;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoDocumento;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoEvaluador;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Usuario;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.DocumentoRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.NotificacionRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.UsuarioRepository;


@Service
public class DocumentoService {
    private static final Set<String> EXTENSIONES_PERMITIDAS =
        Set.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "jpg", "jpeg", "png");

    // true = el estudiante debe pasar su borrador por el asistente antes de enviarlo al gestor
    @Value("${documentos.revision-ia.obligatoria:false}")
    private boolean revisionIaObligatoria;

    @Autowired
    private DocumentoRepository documentoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PracticaRepository practicaRepository;
    @Autowired
    private PlanificacionPdfService planificacionPdfService;
    // NUEVO: aviso al estudiante cuando se evalúa su informe
    @Autowired
    private NotificacionRepository notificacionRepository;
    @Autowired(required = false)
    private EvaluacionPracticaService evaluacionPracticaService;

    private final String CARPETA_SUBIDAS = "uploads/practicas/";

    public List<Documento> obtenerDocumentosSegunRol() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String rut = auth.getName(); // el username es el RUT (CustomUserDetailsService)

        boolean esCoordinador = tieneRol(auth, "ROLE_COORDINADOR");
        boolean esProfesorAsignatura = tieneRol(auth, "ROLE_PROFESOR_ASIGNATURA");
        boolean esTutor = tieneRol(auth, "ROLE_TUTOR");

        List<Documento> documentos;

        // Coordinador: ve todo lo ENVIADO (los borradores son privados del estudiante)
        if (esCoordinador) {
            documentos = soloEnviados(documentoRepository.findAll());
        }
        // Profesor de asignatura o tutor: ve lo ENVIADO de sus estudiantes
        else if (esProfesorAsignatura || esTutor) {
            documentos = soloEnviados(documentoRepository.findByAcademicoRut(rut));
        }
        // Estudiante: solo lo suyo, borradores incluidos (excluyendo pautas, que van en su propia pestaña)
        else {
            List<Documento> docsEstudiante = usuarioRepository.findById(rut)
                    .filter(u -> u instanceof Estudiante)
                    .map(u -> documentoRepository.findByEstudiante((Estudiante) u))
                    .orElse(List.of());

            return docsEstudiante.stream()
                    .filter(doc -> !(doc instanceof Pauta) && (doc.getTipoDocumento() == null || !doc.getTipoDocumento().name().equals("PAUTA")))
                    .collect(Collectors.toList());
        }

        // SI ES PROFESOR DE ASIGNATURA: Excluimos de forma estricta las PAUTAS de los tutores de su vista de gestor
        if (esProfesorAsignatura && !esTutor) {
            documentos = documentos.stream()
                    .filter(doc -> {
                        if (doc instanceof Pauta) {
                            return false;
                        }
                        if (doc.getTipoDocumento() == null) {
                            return true;
                        }
                        String tipo = doc.getTipoDocumento().toString().toUpperCase();
                        return !tipo.contains("PAUTA") && !tipo.contains("EVALUACION");
                    })
                    .collect(Collectors.toList());
        }

        return documentos;
    }

    private List<Documento> soloEnviados(List<Documento> docs) {
        return docs.stream().filter(d -> !d.isBorrador()).collect(Collectors.toList());
    }

    private boolean tieneRol(Authentication auth, String rol) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(rol));
    }

    private String rutDueno(Documento d) {
        if (d.getEstudiante() != null) return d.getEstudiante().getRut();
        if (d.getPractica() != null && d.getPractica().getEstudiante() != null) {
            return d.getPractica().getEstudiante().getRut();
        }
        return null;
    }

    // ===== CAMBIO 2: esRevisable =====
    // El asistente puede leer PDF, DOCX y las planificaciones en línea (se arman desde los campos del formulario).
    // Los demás formatos no quedan bloqueados por la revisión obligatoria.
    private boolean esRevisable(Documento d) {
        String ruta = d.getRutaServidor() == null ? "" : d.getRutaServidor().toLowerCase();
        return ruta.endsWith(".pdf") || ruta.endsWith(".docx") || ruta.equals("online_planificacion");
    }

    /** Paso 1 de la cadena: el archivo se sube y queda como BORRADOR (planificaciones e informes). */
    public Documento subirArchivo(MultipartFile archivo, Integer numeroPractica, TipoDocumento tipoDocumento) throws Exception {
        if (archivo == null || archivo.isEmpty()) {
            throw new Exception("El archivo está vacío");
        }

        // --- Nombre y extensión seguros ---
        String limpio = StringUtils.cleanPath(archivo.getOriginalFilename() == null ? "" : archivo.getOriginalFilename());
        String nombreVisible = limpio.substring(limpio.lastIndexOf('/') + 1); // descarta cualquier ruta
        if (nombreVisible.length() > 150 || nombreVisible.matches(".*[<>\"'`].*")) {
            throw new Exception("El nombre del archivo contiene caracteres no permitidos");
        }
        if (nombreVisible.isBlank()) {
            throw new Exception("El nombre del archivo no es válido");
        }
        String extension = StringUtils.getFilenameExtension(nombreVisible);
        if (extension == null || !EXTENSIONES_PERMITIDAS.contains(extension.toLowerCase())) {
            throw new Exception("Tipo de archivo no permitido. Formatos aceptados: " + EXTENSIONES_PERMITIDAS);
        }

        String identificador = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findById(identificador)
                .orElseThrow(() -> new Exception("No se encontró el usuario: " + identificador));

        if (!(usuario instanceof Estudiante)) {
            throw new Exception("El usuario autenticado no es un Estudiante.");
        }
        Estudiante estudiante = (Estudiante) usuario;

        Practica practicaEncontrada = null;
        if (estudiante.getPracticas() != null) {
            practicaEncontrada = estudiante.getPracticas().stream()
                    .filter(p -> numeroPractica.equals(p.getNumeroPractica()))
                    .findFirst()
                    .orElse(null);
        }

        // --- Ruta física controlada por nosotros ---
        Path directorio = Paths.get(CARPETA_SUBIDAS).toAbsolutePath().normalize();
        Files.createDirectories(directorio);

        String nombreFisico = UUID.randomUUID() + "." + extension.toLowerCase();
        Path rutaFinal = directorio.resolve(nombreFisico).normalize();
        if (!rutaFinal.startsWith(directorio)) {
            throw new Exception("Ruta de archivo no válida");
        }
        try (var in = archivo.getInputStream()) {
            Files.copy(in, rutaFinal, StandardCopyOption.REPLACE_EXISTING);
        }

        Documento nuevoDocumento;
        if (tipoDocumento == TipoDocumento.PLANIFICACION) {
            nuevoDocumento = new Planificacion();
        } else if (tipoDocumento == TipoDocumento.INFORME_FINAL) {
            nuevoDocumento = new Informe();
        } else if (tipoDocumento == TipoDocumento.PAUTA) {
            nuevoDocumento = new Pauta();
        } else {
            throw new Exception("Tipo de documento no soportado: " + tipoDocumento);
        }

        nuevoDocumento.setNombreArchivo(nombreVisible);                 // lo que ve el usuario
        nuevoDocumento.setRutaServidor(CARPETA_SUBIDAS + nombreFisico); // lo que se sirve por /uploads/**
        nuevoDocumento.setNumeroPractica(numeroPractica);
        nuevoDocumento.setPractica(practicaEncontrada);
        nuevoDocumento.setEstudiante(estudiante);
        // Planificaciones e informes pasan por la cadena (borrador -> revisión -> gestor)
        nuevoDocumento.setEstado(tipoDocumento == TipoDocumento.PAUTA ? EstadoDocumento.ENVIADO : EstadoDocumento.BORRADOR);

        return documentoRepository.save(nuevoDocumento);
    }

    /** Paso 3 de la cadena: el estudiante envía su borrador al gestor (queda visible para sus profesores). */
    @Transactional
    public Documento enviarAlGestor(Long idDocumento) throws Exception {
        Documento documento = documentoRepository.findById(idDocumento)
                .orElseThrow(() -> new Exception("No se encontró el documento con ID: " + idDocumento));

        String rutUsuario = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!rutUsuario.equals(rutDueno(documento))) {
            throw new Exception("Acceso denegado: este documento no es tuyo.");
        }
        if (!documento.isBorrador()) {
            throw new Exception("Este documento ya fue enviado al gestor.");
        }
        if (revisionIaObligatoria && esRevisable(documento) && !documento.isRevisadoIa()) {
            throw new Exception("Antes de enviarlo debes pasar el documento por la revisión del asistente.");
        }


        if (documento instanceof Planificacion pl && pl.isOnline()) {
            List<String> faltan = CamposPlanificacion.faltantes(pl.getCampos());
            if (!faltan.isEmpty()) {
                throw new Exception("Completa antes de enviar: " + String.join(", ", faltan) + ".");
            }
            pl.setRutaServidor(planificacionPdfService.generar(pl));   // ← nuevo
        }

        documento.setEstado(EstadoDocumento.ENVIADO);
        return documentoRepository.save(documento);
    }

    // =====================================================================
    //  NUEVO: evaluación de informes por el profesor
    // =====================================================================

    /**
     * El profesor de la práctica (o, si la práctica no tiene profesor, el coordinador del estudiante)
     * pone la nota del informe y, opcionalmente, una retroalimentación.
     */
    @Transactional
    public Informe evaluarInforme(Long idDocumento, Double nota, String retroalimentacion) throws Exception {
        Documento doc = documentoRepository.findById(idDocumento)
                .orElseThrow(() -> new Exception("No se encontró el documento."));
        if (!(doc instanceof Informe informe)) {
            throw new Exception("Solo se pueden evaluar informes.");
        }
        if (informe.isBorrador()) {
            throw new Exception("El informe aún es un borrador del estudiante.");
        }

        String rut = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!puedeEvaluar(informe, rut)) {
            throw new Exception("No tienes permiso para evaluar este informe.");
        }
        if (nota == null || nota < 1.0 || nota > 7.0) {
            throw new Exception("La nota debe estar entre 1,0 y 7,0.");
        }
        String retro = retroalimentacion == null ? "" : retroalimentacion.trim();
        if (retro.length() > 2000) {
            throw new Exception("La retroalimentación puede tener hasta 2000 caracteres.");
        }
        Profesor profesor = usuarioRepository.findById(rut)
                .filter(u -> u instanceof Profesor)
                .map(u -> (Profesor) u)
                .orElseThrow(() -> new Exception("No se encontró al profesor."));

        informe.setNota(Math.round(nota * 10) / 10.0);
        informe.setRetroalimentacion(retro.isEmpty() ? null : retro);
        informe.setFechaEvaluacion(LocalDateTime.now());
        informe.setProfesorEvaluador(profesor);
        documentoRepository.save(informe);

        // Aviso al estudiante
        Estudiante est = informe.getEstudiante() != null
                ? informe.getEstudiante()
                : (informe.getPractica() != null ? informe.getPractica().getEstudiante() : null);
        if (est != null) {
            String quien = (profesor.getNombre() + " " + profesor.getApellido()).trim();
            String notaTxt = String.format(Locale.US, "%.1f", informe.getNota()).replace('.', ',');
            notificacionRepository.save(new Notificacion(
                    est,
                    "Tu informe fue evaluado",
                    "Tu profesor " + quien + " evaluó «" + informe.getNombreArchivo() + "» con nota " + notaTxt
                            + (retro.isEmpty() ? "." : ". Incluyó retroalimentación."),
                    null));
        }

        // Actualizar automáticamente la evaluación de la práctica si existe
        if (evaluacionPracticaService != null && informe.getPractica() != null) {
            try {
                evaluacionPracticaService.obtenerOAsegurarEvaluacionPractica(informe.getPractica());
            } catch (Exception ignored) {}
        }

        return informe;
    }

    private boolean puedeEvaluar(Informe informe, String rut) {
        if (rut == null) return false;
        Practica pr = informe.getPractica();
        Estudiante e = informe.getEstudiante();
        if (pr == null && e != null && informe.getNumeroPractica() != null && e.getPracticas() != null) {
            final Integer n = informe.getNumeroPractica();
            pr = e.getPracticas().stream()
                    .filter(p -> n.equals(p.getNumeroPractica()))
                    .max(Comparator
                            .comparing(Practica::getAno, Comparator.nullsFirst(Comparator.<Integer>naturalOrder()))
                            .thenComparing(Practica::getSemestre, Comparator.nullsFirst(Comparator.<Integer>naturalOrder())))
                    .orElse(null);
            if (pr != null) {
                informe.setPractica(pr);
            }
        }
        if (pr != null && pr.getProfesor() != null && rut.equals(pr.getProfesor().getRut())) {
            return true;
        }
        if (e != null && e.getProfesorCoordinador() != null && rut.equals(e.getProfesorCoordinador().getRut())) {
            return true;
        }
        if (e != null && e.getPracticas() != null) {
            boolean esProfesor = e.getPracticas().stream()
                    .anyMatch(p -> p.getProfesor() != null && rut.equals(p.getProfesor().getRut())
                            && (informe.getNumeroPractica() == null || informe.getNumeroPractica().equals(p.getNumeroPractica())));
            if (esProfesor) {
                return true;
            }
        }
        return false;
    }

    @Transactional
    public Documento subirPautaTutor(MultipartFile archivo, Long idPractica) throws Exception {
        if (archivo == null || archivo.isEmpty()) {
            throw new Exception("El archivo está vacío");
        }

        // --- Validaciones de nombre y extensión ---
        String limpio = StringUtils.cleanPath(archivo.getOriginalFilename() == null ? "" : archivo.getOriginalFilename());
        String nombreVisible = limpio.substring(limpio.lastIndexOf('/') + 1);
        if (nombreVisible.length() > 150 || nombreVisible.matches(".*[<>\"'`].*")) {
            throw new Exception("El nombre del archivo contiene caracteres no permitidos");
        }

        String extension = StringUtils.getFilenameExtension(nombreVisible);
        if (extension == null || !EXTENSIONES_PERMITIDAS.contains(extension.toLowerCase())) {
            throw new Exception("Tipo de archivo no permitido. Formatos aceptados: " + EXTENSIONES_PERMITIDAS);
        }

        // --- Validaciones de Práctica y Permisos del Tutor ---
        Practica practica = practicaRepository.findById(idPractica)
                .orElseThrow(() -> new Exception("No se encontró la práctica con ID: " + idPractica));

        String rutTutorLogueado = SecurityContextHolder.getContext().getAuthentication().getName();
        if (practica.getTutor() == null || !practica.getTutor().getRut().equals(rutTutorLogueado)) {
            throw new Exception("Acceso denegado: No eres el tutor asignado a esta práctica.");
        }

        // --- Lógica del Límite: Máximo 4 Pautas ---
        // Solo cuentan las pautas del tutor: las del profesor colaborador llegan por enlace y no consumen este límite
        long cantidadPautas = practica.getArchivos().stream()
                .filter(d -> d instanceof Pauta || (d.getTipoDocumento() != null && d.getTipoDocumento().name().equals("PAUTA")))
                .filter(d -> !(d instanceof Pauta pa && pa.getTipoEvaluador() == TipoEvaluador.PROFESOR_COLABORADOR))
                .count();

        if (cantidadPautas >= 4) {
            throw new Exception("No puedes subir más archivos. Se ha alcanzado el límite de 4 pautas.");
        }

        // --- Guardado Físico ---
        Path directorio = Paths.get(CARPETA_SUBIDAS).toAbsolutePath().normalize();
        Files.createDirectories(directorio);

        String nombreFisico = UUID.randomUUID() + "." + extension.toLowerCase();
        Path rutaFinal = directorio.resolve(nombreFisico).normalize();

        try (var in = archivo.getInputStream()) {
            Files.copy(in, rutaFinal, StandardCopyOption.REPLACE_EXISTING);
        }

        // --- Registro en Base de Datos ---
        Pauta nuevaPauta = new Pauta();
        nuevaPauta.setNombreArchivo(nombreVisible);
        nuevaPauta.setRutaServidor(CARPETA_SUBIDAS + nombreFisico);
        nuevaPauta.setNumeroPractica(practica.getNumeroPractica());
        nuevaPauta.setPractica(practica);
        nuevaPauta.setEstudiante(practica.getEstudiante());
        nuevaPauta.setTipoDocumento(TipoDocumento.PAUTA);
        nuevaPauta.setTipoEvaluador(TipoEvaluador.TUTOR_UNIVERSIDAD);

        Usuario usuarioLogueado = usuarioRepository.findById(rutTutorLogueado).orElse(null);
        if (usuarioLogueado instanceof Profesor) {
            nuevaPauta.setProfesorEvaluador((Profesor) usuarioLogueado);
        }

        return documentoRepository.save(nuevaPauta);
    }

    public List<Documento> obtenerTodosLosDocumentos() {
        return soloEnviados(documentoRepository.findAll());
    }

    // ===== CAMBIO 4a: puedeVerArchivo ahora delega en puedeVer(Documento) =====
    // (la lógica de permisos que estaba aquí se movió al método de abajo, sin cambios de comportamiento)
    @Transactional(readOnly = true)
    public boolean puedeVerArchivo(String rutaRelativa) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;

        Optional<Documento> docOpt = documentoRepository.findFirstByRutaServidor(rutaRelativa)
                .or(() -> documentoRepository.findFirstByRutaServidor("/" + rutaRelativa));

        // Archivo sin registro en la BD: solo el coordinador (comportamiento anterior)
        if (docOpt.isEmpty()) {
            return tieneRol(auth, "ROLE_COORDINADOR");
        }

        return puedeVer(docOpt.get());
    }

    // ===== CAMBIO 4b: método nuevo (lo usa PlanificacionOnlineController para la vista de solo lectura) =====
    @Transactional(readOnly = true)
    public boolean puedeVer(Documento doc) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        String rutUsuario = auth.getName();
        boolean esDueno = rutUsuario.equals(rutDueno(doc));

        // Los borradores son privados: solo los ve su dueño
        if (doc.isBorrador()) return esDueno;

        if (tieneRol(auth, "ROLE_COORDINADOR") || esDueno) return true;

        // Tutor de la práctica a la que pertenece el documento
        if (doc.getPractica() != null && doc.getPractica().getTutor() != null
                && doc.getPractica().getTutor().getRut().equals(rutUsuario)) {
            return true;
        }

        return obtenerDocumentosSegunRol().stream()
                .anyMatch(d -> d.getId() != null && d.getId().equals(doc.getId()));
    }

    @Transactional
    public void eliminarDocumento(Long idDocumento) throws Exception {
        Documento documento = documentoRepository.findById(idDocumento)
                .orElseThrow(() -> new Exception("No se encontró el documento con ID: " + idDocumento));

        // Permisos: el coordinador borra todo; el estudiante puede descartar SUS borradores;
        // el tutor puede borrar documentos de su práctica
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean esCoordinador = tieneRol(auth, "ROLE_COORDINADOR");
        String rutUsuario = auth.getName();
        boolean esDuenoDeBorrador = documento.isBorrador() && rutUsuario.equals(rutDueno(documento));

        if (!esCoordinador && !esDuenoDeBorrador) {
            if (documento.getPractica() == null || documento.getPractica().getTutor() == null ||
                !documento.getPractica().getTutor().getRut().equals(rutUsuario)) {
                throw new Exception("Acceso denegado: No tienes permisos para eliminar este documento.");
            }
        }

        // 1. Borrar el archivo físico del disco si existe
        // ===== CAMBIO 5: los documentos "ONLINE_..." (pautas y planificaciones en línea) no tienen archivo físico =====
        try {
            if (documento.getRutaServidor() != null && !documento.getRutaServidor().startsWith("ONLINE_")) {
                Path rutaFisica = Paths.get(documento.getRutaServidor()).toAbsolutePath().normalize();
                Files.deleteIfExists(rutaFisica);
            }
        } catch (Exception e) {
            // Si el archivo físico no se encuentra, continuamos para limpiar la BD
        }

        // 2. Eliminar el registro de la base de datos
        documentoRepository.delete(documento);
    }

    public List<Documento> obtenerDocumentosDeEstudiantesParaProfesor() {
        return soloEnviados(documentoRepository.findAll()).stream()
                .filter(doc -> doc.getTipoDocumento() == null || !doc.getTipoDocumento().name().equals("PAUTA"))
                .collect(Collectors.toList());
    }

    public List<Documento> obtenerDocumentosMisEstudiantes() {
        return soloEnviados(documentoRepository.findAll()).stream()
                .filter(doc -> doc.getTipoDocumento() == null || !doc.getTipoDocumento().name().equals("PAUTA"))
                .collect(Collectors.toList());
    }

    public List<Documento> obtenerPautasDeEstudiante() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String rut = auth.getName();

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(rut);
        if (usuarioOpt.isPresent() && usuarioOpt.get() instanceof Estudiante) {
            Estudiante estudiante = (Estudiante) usuarioOpt.get();
            List<Documento> docs = documentoRepository.findByEstudiante(estudiante);
            return docs.stream()
                    .filter(doc -> doc instanceof Pauta || (doc.getTipoDocumento() != null && doc.getTipoDocumento().name().equals("PAUTA")))
                    // El estudiante solo ve pautas web ya enviadas; los borradores son privados del tutor
                    .filter(doc -> !"ONLINE_BORRADOR".equals(doc.getRutaServidor()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

}